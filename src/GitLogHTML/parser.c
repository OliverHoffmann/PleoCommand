#include "main.h"

#ifdef UNIX
static char templateFile[] = "/tmp/gitloghtml-datemsk-XXXXXX";
#endif

#ifdef WIN
static char templateFile[] = "C:\\Windows\\Temp\\gitloghtml-datemsk-XXXXXX";
#endif

void parserInit() {
	int fd = mkstemp(templateFile);
	if (fd == -1) {
		fprintf(stderr, "Cannot create temporary file: '%s', "
			"so date parsing is unavailable\n", templateFile);
	} else {
		FILE *f = fdopen(fd, "w");
		if (!f) {
			fprintf(stderr, "Cannot open temporary file: '%s', "
				"so date parsing is unavailable\n", templateFile);
			close(fd);
		} else {
			// ISO-8601 date like "2010-05-06 20:15:40 +0200 (UTC)"
			fputs("%Y-%m-%d %H:%M:%S %z (%Z)\n", f);
			fputs("%y-%m-%d %H:%M:%S %z (%Z)\n", f);
			fputs("%Y-%m-%d %H:%M:%S %z\n", f);
			fputs("%y-%m-%d %H:%M:%S %z\n", f);
			fputs("%Y-%m-%d %H:%M:%S\n", f);
			fputs("%y-%m-%d %H:%M:%S\n", f);
			fputs("%Y-%m-%d\n", f);
			fputs("%y-%m-%d\n", f);

			// RFC-2822 date like "Wed, 6 May 2010 20:15:40 +0200"
			fputs("%a, %d %b %Y %H:%M:%S %z (%Z)\n", f);
			fputs("%a, %d %b %y %H:%M:%S %z (%Z)\n", f);
			fputs("%a, %d %b %Y %H:%M:%S %z\n", f);
			fputs("%a, %d %b %y %H:%M:%S %z\n", f);
			fputs("%a, %d %b %Y %H:%M:%S\n", f);
			fputs("%a, %d %b %y %H:%M:%S\n", f);
			fputs("%a, %d %b %Y\n", f);
			fputs("%a, %d %b %y\n", f);

			// local date like "Wed May 6 20:15:40 2010"
			fputs("%b %d %I:%M:%S %p %Y\n", f);
			fputs("%b %d %I:%M:%S %p %y\n", f);
			fputs("%b %d %H:%M:%S %Y\n", f);
			fputs("%b %d %H:%M:%S %y\n", f);
			fputs("%b %d %Y\n", f);
			fputs("%b %d %y\n", f);
			fputs("%a %b %d %I:%M:%S %p %Y\n", f);
			fputs("%a %b %d %I:%M:%S %p %y\n", f);
			fputs("%a %b %d %H:%M:%S %Y\n", f);
			fputs("%a %b %d %H:%M:%S %y\n", f);
			fputs("%a %b %d %Y\n", f);
			fputs("%a %b %d %y\n", f);

			// raw date like "1273063602 +0200"
			fputs("%s %z (%Z)\n", f);
			fputs("%s %z\n", f);
			fputs("%s\n", f);

			// other fallback formats
			fputs("%c\n", f);
			fputs("%Ec\n", f);
			fputs("%x\n", f);
			fputs("%Ex\n", f);
			fputs("%X\n", f);
			fputs("%EX\n", f);

			fclose(f);
			setenv("DATEMSK", templateFile, 1);
		}
	}
}

void parserFinalize() {
	unsetenv("DATEMSK");
	unlink(templateFile);
}

static void omitGraph(const char **line) {
	char c;
	while ((c = (*line)[0]) && (c == '|' || c == '*' || c == '/' || c == '\\'
	        || c == ' '))
		++*line;
}

int newHeaderBegins(const char *line) {
	omitGraph(&line);

	if (strstr(line, "commit ") == line)
		return 1;
	if (strstr(line, "From ") == line)
		return 1;
	return 0;
}

