#include <iostream>
#include <stdlib.h>
#include <string.h>

#ifdef UNIX
#include <netdb.h>
static const int INVALID_SOCKET = -1;
#define CS close
#define S3 sleep(3)
#define GP getpid
#endif

#ifdef WIN32
#include <winsock.h>
#include <process.h>
#define CS closesocket
#define S3 Sleep(3000)
#define GP _getpid
#endif

using std::cout;
using std::cerr;
using std::endl;

static const int MAX_HOST = 256;

typedef signed char prio_t;
typedef int type_t;

// if this is increased, the syntax of binary data has to be changed, too !!!
// currently we use 3 bits for number of channels
static const int MAX_FIELDS = 8;

static const prio_t PRIO_DEFAULT = 0;
static const prio_t PRIO_LOWEST = -99;
static const prio_t PRIO_HIGHEST = 99;
static const time_t TIME_NOTIME = -1;

static const int FLAG_PRIORITY = 0x01;
static const int FLAG_TIMESTAMP = 0x02;
static const int FLAG_FLAG_RESERVED_2 = 0x04;
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

int g_socket = -1;
char g_host[MAX_HOST];
int g_port = 19876;

int g_fieldCount = 8;
int g_blockCount = 10;
type_t g_fieldType[MAX_FIELDS];

bool connect() {
	if (g_socket != INVALID_SOCKET)
		CS(g_socket);
	g_socket = INVALID_SOCKET;

	struct hostent *he = gethostbyname(g_host);
	if (!he) {
		cerr << "Cannot resolve host name: " << g_host << endl;
		return false;
	}

	int lsd = socket(PF_INET, SOCK_STREAM, 0);
	if (lsd == INVALID_SOCKET) {
		cerr << "Cannot create socket" << endl;
		return false;
	}

	struct sockaddr_in sa;
	memset(&sa, 0, sizeof(sa));
	sa.sin_family = AF_INET;
	sa.sin_port = htons(g_port);
	memcpy(&sa.sin_addr, he->h_addr_list[0], he->h_length);
	if (connect(lsd, reinterpret_cast<struct sockaddr*> (&sa), sizeof(sa))) {
		cerr << "Cannot connect to " << g_host << " at port " << g_port << endl;
		return false;
	}

	g_socket = lsd;
	return true;
}

bool safeSend(const void *data, int len) {
	for (int i = 0; i < 10; ++i) {
		if (send(g_socket, static_cast<const char*> (data), len, 0) == len)
			return true;
		cout << "Could not send data - retrying" << endl;
		S3;
		if (!connect())
			return false;
	}
	cerr << "Cannot write to socket within 30 seconds" << endl;
	return false;
}

bool sendDataHeader(prio_t prio = PRIO_DEFAULT, time_t time = TIME_NOTIME) {
	if (g_fieldCount < 1 || g_fieldCount > MAX_FIELDS) {
		cerr << "Invalid number of fields: " << g_fieldCount << endl;
		return false;
	}

	unsigned int flags = 0;
	if (prio != PRIO_DEFAULT)
		flags |= FLAG_PRIORITY;
	if (time != TIME_NOTIME)
		flags |= FLAG_TIMESTAMP;
	unsigned int header = (flags & 0x1F) << 27 | ((g_fieldCount - 1) & 0x07)
	        << 24;
	for (int i = 0; i < g_fieldCount; ++i)
		header |= (g_fieldType[i] & 0x07) << i * 3;
	header = htonl(header);

	// send header
	if (!safeSend(&header, 4)) // no sizeof here
		return false;
	if (prio != PRIO_DEFAULT) {
		if (prio < PRIO_LOWEST || prio > PRIO_HIGHEST) {
			cerr << "Invalid priority: " << prio << endl;
			return false;
		}
		if (!safeSend(&prio, 1))
			return false;
	}
	if (time != TIME_NOTIME) {
		if (time < 0) {
			cerr << "Invalid time: " << time << endl;
			return false;
		}
		unsigned int timeUI = htonl(time);
		if (!safeSend(&timeUI, 4))
			return false;
	}

	return true;
}

static inline long long htonll(long long l) {
	return (static_cast<long long> (htonl(static_cast<int> (l))) << 32)
	        | htonl(static_cast<int> (l >> 32));
}

