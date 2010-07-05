#include "PCHIncludes.h"
#pragma hdrstop
#include "PleoCommandTCPIPTask.h"
#include <stdio.h>
RegisterFilter(PleoCommandTCPIPTask, 3);

using std::endl;

static unsigned char HEXTOCHAR[] = { // hexadecimal converter table
        0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x00 - 0x07
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x08 - 0x0F
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x10 - 0x17
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x18 - 0x1F
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x20 - 0x27
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x28 - 0x2F
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, // 0x30 - 0x37
                0x08, 0x09, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x38 - 0x3F
                0xFF, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0xFF, // 0x40 - 0x47
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x48 - 0x4F
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x50 - 0x57
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x58 - 0x5F
                0xFF, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0xFF, // 0x60 - 0x67
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x68 - 0x6F
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x70 - 0x77
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x78 - 0x7F
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x80 - 0x87
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x88 - 0x8F
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x90 - 0x97
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x98 - 0x9F
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xA0 - 0xA7
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xA8 - 0xAF
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xB0 - 0xB7
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xB8 - 0xBF
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xC0 - 0xC7
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xC8 - 0xCF
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xD0 - 0xD7
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xD8 - 0xDF
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xE0 - 0xE7
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xE8 - 0xEF
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xF0 - 0xF7
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0xF8 - 0xFF
        };

#define printErr(msg)													\
	bcierr << msg << ": " << strerror(WSAGetLastError()) << "["         \
			<< WSAGetLastError() << "]" << endl

#define printOut(msg)													\
	bciout << msg << ": " << strerror(WSAGetLastError()) << "["         \
			<< WSAGetLastError() << "]" << endl

// atoll() not defined for Borland Compiler
#define atoll(str)	strtoll(str, NULL, 10)

PleoCommandTCPIPTask::PleoCommandTCPIPTask() :
	m_host("localhost"), m_port(19876), m_fieldCount(0), m_socket(
	        INVALID_SOCKET) {
	WSADATA wd;
	if (WSAStartup(MAKEWORD(2, 0), &wd))
		printErr("Failed to startup WSA");

	// 1163 1130
	char matrix[130 * MAX_FIELDS + 150];
	// (5 + 116 + 4 + 2) * MAX_FIELDS + 73 + 2 + 6 + 65 + 1
	matrix[0] = 0;
	strcat(matrix, "Application matrix Fields= "
		"[ DataType a)Channel%20Nr b)Fixed%20Value ] [ ");
	for (int i = 1; i <= MAX_FIELDS; ++i) {
		char buf[6];
		snprintf(buf, 6, "#%d ", i);
		strcat(matrix, buf);
	}
	strcat(matrix, "] ");
	for (int i = 0; i < MAX_FIELDS; ++i)
		strcat(matrix, "{ matrix [ int8 int32 int64 float32 float64 utfString "
			"ansiString hexData <omitted> ] [ Choose ] % % % % x % % % % }");
	for (int i = 0; i < MAX_FIELDS; ++i) {
		char buf[5];
		snprintf(buf, 5, "%d ", i);
		strcat(matrix, buf);
	}
	for (int i = 0; i < MAX_FIELDS; ++i)
		strcat(matrix, "% ");
	strcat(matrix, "% % % // Type and number of channel or "
		"fixed value for each field");
	bcidbg << "matrix size: " << strlen(matrix) << " of " << sizeof(matrix)
	        << endl;

	BEGIN_PARAMETER_DEFINITIONS
		"Application String Host= localhost localhost"
		"// Name or IP address of target computer",
		"Application int Port= 19876 19876 1 65535"
		"// Port to connect to on target computer",
		matrix,
	END_PARAMETER_DEFINITIONS
}

PleoCommandTCPIPTask::~PleoCommandTCPIPTask() {
	Halt();
	WSACleanup();
}

static long long strToInt(const char *str, const char *msg, int idx) {
	char *endptr;
	long long i = strtoll(str, &endptr, 10);
	if (*endptr)
		bcierr << msg << " for field " << (idx + 1) << ": " << str << endl;
	return i;
}

