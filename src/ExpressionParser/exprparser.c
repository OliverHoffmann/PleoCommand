#include "exprparser.h"

#include "parserrules.h"

#include <math.h>

typedef int bool;
#define TRUE 1
#define FALSE  0

#define MAX_VAL 64

function functable[] = { //
        {fDD:&acos, name:"acos"}, // math.h
                {fDD:&acosh, name:"acosh"}, // math.h
                {fDD:&asin, name:"asin"}, // math.h
                {fDD:&asinh, name:"asinh"}, // math.h
                {fDD:&atan, name:"atan"}, // math.h
                {fDDD:&atan2, name:"atan2"}, // math.h
                {fDD:&atanh, name:"atanh"}, // math.h
                {fDD:&cbrt, name:"cbrt"}, // math.h
                {fDD:&ceil, name:"ceil"}, // math.h
                {fDD:&cos, name:"cos"}, // math.h
                {fDD:&cosh, name:"cosh"}, // math.h
                {fDD:&erf, name:"erf"}, // math.h
                {fDD:&erfc, name:"erfc"}, // math.h
                {fDD:&exp, name:"exp"}, // math.h
                //{fDD:&exp10, name:"exp10"}, // math.h
                {fDD:&exp2, name:"exp2"}, // math.h
                {fDD:&fabs, name:"abs"}, // math.h
                {fDDD:&fdim, name:"dim"}, // math.h
                {fDD:&floor, name:"floor"}, // math.h
                {fDDD:&fmax, name:"max"}, // math.h
                {fDDD:&fmin, name:"min"}, // math.h
                {fDDD:&fmod, name:"mod"}, // math.h
                {fDDD:&hypot, name:"hypot"}, // math.h
                {fDID:&jn, name:"jn"}, // math.h
                {fDD:&lgamma, name:"lgamma"}, // math.h
                {fDD:&log, name:"log"}, // math.h
                {fDD:&log10, name:"log10"}, // math.h
                {fDD:&log1p, name:"log1p"}, // math.h
                {fDD:&log2, name:"log2"}, // math.h
                {fDD:&logb, name:"logb"}, // math.h
                {fDD:&nearbyint, name:"nearbyint"}, // math.h
                {fDDD:&pow, name:"pow"}, // math.h, also "^" operator
                {fDDD:&remainder, name:"remainder"}, // math.h
                {fDD:&round, name:"round"}, // math.h
                {fDD:&sin, name:"sin"}, // math.h
                {fDD:&sinh, name:"sinh"}, // math.h
                {fDD:&sqrt, name:"sqrt"}, // math.h
                {fDD:&tan, name:"tan"}, // math.h
                {fDD:&tanh, name:"tanh"}, // math.h
                {fDD:&tgamma, name:"tgamma"}, // math.h
                {fDD:&trunc, name:"trunc"}, // math.h
                {fDID:&yn, name:"yn"}, // math.h
                {fDD:0, 0, name:""}, // END MARKER
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
	long long i = ie->args[1].i;
	val[ie->args[0].i] = i < 0 || i > 31 ? 0 : channelData[i];
}

static void fDD(instrexec* ie) {
	val[ie->args[0].i] = (*functable[ie->args[1].i].fDD)(val[ie->args[2].i]);
}

static void fDDD(instrexec* ie) {
	val[ie->args[0].i] = (*functable[ie->args[1].i].fDDD)(val[ie->args[2].i],
	        val[ie->args[3].i]);
}

static void fDID(instrexec* ie) {
	val[ie->args[0].i] = (*functable[ie->args[1].i].fDID)(
	        (int) val[ie->args[2].i], val[ie->args[3].i]);
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

static void lteqD(instrexec* ie) {
	val[ie->args[0].i] = val[ie->args[1].i] <= val[ie->args[2].i];
}

static void gteqD(instrexec* ie) {
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
                {&fDD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "D_D"}, //     3
                {&fDDD, {SIG_INT, SIG_INT, SIG_INT, SIG_INT}, "D_DD"}, //   4
                {&addD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "ADD"}, //    5
                {&subD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "SUB"}, //    6
                {&mulD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "MUL"}, //    7
                {&divD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "DIV"}, //    8
                {&powD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "POW"}, //    9
                {&negD, {SIG_INT, SIG_INT, SIG_NON, SIG_NON}, "NEG"}, //   10
                {&retD, {SIG_INT, SIG_NON, SIG_NON, SIG_NON}, "RET"}, //   11
                {&lteqD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "LTEQ"}, // 12
                {&gteqD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "GTEQ"}, // 13
                {&ltD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "LT"}, //     14
                {&gtD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "GT"}, //     15
                {&andD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "AND"}, //   16
                {&orD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "OR"}, //     17
                {&equD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "EQU"}, //   18
                {&neqD, {SIG_INT, SIG_INT, SIG_INT, SIG_NON}, "NEQ"}, //   19
                {&fDID, {SIG_INT, SIG_INT, SIG_INT, SIG_INT}, "D_ID"}, //  20
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
		while (fp->name[0]) {
			if (!strcmp(fp->name, buf)) {
				if (fp->fDD)
					return FDD;
				if (fp->fDDD)
					return FDDD;
				if (fp->fDID)
					return FDID;
				curInstrlist->lastError = ERROR_SYNTAX;
				curInstrlist->lastErrorPos = 0;
				return -1;
			}
			++yylval.i;
			++fp;
		}
		curInstrlist->lastError = ERROR_FUNCTION_UNKNOWN;
		curInstrlist->lastErrorPos = 1 - (int) strlen(buf);
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

#define ADD(format, value) 							\
	cnt = snprintf(pc, remain, format, value);		\
	if (cnt > 0) {									\
		if ((size_t) cnt > remain) {				\
			*pc = 0;								\
			return res;								\
		} 											\
		pc += cnt;									\
		remain -= (size_t) cnt;						\
	}

char *printAll(instrlist *il) {
	size_t remain = il->cnt * 128;
	char *res = malloc(remain + 1), *pc = res;
	int cnt;
	size_t i;
	for (i = 0; i < il->cnt; ++i) {
		instrexec* ie = &il->p[i];
		instruction *is = &instrtable[ie->idx];
		ADD("%s", is->name)
		size_t j;
		for (j = 0; j < 4; ++j)
			switch (is->sig[j]) {
			case SIG_INT:
				ADD(" %Ld", ie->args[j].i)
				break;
			case SIG_DBL:
				ADD(" %f", ie->args[j].d)
				break;
			default:
				break;
			}
		*pc++ = '\n';
		--remain;
	}
	*pc = 0;
	return res;
}

double execute(instrlist *il, double *in, int inCount) {
	if (executing) {
		return .0;
	}
	executing = TRUE;
	memset(channelData, 0, sizeof(channelData));
	int i;
	for (i = 0; i < inCount; ++i)
		channelData[i] = *in++ ;
	output = .0;
	size_t j;
	for (j = 0; j < il->cnt; ++j)
		(*instrtable[il->p[j].idx].f)(&il->p[j]);
	executing = FALSE;
	return output;
}
