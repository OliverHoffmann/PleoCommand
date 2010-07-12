#include "exprparser.h"

#include "parserrules.h"

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
	val[ie->args[0].i] = (double) ie->args[1].i;
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

static void loetD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] <= val[ie->args[2].i];
}

static void goetD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] >= val[ie->args[2].i];
}

static void ltD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] < val[ie->args[2].i];
}

static void gtD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] > val[ie->args[2].i];
}

static void andD(instrexec* ie) {
	val[ie->args[0].i] = (int) val[ie->args[1].i] && (int) val[ie->args[2].i];
}

static void orD(instrexec* ie) {
	val[ie->args[0].i] = (int) val[ie->args[1].i] || (int) val[ie->args[2].i];
}

static void equD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] == val[ie->args[2].i];
}

static void neqD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] != val[ie->args[2].i];
}

static instruction instrtable[] = { //
        {&setL, {SIG_INT, SIG_INT, SIG_NON, SIG_NON}, "SETL"}, //           0
                {&setD, {SIG_INT, SIG_DBL, SIG_NON, SIG_NON}, "SETD"}, //   1
                {&load, {SIG_INT, SIG_INT, SIG_NON, SIG_NON}, "LOAD"}, //   2
                {&fn1, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "FN1"}, //     3
                {&fn2, {SIG_INT, SIG_INT, SIG_INT, SIG_INT}, "FN2"}, //     4
                {&addD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "ADD"}, //    5
                {&subD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "SUB"}, //    6
                {&mulD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "MUL"}, //    7
                {&divD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "DIV"}, //    8
                {&powD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "POW"}, //    9
                {&negD, {SIG_INT, SIG_INT, SIG_NON, SIG_NON}, "NEG"}, //   10
                {&retD, {SIG_INT, SIG_NON, SIG_NON, SIG_NON}, "RET"}, //   11
                {&loetD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "LOET"}, // 12
                {&goetD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "GOET"}, // 13
                {&ltD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "LT"}, //     14
                {&gtD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "GT"}, //     15
                {&andD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "AND"}, //   16
                {&orD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "OR"}, //     17
                {&equD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "EQU"}, //   18
                {&neqD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "NEQ"}, //   19
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
		il->lastErrorPos += (int) (exprPos - expr - 1);
		il->cnt = 0;
	}
	return il;
}

void freeInstrList(instrlist *il) {
	if (il)
		free(il->p);
	free(il);
}

int yylex(void) {
	char c;
	while ((c = *exprPos++ ) && (c == ' ' || c == '\t')) {
		// ignore spaces
	}
	if (!c)
		return 0; // end of stream

	if (isdigit(c)) {
		yylval.i = c - '0';
		return DIGIT;
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
		yylval.i = 0;
		while (*fp->fDD) {
			if (!strcmp(fp->name, buf))
				return FNCT;
			++yylval.i;
			++fp;
		}
		curInstrlist->lastError = ERROR_FUNCTION_UNKNOWN;
		curInstrlist->lastErrorPos = -(int) strlen(buf);
		return -1;
	}

	// look ahead to check whether we have to combine two tokens
	switch (c) {
	case '<':
		if (*exprPos == '=') {
			++exprPos;
			return LTEQ;
		}
		break;
	case '>':
		if (*exprPos == '=') {
			++exprPos;
			return GTEQ;
		}
		break;
	case '!':
		if (*exprPos == '=') {
			++exprPos;
			return NEQ;
		}
		break;
	case '=':
		if (*exprPos == '=') {
			++exprPos;
			return EQU;
		}
		break;
	case '&':
		if (*exprPos == '&') {
			++exprPos;
			return AND;
		}
		break;
	case '|':
		if (*exprPos == '|') {
			++exprPos;
			return OR;
		}
		break;
	default:
		break;
	}

	// is a token
	return c;
}

static void print(instrexec* ie) {
	instruction *is = &instrtable[ie->idx];
	fprintf(stderr, "%s", is->name);
	size_t i;
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
	size_t i;
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
	size_t j;
	for (j = 0; j < il->cnt; ++j)
		(*instrtable[il->p[j].idx].f)(&il->p[j]);
	executing = FALSE;
	return output;
}