static double strToFloat(const char *str, const char *msg, int idx) {
	char *endptr;
	double i = strtod(str, &endptr);
	if (*endptr)
		bcierr << msg << " for field " << (idx + 1) << ": " << str << endl;
	return i;
}

static void outOfRange(const char *type, int idx) {
	bcierr << "Fixed value out of range for " << type << " of field " << (idx
	        + 1);
}

void PleoCommandTCPIPTask::Preflight(
        const SignalProperties& inSignalProperties,
        SignalProperties& outSignalProperties) const {
	outSignalProperties = inSignalProperties;

	bool afterEnd = false;
	if (Parameter("Fields")->NumColumns() == 0
	        || Parameter("Fields")->NumRows() != 3) {
		bcierr << "Parameter \"Fields\" must contain a n x 3 matrix" << endl;
		return;
	}
	int cols = Parameter("Fields")->NumColumns();
	for (int i = 0; i < cols; ++i) {
		if (i >= MAX_FIELDS) {
			bcierr << "Too many columns in matrix: " << cols << ", but only "
			        << MAX_FIELDS << " supported" << endl;
			break;
		}

		// check data type
		ParamRef typeMatrix = Parameter("Fields")(0, i);
		if (typeMatrix->NumColumns() != 1 || typeMatrix->NumRows() != 9) {
			bcierr << "Cell " << i << ",0 must contain a 1 x 9 matrix" << endl;
			continue; // skip this entry
		}
		int marked = -1;
		for (int j = 0; j < 9; ++j) {
			const char *mark = typeMatrix(j, 0).c_str();
			if (!strcmp(mark, "x")) {
				if (marked != -1)
					bcierr << "More than one mark for data-type of field "
					        << (i + 1) << ": Marks at rows " << marked
					        << " and " << j << endl;
				marked = j;
			} else if (strlen(mark))
				bcierr << "Invalid mark for data-type of field " << (i + 1)
				        << " at row " << j << ": " << mark << endl;
		}
		if (marked == -1)
			bcierr << "No mark for data-type of field " << (i + 1) << endl;
		else if (marked == 8)
			afterEnd = true;
		else if (afterEnd)
			bcierr << "Bad data-type of field " << (i + 1) << ": "
			        << "Only <omitted> allowed after first <omitted>" << endl;

		// check if exactly one of a) and b) is defined
		const char *chnr = Parameter("Fields")(1, i).c_str();
		const char* fixed = Parameter("Fields")(2, i).c_str();
		if (strlen(chnr) > 0 && strlen(fixed) > 0) {
			bcierr << "Need channel number or a fixed value for field " << (i
			        + 1) << ", not both" << endl;
		}
		if (afterEnd) {
			// if field is omitted, both should be empty
			if (strlen(chnr) > 0)
				bciout << "Channel number for omitted field " << (i + 1)
				        << " will not be used" << endl;
			if (strlen(fixed) > 0)
				bciout << "Fixed value for omitted field " << (i + 1)
				        << " will not be used" << endl;
		} else if (strlen(chnr) == 0 && strlen(fixed) == 0) {
			// if field is used, one should be set
			bcierr << "Need channel number or a fixed value for field " << (i
			        + 1) << endl;
		}

		// check fixed value
		if (strlen(fixed) > 0) {
			switch (marked) {
			case 0: {
				long long v = strToInt(fixed, "Invalid integer in fixed value",
				        i);
				if (v < CHAR_MIN || v > CHAR_MAX)
					outOfRange("INT8", i);
				break;
			}
			case 1: {
				long long v = strToInt(fixed, "Invalid integer in fixed value",
				        i);
				if (v < INT_MIN || v > INT_MAX)
					outOfRange("INT32", i);
				break;
			}
			case 2:
				strToInt(fixed, "Invalid integer in fixed value", i);
				break;
			case 3:
			case 4:
				strToFloat(fixed, "Invalid float in fixed value", i);
				break;
			case 7: {
				const char *pc = fixed;
				char c;
				int cnt = 0;
				bool err = false;
				while ((c = *pc++ )) {
					if (HEXTOCHAR[c] == 0xFF) {
						bcierr
						        << "Invalid hex-string in fixed value for field "
						        << (i + 1) << ": Character " << c << " ("
						        << (int) c << ") is invalid";
						err = true;
						break;
					}
					++cnt;
				}
				if (!err && cnt % 2)
					bcierr << "Invalid hex-string in fixed value for field "
					        << (i + 1) << ": Length is not a multiple of two: "
					        << cnt;
				break;
			}
			}
		}

		// check channel number
		if (strlen(chnr) > 0) {
			long long ch = strToInt(chnr, "Invalid channel number", i);
			if (ch < 0 || ch >= inSignalProperties.Channels())
				bciout << "channel number " << ch << " out of range for field "
				        << (i + 1) << ": " << ", not between 0 and "
				        << (inSignalProperties.Channels() - 1) << endl;
		}
	}

	const char *pcHost = Parameter("Host").c_str();
	struct hostent *he = gethostbyname(pcHost);
	if (!he)
		bcierr << "Cannot resolve host name: " << pcHost << endl;
}