bool sendDataField(type_t type, long long data) {
	switch (type) {
	case TYPE_INT8: {
		signed char i = static_cast<signed char> (data);
		return safeSend(&i, 1);
	}
	case TYPE_INT32: {
		int i = static_cast<int> (data);
		return safeSend(&i, 4);
	}
	case TYPE_INT64: {
		return safeSend(&data, 8);
	}
	default:
		cerr << "Invalid data type for int values: " << type << endl;
		return false;
	}
}

bool sendDataField(type_t type, double data) {
	switch (type) {
	case TYPE_FLOAT32: {
		float f = static_cast<float> (data);
		int i = htonl(*(static_cast<int *> (static_cast<void *> (&f))));
		return safeSend(&i, 4);
	}
	case TYPE_FLOAT64: {
		long long i = htonll(
		        *(static_cast<long long *> (static_cast<void *> (&data))));
		return safeSend(&i, 8);
	}
	default:
		cerr << "Invalid data type for float values: " << type << endl;
		return false;
	}
}

bool sendDataField(type_t type, const char *data) {
	switch (type) {
	case TYPE_NTSTRING: {
		unsigned char b0 = 0;
		return safeSend(data, strlen(data)) && safeSend(&b0, 1);
	}
	case TYPE_UTFSTRING: {
		unsigned short len = htons(static_cast<unsigned short> (strlen(data)));
		if (!safeSend(&len, 2))
			return false;
		const char *pc = data;
		unsigned char c;
		while ((c = *pc++ )) {
			if (c & 0x80) {
				// need special handling if highest bit is set
				unsigned char c0 = 0xC0 | ((c >> 6) & 0x1F);
				unsigned char c1 = static_cast<unsigned char> (0x80
				        | (c & 0x3F));
				if (!safeSend(&c0, 1) || !safeSend(&c1, 1)) //TODO send c1 before c0?
					return false;
			} else if (!safeSend(&c, 1))
				return false;
		}
		return true;
	}
	default:
		cerr << "Invalid data type for string values: " << type << endl;
		return false;
	}
}

bool sendDataField(type_t type, const unsigned char *data, int len) {
	switch (type) {
	case TYPE_DATA: {
		int lenNBO = htonl(len);
		return safeSend(&lenNBO, 4) && safeSend(data, len);
	}
	default:
		cerr << "Invalid data type for binary values: " << type << endl;
		return false;
	}
}