static int parseRelativeDate(const char *buf, struct tm *tm) {
	struct tm now;
	time_t nowTT = time(NULL);
	localtime_r(&nowTT, &now);

	memset(tm, 0, sizeof(*tm));

	char dummy[2];
	int val;
	if (sscanf(buf, "%d minute%1[s] ago", &val, dummy) == 2) {
		tm->tm_year = now.tm_year;
		tm->tm_mon = now.tm_mon;
		tm->tm_mday = now.tm_mday;
		tm->tm_hour = now.tm_hour;
		tm->tm_min = now.tm_min - val;
		tm->tm_sec = now.tm_sec;
		return 1;
	}
	if (sscanf(buf, "%d hour%1[s] ago", &val, dummy) == 2) {
		tm->tm_year = now.tm_year;
		tm->tm_mon = now.tm_mon;
		tm->tm_mday = now.tm_mday;
		tm->tm_hour = now.tm_hour - val;
		tm->tm_min = now.tm_min;
		return 1;
	}
	if (sscanf(buf, "%d day%1[s] ago", &val, dummy) == 2) {
		tm->tm_year = now.tm_year;
		tm->tm_mon = now.tm_mon;
		tm->tm_mday = now.tm_mday - val;
		tm->tm_hour = now.tm_hour;
		return 1;
	}
	if (sscanf(buf, "%d week%1[s] ago", &val, dummy) == 2) {
		tm->tm_year = now.tm_year;
		tm->tm_mon = now.tm_mon;
		tm->tm_mday = now.tm_mday - val * 7;
		return 1;
	}
	if (sscanf(buf, "%d month%1[s] ago", &val, dummy) == 2) {
		tm->tm_year = now.tm_year;
		tm->tm_mon = now.tm_mon - val;
		tm->tm_mday = now.tm_mday;
		return 1;
	}
	if (sscanf(buf, "%d year%1[s] ago", &val, dummy) == 2) {
		tm->tm_year = now.tm_year - val;
		tm->tm_mon = now.tm_mon;
		return 1;
	}
	return 0;
}

static int handleGetDateResult(header *hdr, const char *buf) {
	struct tm tm;
	switch (getdate_r(buf, &tm)) {
	case 0:
		hdr->date = mktime(&tm);
		return 1;
	case 1:
		fprintf(stderr, "Cannot parse date: DATEMSK undefined \n");
		break;
	case 2:
		fprintf(stderr, "Cannot parse date: Cannot open file\n");
		break;
	case 3:
		fprintf(stderr, "Cannot parse date: Cannot get file-status\n");
		break;
	case 4:
		fprintf(stderr, "Cannot parse date: Not a regular file\n");
		break;
	case 5:
		fprintf(stderr, "Cannot parse date: Cannot read from file\n");
		break;
	case 6:
		fprintf(stderr, "Cannot parse date: Out of memory\n");
		break;
	case 7:
		if (parseRelativeDate(buf, &tm)) {
			hdr->date = mktime(&tm);
			return 1;
		}
		fprintf(stderr, "Cannot parse date: Unknown format: %s\n", buf);
		break;
	case 8:
		fprintf(stderr, "Cannot parse date: Invalid format specification\n");
		break;
	default:
		fprintf(stderr, "Cannot parse date: Unknown error in getdate_r()\n");
	}
	return 0;
}

int parseHeader(const char *line, header *hdr) {
	char buf[STR_SHORT];
	const char *lineOrg = line;

	omitGraph(&line);

	if (sscanf(line, "commit %"STR_SHORT_S"s", hdr->commit) == 1)
		return 1;
	if (sscanf(line, "From %"STR_SHORT_S"[^\n]", hdr->commit) == 1)
		return 1;

	if (sscanf(line, "Author: %"STR_SHORT_S"[^\n]", hdr->author) == 1)
		return 1;
	if (sscanf(line, "From: %"STR_SHORT_S"[^\n]", hdr->author) == 1)
		return 1;

	if (sscanf(line, "Date: %"STR_SHORT_S"[^\n]", buf) == 1 || //
	        sscanf(line, "AuthorDate: %"STR_SHORT_S"[^\n]", buf) == 1) {
		if (handleGetDateResult(hdr, buf))
			return 1;
	}

	// is a message the last four characters before the string were spaces
	if ((line - lineOrg >= 4) && line[-1] == ' ' && line[-2] == ' ' && line[-3]
	        == ' ' && line[-4] == ' ' && sscanf(line, "%"STR_SHORT_S"[^\n]",
	        buf) == 1) {
		strncat(hdr->message, buf, STR_LONG - strlen(hdr->message));
		size_t ml = strlen(hdr->message);
		if (ml < STR_LONG - 1) {
			hdr->message[ml] = '\n';
			hdr->message[ml + 1] = 0;
		}
		return 1;
	}

	// return true if line is empty after graph removal
	return !line[0] || line[0] == '\n';
}

