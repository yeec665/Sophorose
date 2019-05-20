#ifndef modbus_server_h
#define modbus_server_h
//---------------------------------------------------------------------------
#include "snap_msgsock.h"
#include "snap_tcpsrvr.h"
#include "s7_types.h"
#include "plc_device.h"
//---------------------------------------------------------------------------
#pragma pack(1)

#define ModbusProtocolIdentifier 0
#define ModbusTcpPort    		 502

const longword errModbusMask    	        = 0x000F0000;

const longword errModbusConnect             = 0x00010000;
const longword errModbusDisconnect          = 0x00020000;
const longword errModbusInvalidPDU          = 0x00030000;
const longword errModbusInvalidDataSize     = 0x00040000;
const longword errModbusNullPointer    	    = 0x00050000;
const longword errModbusShortPacket    	    = 0x00060000;
const longword errModbusTooManyFragments    = 0x00070000;
const longword errModbusPduOverflow    	    = 0x00080000;
const longword errModbusSendPacket          = 0x00090000;
const longword errModbusRecvPacket          = 0x000A0000;
const longword errModbusInvalidParams    	= 0x000B0000;

const u_char ModbusFunctionReadDiscreteInputs     = 0x02;
const u_char ModbusFunctionReadCoils              = 0x01;
const u_char ModbusFunctionWriteSingleCoil        = 0x05;
const u_char ModbusFunctionWriteMultipleCoils     = 0x0F;
const u_char ModbusFunctionReadInputRegisters     = 0x04;
const u_char ModbusFunctionReadHoldingRegisters   = 0x03;
const u_char ModbusFunctionWriteSingleRegister    = 0x06;
const u_char ModbusFunctionWriteMultipleRegisters = 0x10;
const u_char ModbusFunctionReadWriteRegisters     = 0x17;
const u_char ModbusFunctionMaskWriteRegisters     = 0x16;
const u_char ModbusFunctionReadFifoQueue          = 0x18;
const u_char ModbusFunctionReadFileRecord         = 0x14;
const u_char ModbusFunctionWriteFileRecord        = 0x15;
const u_char ModbusFunctionEncapsulatedTransport  = 0x2B;

const u_char ModbusSubFunctionCanOpenGeneral    = 0x0D;
const u_char ModbusSubFunctionDeviceId          = 0x0E;

const u_char ModbusObjectIdVendorName  = 0x00;
const u_char ModbusObjectIdProductCode  = 0x01;
const u_char ModbusObjectIdMajorMinorRevision = 0x02;
const u_char ModbusObjectIdVendorUrl = 0x03;
const u_char ModbusObjectIdProductName = 0x04;
const u_char ModbusObjectIdModelName = 0x05;
const u_char ModbusObjectIdApplicationName = 0x06;
const u_char ModbusConformityLevelBasic = 0x81;
const u_char ModbusConformityLevelRegular = 0x82;
const u_char ModbusConformityLevelExtended = 0x83;

typedef struct {
	u_short TransactionIdentifier;
	u_short ProtocolIdentifier;
	u_short Length;
	u_char UnitIdentifier;
	u_char FunctionCode;
} TMbapHeader;

typedef struct {
    u_short StartingAddress;
    u_short QuantityOfCoils;
} TModbusReadCoilsRequest;

typedef struct {
    u_char ByteCount;
} TModbusReadCoilsResponse;

typedef struct {
    u_short StartingAddress;
    u_short QuantityOfInputs;
} TModbusReadDiscreteInputsRequest;

typedef struct {
    u_char ByteCount;
} TModbusReadDiscreteInputsResponse;

typedef struct {
    u_short StartingAddress;
    u_short QuantityOfRegisters;
} TModbusReadHoldingRegistersRequest;

typedef struct {
    u_char ByteCount;
} TModbusReadHoldingRegistersResponse;

typedef struct {
    u_short StartingAddress;
    u_short QuantityOfInputRegisters;
} TModbusReadInputRegistersRequest;

typedef struct {
    u_char ByteCount;
} TModbusReadInputRegistersResponse;

typedef struct {
    u_short OutputAddress;
    u_short OutputValue;
} TModbusWriteSingleCoilEcho;

typedef struct {
    u_short RegisterAddress;
    u_short RegisterValue;
} TModbusWriteSingleRegisterEcho;

typedef struct {
    u_short StartingAddress;
    u_short QuantityOfOutputs;
    u_char ByteCount;
} TModbusWriteMultipleCoilsRequest;

typedef struct {
    u_short StartingAddress;
    u_short QuantityOfOutputs;
} TModbusWriteMultipleCoilsResponse;

typedef struct {
    u_short StartingAddress;
    u_short QuantityOfRegisters;
    u_char ByteCount;
} TModbusWriteMultipleRegistersRequest;

typedef struct {
    u_short StartingAddress;
    u_short QuantityOfRegisters;
} TModbusWriteMultipleRegistersResponse;

typedef struct {
    u_char MeiType;
    u_char DeviceIdCode;
    u_char ObjectId;
} TModbusReadDeviceIdRequest;

typedef struct {
    u_char MeiType;
    u_char DeviceIdCode;
    u_char ConformityLevel;
    u_char MoreFollows;
    u_char NextObjectId;
    u_char NumberOfObjects;
} TModbusReadDeviceIdResponse;

typedef struct {
    u_char ObjectId;
    u_char ObjectLength;
} TModbusReadDeviceIdEntryResponse;

typedef struct {
    u_char ExceptionCode;
} TModbusExceptionResponse;

class TModbusServer; // forward declaration

class TModbusTcpWorker : public TMsgSocket {
private:
	TMbapHeader MbapHeader;
	u_char Data[1024];
	int DataSize;
protected:
    int RequestLength;
    void DoEvent(longword Code, word RetCode, word Param1, word Param2, word Param3, word Param4);
    bool Execute();
    bool ExecuteRecv();
    void SetResponseLength(int x);
    void AppendBuffer(void* Src, int Len);
    void FillBuffer(u_char x, int Len);
    void SendBuffer();
    bool CheckMbapHeader();
    bool PerformFunction();
    bool PerformReadCoils();
    bool PerformReadDiscreteInputs();
    bool PerformReadHoldingRegisters();
    bool PerformReadInputRegisters();
    bool PerformWriteSingleCoil();
    bool PerformWriteSingleRegister();
    bool PerformWriteMultipleCoils();
    bool PerformWriteMultipleRegisters();
    bool PerformEncapsulatedTransport();
    bool PerformCanOpenGeneral(TModbusReadDeviceIdRequest* request);
    bool PerformDeviceId(TModbusReadDeviceIdRequest* request);
    void PerformSendEntry(TModbusReadDeviceIdResponse* response, TModbusReadDeviceIdEntryResponse* entryResponse, char* objectValue);
    void SendExceptionCode(u_char x);
    int SetModbusError(int Error);
public:
    TModbusServer* FServer;
	int LastModbusError;
	TModbusTcpWorker();
	~TModbusTcpWorker();
};

typedef TModbusTcpWorker* PModbusTcpWorker;

class TModbusServer : public TCustomMsgServer {
protected:
    PWorkerSocket CreateWorkerSocket(socket_t Sock);
public:
    TPlcMemory *FDevice;
    TModbusServer(TPlcMemory *Device);
    ~TModbusServer();
    friend class TModbusTcpWorker;
};

typedef TModbusServer* PModbusServer;

#endif // modbus_server_h
