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
	int cnt, cap;
	instrexec* p;
	int lastError;
	int lastErrorPos;
};
typedef struct instrlist instrlist;

#define FUNC_DD		1
#define FUNC_DDD	2

typedef double (*funcDD)(double v1);
typedef double (*funcDDD)(double v1, double v2);
struct function {
	union {
		funcDD fDD;
		funcDDD fDDD;
	};
	int type;
	char name[MAX_FUNC_NAME];
};
typedef struct function function;

void resetfree();
int yyparse(void);
void yyerror(const char*);

void addInstrI(int idx, long long a1);
void addInstrID(int idx, long long a1, double a2);
void addInstrII(int idx, long long a1, long long a2);
void addInstrIII(int idx, long long a1, long long a2, long long a3);
void addInstrIIII(int idx, long long a1, long long a2, long long a3,
        long long a4);

int getNextToken(int *res);
instrlist *parse(const char *expr);
void freeInstrList(instrlist *il);
void printAll(instrlist *il);
double execute(instrlist *il, double *in, int inCount);

#endif