void PleoCommandTCPIPTask::Initialize(const SignalProperties&,
        const SignalProperties&) {

	memset(m_fields, 0, sizeof(m_fields));
	m_fieldCount = 0;
	int cols = Parameter("Fields")->NumColumns();
	for (int i = 0; i < cols && i < MAX_FIELDS; ++i) {
		Field *field = &m_fields[i];

		// get data type
		ParamRef typeMatrix = Parameter("Fields")(0, i);
		int marked = -1;
		for (int j = 0; j < 9; ++j) {
			if (!strcmp(typeMatrix(j, 0).c_str(), "x")) {
				marked = j;
				break;
			}
		}
		if (marked == -1 || marked == 8)
			break;
		m_fieldCount = i + 1;
		field->type = marked;

		// get channel
		const char *chnr = Parameter("Fields")(1, i).c_str();
		field->channel = strlen(chnr) ? atol(chnr) : -1;

		// get fixed value
		const char* fixed = Parameter("Fields")(2, i).c_str();
		if (strlen(fixed) > 0) {
			switch (marked) {
			case 0:
			case 1:
			case 2:
				field->fixedInt = atoll(fixed);
				break;
			case 3:
			case 4:
				field->fixedFloat = atof(fixed);
				break;
			case 5:
			case 6:
				field->fixedString = strdup(fixed);
				break;
			case 7: {
				int cnt = strlen(fixed) + 1;
				field->fixedDataLen = cnt / 2;
				field->fixedData = (unsigned char*) malloc(field->fixedDataLen);
				unsigned char *fd = field->fixedData;
				const char *pc = fixed;
				while (--cnt)
					*fd++ = HEXTOCHAR[*pc++ ] << 4 | HEXTOCHAR[*pc++ ];
				break;
			}
			}
		}
	}

	m_host = AnsiString(Parameter("Host").c_str());
	m_port = Parameter("Port");
	CloseSocket();
}

void PleoCommandTCPIPTask::CloseSocket() {
	if (m_socket != INVALID_SOCKET)
		closesocket(m_socket);
	m_socket = INVALID_SOCKET;
}

bool PleoCommandTCPIPTask::Connect() {
	CloseSocket();

	bciout << "Resolving host name..." << endl;
	struct hostent *he = gethostbyname(m_host.c_str());
	if (!he) {
		bcierr << "Cannot resolve host name: " << m_host.c_str() << endl;
		return false;
	}

	bciout << "Creating socket ..." << endl;
	SOCKET lsd = socket(PF_INET, SOCK_STREAM, 0);
	if (lsd == INVALID_SOCKET) {
		printErr("Cannot create socket");
		return false;
	}

	struct sockaddr_in sa;
	memset(&sa, 0, sizeof(sa));
	sa.sin_family = AF_INET;
	sa.sin_port = htons(m_port);
	memcpy(&sa.sin_addr, he->h_addr_list[0], he->h_length);
	bciout << "Connecting socket..." << endl;
	if (connect(lsd, (struct sockaddr*) &sa, sizeof(sa))) {
		closesocket(lsd);
		printOut("Cannot connect to " << m_host.c_str() << " at port "
				<< m_port);
		return false;
	}
	m_socket = lsd;
	bciout << "Connected" << endl;
	return true;
}

