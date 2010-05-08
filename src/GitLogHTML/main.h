#ifndef MAIN_H_
#define MAIN_H_

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <time.h>

#define STR_SHORT 220
#define STR_LONG 65000
#define STR_SHORT_S "220"
#define STR_LONG_S "65000"

struct list {
	void **data;
	unsigned char *dup;
	unsigned int cap, size;
};
typedef struct list list;

void listInit(list *l);

list *listNew();

void listFree(list *l);

void listUninit(list *l);

unsigned int listSize(const list *l);

void listAdd(list *l, void *p);

void listAddDup(list *l, const void *p, size_t len);

void listSet(list *l, size_t index, void *p);

void listSetDup(list *l, size_t index, const void *p, size_t len);

const void *listGet(const list *l, size_t index);
void *listGetNC(list *l, size_t index);

void listClear(list *l);

//////////////////////////////////////////////

struct header {
	char commit[STR_SHORT + 1];
	char author[STR_SHORT + 1];
	time_t date;
	char message[STR_LONG + 1];
};
typedef struct header header;

struct statEntry {
	int add;
	int del;
	char file[STR_SHORT + 1];
};
typedef struct statEntry statEntry;

//////////////////////////////////////////////

void parserInit();
void parserFinalize();
int newHeaderBegins(const char *line);
int parseHeader(const char *line, header *hdr);
int parseNumStats(const char *line, list *l);
int parseStats(const char *line, list *l);

void printerInit();
void printerFinalize();
void printEntry(const header *hdr, const list *statList);

#endif
