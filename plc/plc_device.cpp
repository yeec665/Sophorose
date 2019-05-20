#include "plc_device.h"

TS7Area::TS7Area(word aNumber, word aSize) {
    Number = aNumber;
    Size = aSize;
    PData = new byte[aSize];
    cs = new TSnapCriticalSection();
}

TS7Area::~TS7Area() {
    if (PData != NULL) {
        delete[] PData;
    }
    if (cs != NULL) {
        delete cs;
    }
}

TS7AreaContainer::TS7AreaContainer(size_t aCapacity) {
    Capacity = aCapacity;
    PList = new PS7Area[aCapacity];
    memset(PList, NULL, aCapacity);
    AreaCount = 0;
    MaxIndex = 0;
}

TS7AreaContainer::~TS7AreaContainer() {
    for (int i = 0; i < Capacity; i++) {
        if (PList[i] != NULL) {
            PS7Area Area = PList[i];
            PList[i] = NULL;
            delete Area;
        }
    }
    AreaCount = 0;
    MaxIndex = 0;
    delete PList;
}

PS7Area TS7AreaContainer::Get(word Index) {
    if (0 <= Index && Index < Capacity) {
        return PList[Index];
    } else {
        return NULL;
    }
}

PS7Area TS7AreaContainer::Find(word Number) {
    int index = IndexOf(Number);
    return index < 0 ? NULL : PList[index];
}

int TS7AreaContainer::FindFirstFree() {
    for (int i = 0; i < Capacity; i++) {
        if (PList[i] == NULL) {
            return i;
        }
    }
    return -1;
}

int TS7AreaContainer::IndexOf(word Number) {
    for (int i = 0; i <= MaxIndex; i++) {
        if (PList[i] != NULL && PList[i]->Number == Number) {
            return i;
        }
    }
    return -1;
}

int TS7AreaContainer::Add(PS7Area NewArea) {
    if (IndexOf(NewArea->Number) != -1) {
        return errSrvAreaAlreadyExists;
    }
    int Index = FindFirstFree();
    if (Index == -1) {
        return errSrvTooManyDB;
    }
    PList[Index] = NewArea;
    AreaCount++;
    if (MaxIndex < Index) {
        MaxIndex = Index;
    }
    return 0;
}

int TS7AreaContainer::Remove(word Number) {
    int Index = IndexOf(Number);
    if (Index == -1) {
        return errSrvInvalidParams;
    }
    PS7Area Area = PList[Index];
    PList[Index] = NULL;
    delete Area;
    AreaCount--;
    return 0;
}

TPlcMemory::TPlcMemory() {
    PE = new TS7Area(0, 128);
    PA = new TS7Area(0, 128);
    MK = new TS7Area(0, 2048);
    CT = new TS7Area(0, 512);
    TM = new TS7Area(0, 512);
    DB = new TS7AreaContainer(128);
    DB->Add(new TS7Area(1, 256));
    DB->Add(new TS7Area(2, 256));
    DB->Add(new TS7Area(3, 256));
    OB = new TS7AreaContainer(128);
    OB->Add(new TS7Area(1, 302));
    OB->Add(new TS7Area(100, 186));
    fillBlockHead(OB);
    FB = new TS7AreaContainer(128);
    FC = new TS7AreaContainer(128);
    SDB = new TS7AreaContainer(128);
}

TPlcMemory::~TPlcMemory() {
    delete PE;
    delete PA;
    delete MK;
    delete CT;
    delete TM;
    delete DB;
    delete OB;
    delete FB;
    delete FC;
    delete SDB;
}

PS7AreaContainer TPlcMemory::getArea(byte blkType) {
    switch (blkType) {
        case Block_DB:
            return DB;
        case Block_OB:
            return OB;
        case Block_FB:
            return FB;
        case Block_FC:
            return FC;
        case Block_SDB:
            return SDB;
    }
    return NULL;
}

void TPlcMemory::fillBlockHead(PS7AreaContainer BP) {
    for (int i = 0; i < BP->AreaCount; i++) {
        PS7CompactBlockInfo PCB = (PS7CompactBlockInfo) (BP->PList[i]->PData);
        PCB->Cst_pp = 0x0707;
        PCB->Uk_01 = 0x01;
        PCB->BlkFlags = 0x01;
        PCB->BlkLang = 0x01; // STL
        PCB->SubBlkType = 0x08; // OB
        PCB->BlkNum = SwapWord(BP->PList[i]->Number);
        PCB->LenLoadMem = SwapDWord((longword) (BP->PList[i]->Size));
        PCB->BlkSec = 0x00000000;
        PCB->CodeTime_ms = 0x02AA829C;
        PCB->CodeTime_dy = 0x4FE7;
        PCB->IntfTime_ms = 0x03A3E47F;
        PCB->IntfTime_dy = 0x23EA;
        PCB->SbbLen = 0x001C;
        PCB->AddLen = 0x0006;
        PCB->LocDataLen = 0x0014;
        PCB->MC7Len = 0x000A;
    }
}

void S7API srvCallback(void* UserPtr, PSrvEvent PEvent, int Size) {
    std::cout<<"INSERT INTO s7_events SET";
    std::cout<<" time=\'"<<(int32_t)(PEvent->EvtTime);
    std::cout<<"\',host=\'"<<*((int32_t*) UserPtr);
    std::cout<<"\',es=\'"<<(int32_t)(SwapDWord(PEvent->EvtSender));
    std::cout<<"\',ec=\'"<<(int32_t)(PEvent->EvtCode);
    std::cout<<"\',erc=\'"<<(int16_t)(PEvent->EvtRetCode);
    std::cout<<"\',p1=\'"<<(int16_t)(PEvent->EvtParam1);
    std::cout<<"\',p2=\'"<<(int16_t)(PEvent->EvtParam2);
    std::cout<<"\',p3=\'"<<(int16_t)(PEvent->EvtParam3);
    std::cout<<"\',p4=\'"<<(int16_t)(PEvent->EvtParam4);
    std::cout<<"\';@SQL"<<std::endl;
}

TPlcDevice::TPlcDevice() {
    S7Server = new TSnap7Server(this);
    S7Server->SetEventsCallBack(srvCallback, &UserId);
    ModbusServer = new TModbusServer(this);
    ModbusServer->SetEventsCallBack(srvCallback, &UserId);
}

TPlcDevice::~TPlcDevice() {
    delete S7Server;
}

void TPlcDevice::AskForUserId() {
    std::cout<<"Please specify the honeypot ID:@0"<<std::endl;
    std::cin>>UserId;
}

void TPlcDevice::Start() {
    S7Server->Start();
    ModbusServer->Start();
}

void TPlcDevice::Stop() {
    S7Server->Stop();
    ModbusServer->Stop();
}
