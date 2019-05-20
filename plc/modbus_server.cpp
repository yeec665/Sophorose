#include "modbus_server.h"

void CopyBit(pbyte Src, longword SrcOff, pbyte Dst, longword DstOff) {
    if (Src[SrcOff >> 3] & (1 << (0x7 & SrcOff))) {
        Dst[DstOff >> 3] |= (1 << (0x7 & DstOff));
    } else {
        Dst[DstOff >> 3] &= ~(1 << (0x7 & DstOff));
    }
}

void CopyBits(pbyte Src, longword SrcOff, pbyte Dst, longword DstOff, longword Length) {
    for (longword i = 0; i < Length; i++) {
        longword SrcI = SrcOff + i;
        longword DstI = DstOff + i;
        if (Src[SrcI >> 3] & (1 << (0x7 & SrcI))) {
            Dst[DstI >> 3] |= (1 << (0x7 & DstI));
        } else {
            Dst[DstI >> 3] &= ~(1 << (0x7 & DstI));
        }
    }
}
//---------------------------------------------------------------------------
TModbusTcpWorker::TModbusTcpWorker() {
	RecvTimeout = 1000;
	RemotePort = ModbusTcpPort;
    LastModbusError = 0;
    DataSize = 0;
}
//---------------------------------------------------------------------------
TModbusTcpWorker::~TModbusTcpWorker() {}
//---------------------------------------------------------------------------
void TModbusTcpWorker::DoEvent(longword Code, word RetCode, word Param1, word Param2, word Param3, word Param4) {
    FServer->DoEvent(ClientHandle, Code, RetCode, Param1, Param2, Param3, Param4);
}

bool TModbusTcpWorker::Execute() {
    return ExecuteRecv();
}

bool TModbusTcpWorker::ExecuteRecv() {
    RecvPacket(&MbapHeader, sizeof(TMbapHeader));
    if (LastTcpError == 0) {
        if (CheckMbapHeader()) {
            return PerformFunction();
        } else {
            return false;
        }
    } else {
        return LastTcpError != WSAECONNRESET;
    }
}

void TModbusTcpWorker::SetResponseLength(int x) {
    MbapHeader.Length = SwapWord((word) (2 + x));
}

void TModbusTcpWorker::AppendBuffer(void* Src, int Len) {
    memcpy(Data + DataSize, Src, Len);
    DataSize += Len;
}

void TModbusTcpWorker::FillBuffer(u_char x, int Len) {
    int i;
    for (i = 0; i < Len; i++) {
        Data[DataSize + i] = x;
    }
    DataSize += Len;
}

void TModbusTcpWorker::SendBuffer() {
    SendPacket(Data, DataSize);
    DataSize = 0;
}

bool TModbusTcpWorker::CheckMbapHeader() {
    if (SwapWord(MbapHeader.ProtocolIdentifier) != ModbusProtocolIdentifier) {
        DoEvent(evcModbusPDUincoming, evrIllegalProtocolId, 0, 0, 0, 0);
        SendExceptionCode(0x01);
        return false;
    }
    RequestLength = (int) (0xFFFF & SwapWord(MbapHeader.Length)) - 2;
    if (RequestLength < 0) {
        DoEvent(evcModbusPDUincoming, evrShortPDU, 0, 0, 0, 0);
        SendExceptionCode(0x01);
        return false;
    }
    return true;
}

bool TModbusTcpWorker::PerformFunction() {
    switch (MbapHeader.FunctionCode) {
    case ModbusFunctionReadDiscreteInputs:
        return PerformReadDiscreteInputs();
    case ModbusFunctionReadCoils:
        return PerformReadCoils();
    case ModbusFunctionWriteSingleCoil:
        return PerformWriteSingleCoil();
    case ModbusFunctionWriteMultipleCoils:
        return PerformWriteMultipleCoils();
    case ModbusFunctionReadInputRegisters:
        return PerformReadInputRegisters();
    case ModbusFunctionReadHoldingRegisters:
        return PerformReadHoldingRegisters();
    case ModbusFunctionWriteSingleRegister:
        return PerformWriteSingleRegister();
    case ModbusFunctionWriteMultipleRegisters:
        return PerformWriteMultipleRegisters();
    case ModbusFunctionEncapsulatedTransport:
        return PerformEncapsulatedTransport();
    default:
        DoEvent(evcModbusPDUincoming, evrUnknownFunctionCode, MbapHeader.FunctionCode, 0, 0, 0);
        SendExceptionCode(0x01);
        return true;
    }
}

