#include "main.h"

#define MAX_INDENT 128
#define INDENT_CHARS 4
#define NOI 0
#define DOI 1
#define UNI -1
#define WITHOUT_INDENT 1234567
static int indent;
static char INDENT[MAX_INDENT * INDENT_CHARS];

#define MAX_PBUF 240
static char pbuf[MAX_PBUF + 1];

static void print(const char *pc, int indDelta) {
	if (indDelta < 0 && indDelta != WITHOUT_INDENT) {
		indent += indDelta;
		if (indent < 0)
			indent = 0;
	}
	if (indDelta != WITHOUT_INDENT && indent && fwrite(INDENT, (size_t) indent,
	        INDENT_CHARS, stdout) < INDENT_CHARS)
		fprintf(stderr, "Cannot write to stdout\n");
	puts(pc);
	if (indDelta > 0 && indDelta != WITHOUT_INDENT) {
		indent += indDelta;
		if (indent > MAX_INDENT)
			indent = MAX_INDENT;
	}
}

static void printSafe(const char *pc, int indDelta) {
	char buf[strlen(pc) * 5 + 1];
	char c, *pb = buf;
	while ((c = *pc++ )) {
		switch (c) {
		case '&':
			*pb++ = '&';
			*pb++ = 'a';
			*pb++ = 'm';
			*pb++ = 'p';
			*pb++ = ';';
			break;
		case '<':
			*pb++ = '&';
			*pb++ = 'l';
			*pb++ = 't';
			*pb++ = ';';
			break;
		case '>':
			*pb++ = '&';
			*pb++ = 'g';
			*pb++ = 't';
			*pb++ = ';';
			break;
		default:
			*pb++ = c;
		}
	}
	if (pb > buf && pb[-1] == '\n')
		--pb;
	*pb = 0;
	print(buf, indDelta);
}

static void printHeader(const header *hdr) {
	print("<commit>", DOI);
	printSafe(hdr->commit, NOI);
	print("</commit>", UNI);

	print("<author>", DOI);
	printSafe(hdr->author, NOI);
	print("</author>", UNI);

	print("<date>", DOI);
	struct tm tm;
	localtime_r(&hdr->date, &tm);
	snprintf(pbuf, MAX_PBUF, "%02d.%02d.%04d %02d:%02d", tm.tm_mday, tm.tm_mon
	        + 1, tm.tm_year + 1900, tm.tm_hour, tm.tm_min);
	printSafe(pbuf, NOI);
	print("</date>", UNI);

	print("<message>", DOI);
	printSafe(hdr->message, WITHOUT_INDENT);
	print("</message>", UNI);
}

static void printStats(const list *statList) {
	size_t i;
	print("<paths>", DOI);
	for (i = 0; i < listSize(statList); ++i) {
		const statEntry *se = (const statEntry *) listGet(statList, i);
		print("<path>", DOI);

		print("<file>", DOI);
		printSafe(se->file, NOI);
		print("</file>", UNI);

		snprintf(pbuf, MAX_PBUF, "<modifications additions=\"%d\" "
			"deletions=\"%d\"/>", se->add, se->del);
		print(pbuf, NOI);

		print("</path>", UNI);
	}
	print("</paths>", UNI);
}

void printerInit() {
	indent = 0;
	memset(INDENT, ' ', MAX_INDENT);
	print("<?xml version=\"1.0\"?>", NOI);
	print("<log>", DOI);
}

void printerFinalize() {
	print("</log>", UNI);
}

void printEntry(const header *hdr, const list *statList) {
	print("<logentry>", DOI);
	printHeader(hdr);
	printStats(statList);
	print("</logentry>", UNI);
}

