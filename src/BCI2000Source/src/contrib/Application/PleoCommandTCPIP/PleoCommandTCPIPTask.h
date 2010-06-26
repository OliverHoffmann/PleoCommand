#ifndef PleoCommandTCPIPTaskH
#define PleoCommandTCPIPTaskH

#include "ApplicationBase.h"
#include <winsock.h>

typedef signed char prio_t;
typedef signed long long time_t;
typedef int type_t;

// if this is increased, the syntax of binary data has to be changed, too !!!
// currently we use 3 bits for number of channels, with 2 additional possible
static const int MAX_FIELDS = 32;

static const prio_t PRIO_DEFAULT = 0;
static const prio_t PRIO_LOWEST = -99;
static const prio_t PRIO_HIGHEST = 99;
static const time_t TIME_NOTIME = -1;

static const int FLAG_PRIORITY = 0x01;
static const int FLAG_TIMESTAMP = 0x02;
static const int FLAG_VERYLONG = 0x04;
static const int FLAG_FLAG_RESERVED_3 = 0x08;
static const int FLAG_FLAG_RESERVED_4 = 0x10;

static const int TYPE_INT8 = 0x00;
static const int TYPE_INT32 = 0x01;
static const int TYPE_INT64 = 0x02;
static const int TYPE_FLOAT32 = 0x03;
static const int TYPE_FLOAT64 = 0x04;
static const int TYPE_UTFSTRING = 0x05;
static const int TYPE_NTSTRING = 0x06;
static const int TYPE_DATA = 0x07;

struct Field {
	type_t type;
	int channel;
	union {
		long long fixedInt; // for TYPE_INT8, TYPE_INT32 and TYPE_INT64
		double fixedFloat; // for TYPE_FLOAT32 and TYPE_FLOAT64
		char *fixedString; // for TYPE_UTFSTRING and TYPE_NTSTRING
		struct { // for TYPE_DATA
			int fixedDataLen;
			unsigned char *fixedData;
		};
	};
};

class PleoCommandTCPIPTask: public ApplicationBase {
public:
	PleoCommandTCPIPTask();
	virtual ~PleoCommandTCPIPTask();
	virtual void
	Preflight(const SignalProperties& Input, SignalProperties& Output) const;
	virtual void Initialize(const SignalProperties& Input,
	        const SignalProperties& Output);
	virtual void Process(const GenericSignal& Input, GenericSignal& Output);
	virtual void Halt();

private:
	void CloseSocket();
	bool Connect();
	bool SafeSend(const void *data, int len, bool reconnect = false);
	bool SendDataHeader(int fieldCount, prio_t prio = PRIO_DEFAULT,
	        time_t time = TIME_NOTIME);
	bool SendDataField(type_t type, double data);
	bool SendDataField(type_t type, long long data);
	bool SendDataField(type_t type, const char *data);
	bool SendDataField(type_t type, const unsigned char *data, int len);

	AnsiString m_host;
	int m_port;
	int m_fieldCount;
	Field m_fields[MAX_FIELDS];
	SOCKET m_socket;
};

#endif