bool TModbusTcpWorker::PerformReadCoils() {
    TModbusReadCoilsRequest request;
    TModbusReadCoilsResponse response;
    if (RequestLength != sizeof(TModbusReadCoilsRequest)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x01);
        return false;
    }
    RecvPacket(&request, sizeof(TModbusReadCoilsRequest));
    int Offset = 0xFFFF & SwapWord(request.StartingAddress);
    int Length = 0xFFFF & SwapWord(request.QuantityOfCoils);
    if (Length < 1 || 2000 < Length || 0x10000 < Offset + Length) {
        DoEvent(evcModbusData, evrErrException, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x03);
        return true;
    }
    PS7Area OutputArea = FServer->FDevice->PA;
    if (OutputArea->Size < (Offset + Length) / 8) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x02);
        return true;
    }
    response.ByteCount = (Length + 7) / 8;
    SetResponseLength(sizeof(TModbusReadCoilsResponse) + response.ByteCount);
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&response, sizeof(TModbusReadCoilsResponse));
    Data[DataSize + response.ByteCount - 1] = 0x00;
    CopyBits(OutputArea->PData, Offset, Data, 8 * DataSize, Length);
    DataSize += response.ByteCount;
    DoEvent(evcModbusData, 0, MbapHeader.FunctionCode, Offset, Length, 0);
    SendBuffer();
    return true;
}

bool TModbusTcpWorker::PerformReadDiscreteInputs() {
    TModbusReadDiscreteInputsRequest request;
    TModbusReadDiscreteInputsResponse response;
    if (RequestLength != sizeof(TModbusReadDiscreteInputsRequest)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x01);
        return false;
    }
    RecvPacket(&request, sizeof(TModbusReadDiscreteInputsRequest));
    int Offset = 0xFFFF & SwapWord(request.StartingAddress);
    int Length = 0xFFFF & SwapWord(request.QuantityOfInputs);
    if (Length < 1 || 2000 < Length || 0x10000 < Offset + Length) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x03);
        return true;
    }
    PS7Area InputArea = FServer->FDevice->PE;
    if (InputArea->Size < (Offset + Length) / 8) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x02);
        return true;
    }
    response.ByteCount = (Length + 7) / 8;
    SetResponseLength(sizeof(TModbusReadDiscreteInputsResponse) + response.ByteCount);
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&response, sizeof(TModbusReadDiscreteInputsResponse));
    Data[DataSize + response.ByteCount - 1] = 0x00;
    CopyBits(InputArea->PData, Offset, Data, 8 * DataSize, Length);
    DataSize += response.ByteCount;
    DoEvent(evcModbusData, 0, MbapHeader.FunctionCode, Offset, Length, 0);
    SendBuffer();
    return true;
}

bool TModbusTcpWorker::PerformReadHoldingRegisters() {
    TModbusReadHoldingRegistersRequest request;
    TModbusReadHoldingRegistersResponse response;
    if (RequestLength != sizeof(TModbusReadHoldingRegistersRequest)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x01);
        return false;
    }
    RecvPacket(&request, sizeof(TModbusReadHoldingRegistersRequest));
    int Offset = 0xFFFF & SwapWord(request.StartingAddress);
    int Length = 0xFFFF & SwapWord(request.QuantityOfRegisters);
    if (Length < 1 || 125 < Length || 0x10000 < Offset + Length) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x03);
        return true;
    }
    PS7Area BitArea = FServer->FDevice->MK;
    if (BitArea->Size < 2 * (Offset + Length)) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x02);
        return true;
    }
    response.ByteCount = 2 * Length;
    SetResponseLength(sizeof(TModbusReadHoldingRegistersResponse) + response.ByteCount);
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&response, sizeof(TModbusReadHoldingRegistersResponse));
    memcpy(Data + DataSize, BitArea->PData + Offset, 2 * Length);
    DataSize += response.ByteCount;
    DoEvent(evcModbusData, 0, MbapHeader.FunctionCode, Offset, Length, 0);
    SendBuffer();
    return true;
}