int main(int argc, char **argv) {

	strcpy(g_host, "localhost");
	for (int i = 0; i < g_fieldCount; ++i)
		g_fieldType[i] = 0;

	if (argc == 1 || !strcmp(argv[1], "-?")) {
		cerr << "Syntax: " << argv[0] << " [-h <Host>] [-p <Port>] "
			"[-b <BlockCount>] [-f <FieldCount>] [-t <fieldIndex> <fieldType>]"
		        << endl;
		cerr << "Host defaults to " << g_host << endl;
		cerr << "Port defaults to " << g_port << endl;
		cerr << "BlockCount defaults to " << g_blockCount << endl;
		cerr << "FieldCount defaults to " << g_fieldCount << endl;
		cerr << "FieldType defaults to " << g_fieldType[0] << " for each index"
		        << endl;
		cerr << "FieldIndex may be \"all\"" << endl;
		cerr << "Valid FieldTypes: 0 - 7 for [INT8, INT32, INT64, "
			"FLOAT32, FLOAT64, NTSTR, UTF, DATA]" << endl;
		cerr << "Use \"rand\" for random value" << endl;
		return 2;
	}

	srand(time(NULL) + clock() + GP());

	for (int i = 1; i < argc; ++i) {
		if (!strcmp(argv[i], "-p")) {
			char *arg = argv[++i];
			g_port = atoi(arg);
			if (g_port < 1 || g_port > 65535) {
				cerr << "Invalid port number: " << arg << endl;
				return 2;
			}
		} else if (!strcmp(argv[i], "-h")) {
			strncpy(g_host, argv[++i], MAX_HOST);
			g_host[MAX_HOST - 1] = 0;
		} else if (!strcmp(argv[i], "-b")) {
			char *arg = argv[++i];
			g_blockCount = strcmp(arg, "rand") ? atoi(arg) : 1 + rand()
			        / (RAND_MAX / 1000);
			if (g_blockCount < 1) {
				cerr << "Invalid block count: " << arg << endl;
				return 2;
			}
		} else if (!strcmp(argv[i], "-f")) {
			char *arg = argv[++i];
			g_fieldCount = strcmp(arg, "rand") ? atoi(arg) : 1 + rand()
			        / (RAND_MAX / MAX_FIELDS);
			if (g_fieldCount < 1 || g_fieldCount > MAX_FIELDS) {
				cerr << "Invalid field count: " << arg << endl;
				return 2;
			}
		} else if (!strcmp(argv[i], "-t")) {
			char *arg1 = argv[++i];
			char *arg2 = argv[++i];
			int idx = atoi(arg1);
			int type = atoi(arg2);
			bool all = !strcmp(arg1, "all");
			bool rnd = !strcmp(arg2, "rand");
			if (!all && (idx < 1 || idx > MAX_FIELDS)) {
				cerr << "Invalid field index: " << arg1 << endl;
				return 2;
			}
			if (!rnd && (type < 0 || type > 7)) {
				cerr << "Invalid field type: " << arg2 << endl;
				return 2;
			}
			if (all)
				for (int j = 0; j < MAX_FIELDS; ++j)
					g_fieldType[j] = rnd ? rand() / (RAND_MAX / 8) : type;
			else
				g_fieldType[idx - 1] = rnd ? rand() / (RAND_MAX / 8) : type;

		} else {
			cerr << "Invalid switch: " << argv[i] << endl;
			return 2;
		}
	}

	cout << "Using " << g_blockCount << " data blocks with fields: ";
	for (int i = 0; i < g_fieldCount; ++i)
		switch (g_fieldType[i]) {
		case TYPE_INT8:
			cout << "INT8 ";
			break;
		case TYPE_INT32:
			cout << "INT32 ";
			break;
		case TYPE_INT64:
			cout << "INT64 ";
			break;
		case TYPE_FLOAT32:
			cout << "FLOAT32 ";
			break;
		case TYPE_FLOAT64:
			cout << "FLOAT64 ";
			break;
		case TYPE_NTSTRING:
			cout << "NTSTR ";
			break;
		case TYPE_UTFSTRING:
			cout << "UTF ";
			break;
		case TYPE_DATA:
			cout << "DATA ";
			break;
		default:
			cout << "??? ";
			break;
		}
	cout << endl;

	if (!connect())
		return 1;

	time_t lastTime = time(NULL);
	int cnt = g_blockCount + 1;
	int lastCnt = cnt;
	while (--cnt) {
		if (!sendDataHeader())
			return 1;

		for (int i = 0; i < g_fieldCount; ++i) {
			switch (g_fieldType[i]) {
			case TYPE_INT8:
			case TYPE_INT32:
			case TYPE_INT64: {
				long long l = rand() - RAND_MAX / 2;
				if (!sendDataField(g_fieldType[i], l))
					return 1;
				break;
			}
			case TYPE_FLOAT32:
			case TYPE_FLOAT64: {
				double d = static_cast<double> ((rand() - RAND_MAX / 2))
				        / rand();
				if (!sendDataField(g_fieldType[i], d))
					return 1;
				break;
			}
			case TYPE_NTSTRING:
			case TYPE_UTFSTRING: {
				int len = rand() / (RAND_MAX / 1000);
				char buf[len + 1];
				for (int j = 0; j < len; ++j)
					buf[j] = static_cast<char> (32 + rand() / (RAND_MAX / (127
					        - 32)));
				buf[len] = 0;
				if (!sendDataField(g_fieldType[i], buf))
					return 1;
				break;
			}
			case TYPE_DATA: {
				int len = rand() / (RAND_MAX / 1000);
				unsigned char buf[len];
				for (int j = 0; j < len; ++j)
					buf[j] = static_cast<unsigned char> (rand() / (RAND_MAX
					        / 256));
				if (!sendDataField(g_fieldType[i], buf, len))
					return 1;
				break;
			}
			default:
				cerr << "Invalid field type: " << g_fieldType[i] << endl;
				return 1;
			}
		}

		time_t now = time(NULL);
		if (now > lastTime || cnt == 1) {
			cout << "Written " << (lastCnt - cnt) << " block(s) in " << (now
			        - lastTime) << " second(s)" << endl;
			lastTime = now;
			lastCnt = cnt;
		}
	}

	CS(g_socket);
	return 0;

	// Results:
	// ConsoleOut: 11.000 blocks per second, PCMD not responding
}