static int fuzzyEqual(const char *pc1, const char *pc2) {
	if (pc1[0] == '.' && pc1[1] == '.' && pc1[2] == '.') {
		// pc1 contains "...foo", check if pc2 ends with "foo"
		return !strcmp(pc1 + 3, pc2 + strlen(pc2) - strlen(pc1) + 3);
	}
	return 0;
}

static int getListIndex(list *l, const statEntry *se) {
	size_t i;
	for (i = 0; i < listSize(l); ++i) {
		char *pc = ((statEntry *) listGetNC(l, i))->file;
		if (!strcmp(se->file, pc))
			return (int) i;
		if (fuzzyEqual(se->file, pc))
			return (int) i;
		if (fuzzyEqual(pc, se->file)) {
			// replace the short version with the full one
			strcpy(pc, se->file); // pc has same (i.e. enough) space, so this is safe
			return (int) i;
		}
	}
	return -1;
}

static void checkAndAdd(list *l, const statEntry *se, int isBinary) {
	int listIndex = getListIndex(l, se);
	if (isBinary) {
		// use binary version from --stat and not --numstat, if both are available
		// so ignore the --numstat version if file already listed
		// (the probably shortened version of the --numstat filename has already been
		// replaced during getListIndex() with the one in *se if it is listed)
		if (listIndex == -1)
			listAddDup(l, se, sizeof(*se));
	} else {
		// use ascii version from --numstat and not --stat, if both are available
		// so replace the --stat version if file already listed
		if (listIndex >= 0)
			listSetDup(l, (size_t) listIndex, se, sizeof(*se));
		else
			listAddDup(l, se, sizeof(*se));
	}
}

int parseNumStats(const char *line, list *l) {
	// omit graph
	omitGraph(&line);

	statEntry se;
	if (sscanf(line, "%u %u %"STR_SHORT_S"[^\n]", &se.add, &se.del, se.file)
	        == 3) {
		checkAndAdd(l, &se, 0);
		return 1;
	}
	if (sscanf(line, "- - %"STR_SHORT_S"[^\n]", se.file) == 1) {
		se.add = -1;
		se.del = -1;
		checkAndAdd(l, &se, 1);
		return 1;
	}
	return 0;
}

int parseStats(const char *line, list *l) {
	// omit graph
	omitGraph(&line);

	char c;
	const char *pc = line - 1, *delim = NULL;
	unsigned int cntPlus = 0, cntMinus = 0;
	while ((c = *++pc)) {
		switch (c) {
		case '|':
			delim = pc;
			cntPlus = cntMinus = 0;
			break;
		case '+':
			++cntPlus;
			break;
		case '-':
			++cntMinus;
			break;
		default:
			// nothing to do
			break;
		}
	}
	if (!delim)
		return 0;

	// get file name and check if it has already been stated in the commit
	statEntry se;
	{
		size_t cnt = (size_t) (delim - line);
		while (cnt && line[--cnt] == ' ') {
			// ignoring trailing spaces
		}
		++cnt;
		if (cnt > STR_SHORT)
			cnt = STR_SHORT;
		strncpy(se.file, line, cnt);
		se.file[cnt] = 0;
	}
	int listIndex = getListIndex(l, &se);

	unsigned int cntMods;
	if (sscanf(delim + 1, " %u ", &cntMods) == 1) {
		// cntPlus and cntMinus gives the relation between number of added and
		// removed lines, and cntMods gives the actual number of modified lines
		unsigned int cntSum = cntPlus + cntMinus;
		se.add = (int) (cntSum ? cntMods * cntPlus / cntSum : 0);
		se.del = (int) (cntSum ? cntMods * cntMinus / cntSum : 0);
		// use ascii version from --numstat and not --stat, if both are available
		// so ignore the --stat version if file already listed
		if (listIndex == -1)
			listAddDup(l, &se, sizeof(se));
	} else {
		unsigned int bytesBefore, bytesAfter;
		if (sscanf(delim + 1, " Bin %u -> %u ", &bytesBefore, &bytesAfter) != 2)
			return 0;
		// this is a binary modification
		se.add = -(int) bytesAfter - 1;
		se.del = -(int) bytesBefore - 1;
		// use binary version from --stat and not --numstat, if both are available
		// so replace the --numstat version if file already listed
		if (listIndex >= 0) {
			// keep the already stored version of the file-name because the one
			// parsed here may be shortened
			statEntry *seT = (statEntry *) listGetNC(l, (size_t) listIndex);
			seT->add = se.add;
			seT->del = se.del;
		} else
			listAddDup(l, &se, sizeof(se));
	}

	return 1;
}