static const char TOHEX[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'};

bool PleoCommandTCPIPTask::SafeSend(const void *data, int len, bool reconnect) {
	if (m_socket == INVALID_SOCKET) {
		if (!reconnect) {
			bciout << "Not connected - skipping this data block" << endl;
			return false;
		}
		if (!Connect())
			return false;
	}

//	const char* pc = (const char*) data;
//	for (int i = 0; i < len; ++i) {
//		bciout << TOHEX[(pc[i] >> 4) & 0x0F];
//		bciout << TOHEX[pc[i] & 0x0F];
//	}
//	bciout << endl;
	if (send(m_socket, (const char*) data, len, 0) == len)
		return true;
	bciout << "Could not send data - skipping this data block" << endl;
	CloseSocket();
	Sleep(1000);
	return false;
}

bool PleoCommandTCPIPTask::SendDataHeader(int fieldCount, prio_t prio,
        time_t time) {
	if (fieldCount < 1 || fieldCount > MAX_FIELDS) {
		bcierr << "Invalid number of fields: " << fieldCount << endl;
		return false;
	}
	int cnt1 = fieldCount > 8 ? 8 : fieldCount;

	unsigned int flags = 0;
	if (prio != PRIO_DEFAULT)
		flags |= FLAG_PRIORITY;
	if (time != TIME_NOTIME)
		flags |= FLAG_TIMESTAMP;
	if (fieldCount > 8)
		flags |= FLAG_VERYLONG;
	unsigned int header = (flags & 0x1F) << 27 | (cnt1 - 1 & 0x07) << 24;
	for (int i = 0; i < cnt1; ++i)
		header |= (m_fields[i].type & 0x07) << i * 3;
	header = htonl(header);

	// send header
	if (!SafeSend(&header, 4, true)) // no sizeof here
		return false;
	if (prio != PRIO_DEFAULT) {
		if (prio < PRIO_LOWEST || prio > PRIO_HIGHEST) {
			bcierr << "Invalid priority: " << prio << endl;
			return false;
		}
		if (!SafeSend(&prio, 1))
			return false;
	}
	if (time != TIME_NOTIME) {
		if (time < 0 || time > 0xFFFFFFFFL) {
			bcierr << "Invalid time: " << time << endl;
			return false;
		}
		unsigned int timeUI = htonl(time);
		if (!SafeSend(&timeUI, 4))
			return false;
	}
	if (fieldCount > 8) {
		// 10 bytes: 5 bits for count, then 24 * 3 bits for type
		// (last 3 bits ignored)
		unsigned char b = ((fieldCount - 1) << 3) | (m_fields[8].type & 0x07);
		if (!SafeSend(&b, 1))
			return false;
		// send in blocks of 3 bytes (for 8 types)
		int n = 8;
		for (int j = 0; j < 3; ++j) {
			unsigned int b4 = 0;
			for (int i = 0; i < 8; ++i) {
				b4 <<= 3;
				if (++n < fieldCount)
					b4 |= m_fields[n].type & 0x07;
			}
			b4 <<= 8; // only first 3 bytes used
			b4 = htonl(b4);
			if (!SafeSend(&b4, 3))
				return false;
		}
	}

	return true;
}

static inline long long htonll(long long l) {
	return ((long long) htonl((int) l) << 32) | htonl((int) (l >> 32));
}

bool PleoCommandTCPIPTask::SendDataField(type_t type, long long data) {
	switch (type) {
	case TYPE_INT8: {
		signed char i = data;
		return SafeSend(&i, 1);
	}
	case TYPE_INT32: {
		signed int i = data;
		return SafeSend(&i, 4);
	}
	case TYPE_INT64: {
		return SafeSend(&data, 8);
	}
	default:
		bcierr << "Invalid data type for int values: " << type << endl;
		return false;
	}
}

bool PleoCommandTCPIPTask::SendDataField(type_t type, double data) {
	switch (type) {
	case TYPE_FLOAT32: {
		float f = data;
		int i = htonl(*((int *) (void *) &f));
		return SafeSend(&i, 4);
	}
	case TYPE_FLOAT64: {
		long long i = htonll(*((long long *) (void *) &data));
		return SafeSend(&i, 8);
	}
	default:
		bcierr << "Invalid data type for float values: " << type << endl;
		return false;
	}
}

bool PleoCommandTCPIPTask::SendDataField(type_t type, const char *data) {
	switch (type) {
	case TYPE_NTSTRING: {
		unsigned char b0 = 0;
		return SafeSend(data, strlen(data)) && SafeSend(&b0, 1);
	}
	case TYPE_UTFSTRING: {
		unsigned short len = htons(strlen(data));
		if (!SafeSend(&len, 2))
			return false;
		const char *pc = data;
		unsigned char c;
		while ((c = *pc++ )) {
			if (c & 0x80) {
				// need special handling if highest bit is set
				unsigned char c0 = 0xC0 | ((c >> 6) & 0x1F);
				unsigned char c1 = 0x80 | (c & 0x3F);
				if (!SafeSend(&c0, 1) || !SafeSend(&c1, 1)) //TODO send c1 before c0?
					return false;
			} else if (!SafeSend(&c, 1))
				return false;
		}
	}
	default:
		bcierr << "Invalid data type for string values: " << type << endl;
		return false;
	}
}

bool PleoCommandTCPIPTask::SendDataField(type_t type,
        const unsigned char *data, int len) {
	switch (type) {
	case TYPE_DATA: {
		int lenNBO = htonl(len);
		return SafeSend(&lenNBO, 4) && SafeSend(data, len);
	}
	default:
		bcierr << "Invalid data type for binary values: " << type << endl;
		return false;
	}
}

void PleoCommandTCPIPTask::Process(const GenericSignal& input, GenericSignal&) {
	if (!m_fieldCount)
		return; // no need to send anything

	int cntS = input.Elements(), cntC = input.Channels();
	for (int s = 0; s < cntS; ++s) {
		int numberOfFieldsToSend = 0;
		for (int i = m_fieldCount - 1; i >= 0; --i) {
			int ch = m_fields[i].channel;
			if (ch >= 0 && ch < cntC && input(ch, s)) {
				// we have data waiting in a channel connected to this field
				// so i is the last field with valid data
				numberOfFieldsToSend = i + 1;
				break;
			}
		}
		if (!numberOfFieldsToSend)
			continue; // skip this sample as we have no channel-data to send

		if (!SendDataHeader(numberOfFieldsToSend))
			return; // try again on the next data sequence
		for (int i = 0; i < numberOfFieldsToSend; ++i) {
			Field *field = &m_fields[i];
			int ch = field->channel;
			if (ch >= cntC)
				ch = -1; // use the fixed value if channel is out of range
			switch (field->type) {
			case TYPE_INT8:
			case TYPE_INT32:
			case TYPE_INT64:
				if (!SendDataField(field->type, ch >= 0 ? (long long) input(ch,
				        s) : field->fixedInt))
					return;
				break;
			case TYPE_FLOAT32:
			case TYPE_FLOAT64:
				if (!SendDataField(field->type, ch >= 0 ? input(ch, s)
				        : field->fixedFloat))
					return;
				break;
			case TYPE_NTSTRING:
			case TYPE_UTFSTRING:
				if (ch >= 0) {
					char buf[32];
					snprintf(buf, 32, "%f", input(ch, s));
					if (!SendDataField(field->type, buf))
						return;
				} else if (!SendDataField(field->type, field->fixedString))
					return;
				break;
			case TYPE_DATA:
				if (ch >= 0) {
					double d = input(ch, s);
					if (!SendDataField(field->type, (unsigned char*) &d,
					        sizeof(d)))
						return;
				} else if (!SendDataField(field->type, field->fixedData,
				        field->fixedDataLen))
					return;
				break;
			default:
				bcierr << "Invalid field type: " << field->type << endl;
				return;
			}
		}
	}
}

void PleoCommandTCPIPTask::Halt() {
	CloseSocket();

	for (int i = 0; i < MAX_FIELDS; ++i) {
		Field *field = &m_fields[i];
		free(field->fixedString);
		free(field->fixedData);
	}
	memset(m_fields, 0, sizeof(m_fields));

}
