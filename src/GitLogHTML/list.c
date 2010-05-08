#include "main.h"

static void ensureCap(list *l) {
	if (l->cap == l->size) {
		if (l->cap)
			l->cap *= 2;
		else
			l->cap = 4;
		l->data = (void **) realloc(l->data, l->cap * sizeof(void *));
		l->dup = (unsigned char *) realloc(l->dup, l->cap
		        * sizeof(unsigned char));
	}
}

void listInit(list *l) {
	memset(l, 0, sizeof(list));
	ensureCap(l);
}

list *listNew() {
	list *res = (list *) calloc(1, sizeof(list));
	ensureCap(res);
	return res;
}

void listFree(list *l) {
	listUninit(l);
	free(l);
}

void listUninit(list *l) {
	if (l) {
		listClear(l);
		free(l->data);
		free(l->dup);
	}
}

unsigned int listSize(const list *l) {
	return l->size;
}

void listAdd(list *l, void *p) {
	ensureCap(l);
	l->data[l->size] = p;
	l->dup[l->size] = 0;
	++l->size;
}

void listAddDup(list *l, const void *p, size_t len) {
	ensureCap(l);
	void *pd = malloc(len);
	memcpy(pd, p, len);
	l->data[l->size] = pd;
	l->dup[l->size] = 1;
	++l->size;
}

void listSet(list *l, size_t index, void *p) {
	if (l->dup[index])
		free(l->data[index]);
	l->data[index] = p;
	l->dup[index] = 0;
}

void listSetDup(list *l, size_t index, const void *p, size_t len) {
	if (l->dup[index])
		free(l->data[index]);
	void *pd = malloc(len);
	memcpy(pd, p, len);
	l->data[index] = pd;
	l->dup[index] = 1;
}

const void *listGet(const list *l, size_t index) {
	return l->data[index];
}

void *listGetNC(list *l, size_t index) {
	return l->data[index];
}

void listClear(list *l) {
	unsigned int i;
	for (i = 0; i < l->size; ++i)
		if (l->dup[i])
			free(l->data[i]);
	l->size = 0;
}