bool TModbusTcpWorker::PerformReadInputRegisters() {
    TModbusReadInputRegistersRequest request;
    TModbusReadInputRegistersResponse response;
    if (RequestLength != sizeof(TModbusReadInputRegistersRequest)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x01);
        return false;
    }
    RecvPacket(&request, sizeof(TModbusReadInputRegistersRequest));
    int Offset = 0xFFFF & SwapWord(request.StartingAddress);
    int Length = 0xFFFF & SwapWord(request.QuantityOfInputRegisters);
    if (Length < 1 || 125 < Length || 0x10000 < Offset + Length) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x03);
        return true;
    }
    PS7Area InputArea = FServer->FDevice->PE;
    if (InputArea->Size < 2 * (Offset + Length)) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x02);
        return true;
    }
    response.ByteCount = 2 * Length;
    SetResponseLength(sizeof(TModbusReadInputRegistersResponse) + response.ByteCount);
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&response, sizeof(TModbusReadInputRegistersResponse));
    memcpy(Data + DataSize, InputArea->PData + Offset, 2 * Length);
    DataSize += response.ByteCount;
    DoEvent(evcModbusData, 0, MbapHeader.FunctionCode, Offset, Length, 0);
    SendBuffer();
    return true;
}

bool TModbusTcpWorker::PerformWriteSingleCoil() {
    TModbusWriteSingleCoilEcho echo;
    if (RequestLength != sizeof(TModbusWriteSingleCoilEcho)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x01);
        return false;
    }
    RecvPacket(&echo, sizeof(TModbusWriteSingleCoilEcho));
    int Address = 0xFFFF & SwapWord(echo.OutputAddress);
    int Value = 0xFFFF & SwapWord(echo.OutputValue);
    if (Value != 0x0000 && Value != 0xFF00) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Address, Value, 0);
        SetModbusError(0x03);
        return true;
    }
    PS7Area OutputArea = FServer->FDevice->PA;
    if (OutputArea->Size <= Address / 8) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Address, Value, 0);
        SetModbusError(0x02);
        return true;
    }
    CopyBit((pbyte) &Value, 0, OutputArea->PData, Address);
    SetResponseLength(sizeof(TModbusWriteSingleCoilEcho));
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&echo, sizeof(TModbusWriteSingleCoilEcho));
    DoEvent(evcModbusData, 0, MbapHeader.FunctionCode, Address, Value, 0);
    SendBuffer();
    return true;
}

bool TModbusTcpWorker::PerformWriteSingleRegister() {
    TModbusWriteSingleRegisterEcho echo;
    if (RequestLength != sizeof(TModbusWriteSingleRegisterEcho)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x01);
        return false;
    }
    RecvPacket(&echo, sizeof(TModbusWriteSingleRegisterEcho));
    int Address = 0xFFFF & SwapWord(echo.RegisterAddress);
    int Value = 0xFFFF & SwapWord(echo.RegisterValue);
    PS7Area BitArea = FServer->FDevice->MK;
    if (BitArea->Size <= 2 * Address) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Address, Value, 0);
        SetModbusError(0x02);
        return true;
    }
    memcpy(BitArea->PData, (pbyte) &Value, 2);
    SetResponseLength(sizeof(TModbusWriteSingleRegisterEcho));
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&echo, sizeof(TModbusWriteSingleRegisterEcho));
    DoEvent(evcModbusData, 0, MbapHeader.FunctionCode, Address, Value, 0);
    SendBuffer();
    return true;
}

