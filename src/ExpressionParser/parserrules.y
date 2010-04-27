/* PleoCommand Expression Parser Bison Ruleset */
%{
	#include "exprparser.h"

	static int lastfree = 0;

	static int nextfree(void) {
		return ++lastfree;
	}

	void resetfree(void) {
		lastfree = 0;
	}

	int yylex(void);
%}

%union {
	int i;
	double d;
	struct {
		int cnt;
		long long val;
	} s;
}

%token <i> FNCT
%token <i> DIGIT
%type <i> exp
%type <s> nrint
%type <d> nrdbl

%left ','
%left '+' '-'
%left '*' '/'
%left NEG
%right '^'

%%

input: exp { addInstrI(11, $1); }
	;

nrint:
	DIGIT { $$.val = $1; $$.cnt = 1; }
	| nrint DIGIT { $$.val = $1.val * 10 + $2; $$.cnt = $1.cnt + 1; if ($$.cnt == 19) fprintf(stderr, "Overflow possible !!!\n"); }
	;

nrdbl:
	nrint '.' nrint { $$ = $1.val + $3.val / pow(10.0, $3.cnt); }
	;

exp:
	nrint                      { int pos = nextfree(); $$ = pos; addInstrII(0, pos, $1.val); }
	| nrdbl                    { int pos = nextfree(); $$ = pos; addInstrID(1, pos, $1); }
	| '#' nrint                { int pos = nextfree(); $$ = pos; addInstrII(2, pos, $2.val); }
	| FNCT '(' exp ')'         { int pos = nextfree(); $$ = pos; addInstrIII(3, pos, $1, $3); }
	| FNCT '(' exp ',' exp ')' { int pos = nextfree(); $$ = pos; addInstrIIII(4, pos, $1, $3, $5); }
	| exp '+' exp              { int pos = nextfree(); $$ = pos; addInstrIII(5, pos, $1, $3); }
	| exp '-' exp              { int pos = nextfree(); $$ = pos; addInstrIII(6, pos, $1, $3); }
	| exp '*' exp              { int pos = nextfree(); $$ = pos; addInstrIII(7, pos, $1, $3); }
	| exp '/' exp              { int pos = nextfree(); $$ = pos; addInstrIII(8, pos, $1, $3); }
	| exp '^' exp              { int pos = nextfree(); $$ = pos; addInstrIII(9, pos, $1, $3); }
	| '-' exp %prec NEG        { int pos = nextfree(); $$ = pos; addInstrII(10, pos, $2); }
	| '(' exp ')'              { $$ = $2; }
	;

%%

int yylex(void) {
	int res = getNextToken(&yylval.i);
	switch (res) {
	case -2:
		return DIGIT;
	case -3:
		return FNCT;
	default:
		return res; 
	}
}

void yyerror(char const *str) {
	// will be handled handled in parse()
}

