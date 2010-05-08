#include "main.h"

struct commit {
	header hdr;
	list stats;
};
typedef struct commit commit;

static void newCommit(commit *ci) {
	memset(&ci->hdr, 0, sizeof(header));
	listInit(&ci->stats);
}

static void parsedCommit(commit *ci) {
	printEntry(&ci->hdr, &ci->stats);
	listUninit(&ci->stats);
	newCommit(ci);
}

#define POS_BEFORE 0
#define POS_HEADER 1
#define POS_AFTER 2

int main() {
	char *line = 0;
	size_t lineCap = 0;
	commit ci;
	int pos = POS_BEFORE;

	parserInit();
	printerInit();

	newCommit(&ci);

	int nr = 0;
	while (getline(&line, &lineCap, stdin) != -1) {
		++nr;
		select: switch (pos) {
		case POS_BEFORE:
			if (!newHeaderBegins(line))
				break;
			pos = POS_HEADER;
			parseHeader(line, &ci.hdr);
			break;
		case POS_HEADER:
			if (newHeaderBegins(line))
				parsedCommit(&ci);
			if (parseHeader(line, &ci.hdr))
				break;
			pos = POS_AFTER;
		case POS_AFTER:
			if (parseNumStats(line, &ci.stats))
				break;
			if (parseStats(line, &ci.stats))
				break;
			if (newHeaderBegins(line)) {
				pos = POS_HEADER;
				goto select;
			}
			break;
		default:
			fprintf(stderr, "Internal error\n");
			pos = POS_BEFORE;
		}
	}

	if (pos != POS_BEFORE)
		parsedCommit(&ci);

	printerFinalize();
	parserFinalize();

	listUninit(&ci.stats);
	free(line);
	return 0;
}