bool TModbusTcpWorker::PerformWriteMultipleCoils() {
    TModbusWriteMultipleCoilsRequest request;
    TModbusWriteMultipleCoilsResponse response;
    if (RequestLength != sizeof(TModbusWriteMultipleCoilsRequest)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x01);
        return false;
    }
    RecvPacket(&request, sizeof(TModbusWriteMultipleCoilsRequest));
    RecvPacket(Data, request.ByteCount);
    int Offset = 0xFFFF & SwapWord(request.StartingAddress);
    int Length = 0xFFFF & SwapWord(request.QuantityOfOutputs);
    if (Length < 1 || 0x7B0 < Length || 0x10000 < Offset + Length || request.ByteCount < (Length + 7) / 8) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, request.ByteCount);
        SetModbusError(0x03);
        return true;
    }
    PS7Area OutputArea = FServer->FDevice->PA;
    if (OutputArea->Size < (Offset + Length) / 8) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x02);
        return true;
    }
    CopyBits(Data, 0, OutputArea->PData, Offset, Length);
    response.StartingAddress = request.StartingAddress;
    response.QuantityOfOutputs = request.QuantityOfOutputs;
    SetResponseLength(sizeof(TModbusWriteMultipleCoilsResponse));
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&response, sizeof(TModbusWriteMultipleCoilsResponse));
    DoEvent(evcModbusData, 0, MbapHeader.FunctionCode, Offset, Length, request.ByteCount);
    SendBuffer();
    return true;
}

bool TModbusTcpWorker::PerformWriteMultipleRegisters() {
    TModbusWriteMultipleRegistersRequest request;
    TModbusWriteMultipleRegistersResponse response;
    if (RequestLength != sizeof(TModbusWriteMultipleRegistersRequest)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x01);
        return false;
    }
    RecvPacket(&request, sizeof(TModbusWriteMultipleRegistersRequest));
    RecvPacket(Data, request.ByteCount);
    int Offset = 0xFFFF & SwapWord(request.StartingAddress);
    int Length = 0xFFFF & SwapWord(request.QuantityOfRegisters);
    if (Length < 1 || 0x7B < Length || 0x10000 < Offset + Length || request.ByteCount < 2 * Length) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, request.ByteCount);
        SetModbusError(0x03);
        return true;
    }
    PS7Area BitArea = FServer->FDevice->MK;
    if (BitArea->Size <= 2 * (Offset + Length)) {
        DoEvent(evcModbusData, evrErrOutOfRange, MbapHeader.FunctionCode, Offset, Length, 0);
        SetModbusError(0x02);
        return true;
    }
    memcpy(BitArea->PData + 2 * Offset, Data, 2 * Length);
    response.StartingAddress = request.StartingAddress;
    response.QuantityOfRegisters = request.QuantityOfRegisters;
    SetResponseLength(sizeof(TModbusWriteMultipleRegistersResponse));
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&response, sizeof(TModbusWriteMultipleRegistersResponse));
    DoEvent(evcModbusData, 0, MbapHeader.FunctionCode, Offset, Length, request.ByteCount);
    SendBuffer();
    return true;
}

bool TModbusTcpWorker::PerformEncapsulatedTransport() {
    TModbusReadDeviceIdRequest request;
    if (RequestLength != sizeof(TModbusReadDeviceIdRequest)) {
        DoEvent(evcModbusPDUincoming, evrWrongSizeData, MbapHeader.FunctionCode, 0, 0, 0);
        SetModbusError(0x02);
        return false;
    }
    RecvPacket(&request, sizeof(TModbusReadDeviceIdRequest));
    switch (request.MeiType) {
    case ModbusSubFunctionCanOpenGeneral:
        return PerformCanOpenGeneral(&request);
    case ModbusSubFunctionDeviceId:
        return PerformDeviceId(&request);
    default:
        DoEvent(evcModbusPDUincoming, evrUnknownSubFunctionCode, 0, 0, 0, 0);
        SendExceptionCode(0x01);
        return true;
    }
}

