#ifndef EXPRPARSER_H
#define EXPRPARSER_H

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_INSTR_NAME 8
#define MAX_FUNC_NAME 16

#define SIG_NON 0
#define SIG_INT 1
#define SIG_DBL 2

struct instrexec {
	int idx;
	union {
		long long i;
		double d;
	} args[4];
};
typedef struct instrexec instrexec;

typedef void (*instrfunc)(instrexec *ie);
struct instruction {
	instrfunc f;
	unsigned char sig[4];
	char name[MAX_INSTR_NAME];
};
typedef struct instruction instruction;

#define ERROR_SUCCESS 0
#define ERROR_SYNTAX 1
#define ERROR_FUNCTION_UNKNOWN 2
#define ERROR_SYMB_TOO_LONG 3

struct instrlist {
	size_t cnt, cap;
	instrexec* p;
	int lastError;
	int lastErrorPos;
};
typedef struct instrlist instrlist;

typedef double (*funcDD)(double v1);
typedef double (*funcDDD)(double v1, double v2);
typedef double (*funcDID)(int v1, double v2);
struct function {
	// only one of the func... must be defined, all other must be 0
	funcDD fDD;
	funcDDD fDDD;
	funcDID fDID;
	char name[MAX_FUNC_NAME];
};
typedef struct function function;

void resetfree(void);
int yyparse(void);
void yyerror(const char*);
int yylex(void);

void addInstrI(int idx, long long a1);
void addInstrID(int idx, long long a1, double a2);
void addInstrII(int idx, long long a1, long long a2);
void addInstrIII(int idx, long long a1, long long a2, long long a3);
void addInstrIIII(int idx, long long a1, long long a2, long long a3,
        long long a4);

instrlist *parse(const char *expr);
void freeInstrList(instrlist *il);
char *printAll(instrlist *il);
double execute(instrlist *il, double *in, int inCount);

#endif
