#include "exprparser.h"

typedef int bool;
#define TRUE 1
#define FALSE  0

#define MAX_VAL 64

function functable[] = { //
        { {fDD:&sin}, FUNC_DD, "sin"}, //
                { {fDDD:&hypot}, FUNC_DDD, "hypot"}, //
                { {fDD:0}, 0, ""}, //
        };

static const int MAX_SYMB = 240;

static double channelData[32];
static double val[MAX_VAL];
static double output;
static instrlist *curInstrlist;
static const char *exprPos;
static bool parsing = FALSE;
static bool executing = FALSE;

static void setL(instrexec* ie) {
	val[ie->args[0].i] = ie->args[1].i;
}

static void setD(instrexec* ie) {
	val[ie->args[0].i] = ie->args[1].d;
}

static void load(instrexec* ie) {
	val[ie->args[0].i] = channelData[ie->args[1].i];
}

static void fn1(instrexec* ie) {
	val[ie->args[0].i] = (*functable[ie->args[1].i].fDD)(val[ie->args[2].i]);
}

static void fn2(instrexec* ie) {
	val[ie->args[0].i] = (*functable[ie->args[1].i].fDDD)(val[ie->args[2].i],
	        val[ie->args[3].i]);
}

static void addD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] + val[ie->args[2].i];
}

static void subD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] - val[ie->args[2].i];
}

static void mulD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] * val[ie->args[2].i];
}

static void divD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] / val[ie->args[2].i];
}

static void powD(instrexec* ie) {
	val[ie->args[0].i] = pow(val[ie->args[1].i], val[ie->args[2].i]);
}

static void negD(instrexec* ie) {
	val[ie->args[0].i] = -val[ie->args[1].i];
}

static void retD(instrexec* ie) {
	output = val[ie->args[0].i];
}

static instruction instrtable[] = { //
        {&setL, {SIG_INT, SIG_INT, SIG_NON, SIG_NON}, "SETL"}, //
                {&setD, {SIG_INT, SIG_DBL, SIG_NON, SIG_NON}, "SETD"}, //
                {&load, {SIG_INT, SIG_INT, SIG_NON, SIG_NON}, "LOAD"}, //
                {&fn1, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "FN1"}, //
                {&fn2, {SIG_INT, SIG_INT, SIG_INT, SIG_INT}, "FN2"}, //
                {&addD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "ADD"}, //
                {&subD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "SUB"}, //
                {&mulD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "MUL"}, //
                {&divD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "DIV"}, //
                {&powD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "POW"}, //
                {&negD, {SIG_INT, SIG_INT, SIG_NON, SIG_NON}, "NEG"}, //
                {&retD, {SIG_INT, SIG_NON, SIG_NON, SIG_NON}, "RET"}, //
                {0, {SIG_NON, SIG_NON, SIG_NON, SIG_NON}, ""} //
        };

static instrexec* nextInstr() {
	if (curInstrlist->cnt == curInstrlist->cap || !curInstrlist->p) {
		curInstrlist->cap *= 2;
		curInstrlist->p = (instrexec *) realloc(curInstrlist->p,
		        curInstrlist->cap * sizeof(instrexec));
	}
	return &curInstrlist->p[curInstrlist->cnt++ ];
}

void addInstrI(int idx, long long a1) {
	instrexec *ie = nextInstr();
	ie->idx = idx;
	ie->args[0].i = a1;
}

void addInstrID(int idx, long long a1, double a2) {
	instrexec *ie = nextInstr();
	ie->idx = idx;
	ie->args[0].i = a1;
	ie->args[1].d = a2;
}

void addInstrII(int idx, long long a1, long long a2) {
	instrexec *ie = nextInstr();
	ie->idx = idx;
	ie->args[0].i = a1;
	ie->args[1].i = a2;
}

void addInstrIII(int idx, long long a1, long long a2, long long a3) {
	instrexec *ie = nextInstr();
	ie->idx = idx;
	ie->args[0].i = a1;
	ie->args[1].i = a2;
	ie->args[2].i = a3;
}

void addInstrIIII(int idx, long long a1, long long a2, long long a3,
        long long a4) {
	instrexec *ie = nextInstr();
	ie->idx = idx;
	ie->args[0].i = a1;
	ie->args[1].i = a2;
	ie->args[2].i = a3;
	ie->args[3].i = a4;
}

instrlist *parse(const char *expr) {
	if (parsing) {
		return 0;
	}
	resetfree();
	parsing = TRUE;
	exprPos = expr;
	curInstrlist = malloc(sizeof(instrlist));
	curInstrlist->cnt = 0;
	curInstrlist->cap = 16;
	curInstrlist->p = (instrexec *) malloc(16 * sizeof(instrexec));
	curInstrlist->lastError = ERROR_SUCCESS;
	curInstrlist->lastErrorPos = 0;
	int res = yyparse();
	instrlist *il = curInstrlist;
	curInstrlist = 0;
	parsing = FALSE;
	if (res) {
		if (!il->lastError)
			il->lastError = ERROR_SYNTAX;
		il->lastErrorPos += exprPos - expr - 1;
		il->cnt = 0;
	}
	return il;
}

void freeInstrList(instrlist *il) {
	if (il)
		free(il->p);
	free(il);
}

int getNextToken(int *res) {
	char c;
	while ((c = *exprPos++ ) && (c == ' ' || c == '\t')) {
		// ignore spaces
	}
	if (!c)
		return 0;

	if (isdigit(c)) {
		*res = c - '0';
		return -2;
	}

	if (isalpha(c)) {
		// is a function name
		char buf[MAX_SYMB + 1];
		int i = -1;
		do {
			if (++i == MAX_SYMB) {
				curInstrlist->lastError = ERROR_SYMB_TOO_LONG;
				curInstrlist->lastErrorPos = -MAX_SYMB;
				return -1;
			}
			buf[i] = c;
			c = *exprPos++ ;
		} while (isalnum(c));
		--exprPos;
		buf[++i] = '\0';

		function *fp = functable;
		*res = 0;
		while (*fp->fDD) {
			if (!strcmp(fp->name, buf))
				return -3;
			++*res;
			++fp;
		}
		curInstrlist->lastError = ERROR_FUNCTION_UNKNOWN;
		curInstrlist->lastErrorPos = -strlen(buf);
		return -1;
	}

	// is a token
	return c;
}

static void print(instrexec* ie) {
	instruction *is = &instrtable[ie->idx];
	fprintf(stderr, "%s", is->name);
	int i;
	for (i = 0; i < 4; ++i)
		switch (is->sig[i]) {
		case SIG_INT:
			fprintf(stderr, " %Ld", ie->args[i].i);
			break;
		case SIG_DBL:
			fprintf(stderr, " %f", ie->args[i].d);
			break;
		default:
			break;
		}
	fprintf(stderr, "\n");
}

void printAll(instrlist *il) {
	int i;
	for (i = 0; i < il->cnt; ++i)
		print(&il->p[i]);
}

double execute(instrlist *il, double *in, int inCount) {
	if (executing) {
		return .0;
	}
	executing = TRUE;
	int i;
	for (i = 0; i < inCount; ++i)
		channelData[i] = *in++ ; //FIXME bounds in load() not checked
	output = .0;
	for (i = 0; i < il->cnt; ++i)
		(*instrtable[il->p[i].idx].f)(&il->p[i]);
	executing = FALSE;
	return output;
}