bool TModbusTcpWorker::PerformCanOpenGeneral(TModbusReadDeviceIdRequest* request) {
    DoEvent(evcModbusPDUincoming, evrNotImplemented, MbapHeader.FunctionCode, 0, 0, 0);
    SetModbusError(0x02);
    return true;
}

bool TModbusTcpWorker::PerformDeviceId(TModbusReadDeviceIdRequest* request) {
    TModbusReadDeviceIdResponse response;
    TModbusReadDeviceIdEntryResponse entryResponse;
    response.MeiType = request->MeiType;
    response.DeviceIdCode = request->DeviceIdCode;
    if (request->ObjectId < 0x03) {
        response.ConformityLevel = ModbusConformityLevelBasic;
    } else if (request->ObjectId < 0x80) {
        response.ConformityLevel = ModbusConformityLevelRegular;
    } else {
        response.ConformityLevel = ModbusConformityLevelExtended;
    }
    response.MoreFollows = 0x00;
    response.NumberOfObjects = 0x01;
    entryResponse.ObjectId = request->ObjectId;
    DoEvent(evcModbusDiagnostics, 0, MbapHeader.FunctionCode, request->MeiType, request->DeviceIdCode, request->ObjectId);
    switch (request->ObjectId) {
    case ModbusObjectIdVendorName:
        PerformSendEntry(&response, &entryResponse, (char*)"Original Siemens Equipment");
        break;
    case ModbusObjectIdProductCode:
        PerformSendEntry(&response, &entryResponse, (char*)"S C-C2UR29702012");
        break;
    case ModbusObjectIdMajorMinorRevision:
        PerformSendEntry(&response, &entryResponse, (char*)"S7 315-2 PN/DP");
        break;
    case ModbusObjectIdVendorUrl:
        PerformSendEntry(&response, &entryResponse, (char*)"https://new.siemens.com/");
        break;
    case ModbusObjectIdProductName:
        PerformSendEntry(&response, &entryResponse, (char*)"Siemens S7 PLC");
        break;
    case ModbusObjectIdModelName:
        PerformSendEntry(&response, &entryResponse, (char*)"S7 315-2 PN/DP");
        break;
    case ModbusObjectIdApplicationName:
        PerformSendEntry(&response, &entryResponse, (char*)"Sophorose");
        break;
    default:
        PerformSendEntry(&response, &entryResponse, (char*)"HHH");
        break;
    }
    return true;
}

void TModbusTcpWorker::PerformSendEntry(TModbusReadDeviceIdResponse* response, TModbusReadDeviceIdEntryResponse* entryResponse, char* objectValue) {
    entryResponse->ObjectLength = strlen(objectValue);
    SetResponseLength(sizeof(TModbusReadDeviceIdResponse) + sizeof(TModbusReadDeviceIdEntryResponse) + entryResponse->ObjectLength);
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(response, sizeof(TModbusReadDeviceIdResponse));
    AppendBuffer(entryResponse, sizeof(TModbusReadDeviceIdEntryResponse));
    AppendBuffer(objectValue, entryResponse->ObjectLength);
    SendBuffer();
}

void TModbusTcpWorker::SendExceptionCode(u_char x) {
    TModbusExceptionResponse response;
    SetResponseLength(sizeof(TModbusExceptionResponse));
    MbapHeader.FunctionCode |= 0x80;
    response.ExceptionCode = x;
    AppendBuffer(&MbapHeader, sizeof(TMbapHeader));
    AppendBuffer(&response, sizeof(TModbusExceptionResponse));
    SendBuffer();
}

int TModbusTcpWorker::SetModbusError(int Error) {
    LastModbusError = Error | LastTcpError;
	return LastModbusError;
}

TModbusServer::TModbusServer(TPlcMemory *Device) {
    FDevice = Device;
    LocalPort = ModbusTcpPort;
}

TModbusServer::~TModbusServer() {

}

PWorkerSocket TModbusServer::CreateWorkerSocket(socket_t Sock) {
    PWorkerSocket Result;
    Result = new TModbusTcpWorker();
    Result->SetSocket(Sock);
    PModbusTcpWorker(Result)->FServer = this;
    return Result;
}
