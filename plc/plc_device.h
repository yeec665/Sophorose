#ifndef plc_device_h
#define plc_device_h

#include "iostream"
#include "snap_sysutils.h"
#include "win_threads.h"
#include "snap_msgsock.h"

class TS7Area {
public:
	word Number;
	word Size;
	pbyte PData;
	PSnapCriticalSection cs;
	TS7Area(word aNumber, word aSize);
	~TS7Area();
};

typedef TS7Area *PS7Area;

class TS7AreaContainer {
public:
    PS7Area *PList;
    size_t AreaCount;
    size_t MaxIndex;
    size_t Capacity;
    TS7AreaContainer(size_t aCapacity);
    ~TS7AreaContainer();
    PS7Area Get(word Index);
    PS7Area Find(word Number);
    int FindFirstFree();
    int IndexOf(word Number);
    int Add(PS7Area NewArea);
    int Remove(word Number);
};

typedef TS7AreaContainer *PS7AreaContainer;

class TPlcMemory {
public:
    PS7Area PE, PA, MK, CT, TM;
    PS7AreaContainer DB, OB, FB, FC, SDB;
    TPlcMemory();
    ~TPlcMemory();
    PS7AreaContainer getArea(byte blkType);
    void fillBlockHead(PS7AreaContainer BP);
};

#include "s7_server.h"
#include "modbus_server.h"

longword SwapDWord(longword x);
void S7API srvCallback(void * UserPtr, PSrvEvent PEvent, int Size);

class TPlcDevice : public TPlcMemory {
private:
    int32_t UserId;
public:
    TCustomMsgServer *S7Server;
    TCustomMsgServer *ModbusServer;
    TPlcDevice();
    ~TPlcDevice();
    void AskForUserId();
    void Start();
    void Stop();
};
typedef TPlcDevice *PPlcDevice;

#endif // plc_device_h
