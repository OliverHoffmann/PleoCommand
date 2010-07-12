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

%token <i> FDD FDDD FDID
%token <i> DIGIT
%token <i> LTEQ GTEQ EQU NEQ AND OR
%type <i> exp
%type <s> nrint
%type <d> nrdbl

%left ','
%left OR
%left AND
%left EQU NEQ
%left '<' '>' LTEQ GTEQ
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
	nrint '.' nrint { $$ = (double) $1.val + (double) $3.val / pow(10.0, $3.cnt); }
	;

exp:
	nrint                      { int pos = nextfree(); $$ = pos; addInstrII(0, pos, $1.val); }
	| nrdbl                    { int pos = nextfree(); $$ = pos; addInstrID(1, pos, $1); }
	| '#' nrint                { int pos = nextfree(); $$ = pos; addInstrII(2, pos, $2.val); }
	| FDD '(' exp ')'          { int pos = nextfree(); $$ = pos; addInstrIII(3, pos, $1, $3); }
	| FDDD '(' exp ',' exp ')' { int pos = nextfree(); $$ = pos; addInstrIIII(4, pos, $1, $3, $5); }
	| exp '+' exp              { int pos = nextfree(); $$ = pos; addInstrIII(5, pos, $1, $3); }
	| exp '-' exp              { int pos = nextfree(); $$ = pos; addInstrIII(6, pos, $1, $3); }
	| exp '*' exp              { int pos = nextfree(); $$ = pos; addInstrIII(7, pos, $1, $3); }
	| exp '/' exp              { int pos = nextfree(); $$ = pos; addInstrIII(8, pos, $1, $3); }
	| exp '^' exp              { int pos = nextfree(); $$ = pos; addInstrIII(9, pos, $1, $3); }
	| '-' exp %prec NEG        { int pos = nextfree(); $$ = pos; addInstrII(10, pos, $2); }
	| exp LTEQ exp             { int pos = nextfree(); $$ = pos; addInstrIII(12, pos, $1, $3); } // 11 is RET
	| exp GTEQ exp             { int pos = nextfree(); $$ = pos; addInstrIII(13, pos, $1, $3); }
	| exp '<' exp              { int pos = nextfree(); $$ = pos; addInstrIII(14, pos, $1, $3); }
	| exp '>' exp              { int pos = nextfree(); $$ = pos; addInstrIII(15, pos, $1, $3); }
	| exp AND exp              { int pos = nextfree(); $$ = pos; addInstrIII(16, pos, $1, $3); }
	| exp OR exp               { int pos = nextfree(); $$ = pos; addInstrIII(17, pos, $1, $3); }
	| exp EQU exp              { int pos = nextfree(); $$ = pos; addInstrIII(18, pos, $1, $3); }
	| exp NEQ exp              { int pos = nextfree(); $$ = pos; addInstrIII(19, pos, $1, $3); }
	| FDID '(' exp ',' exp ')' { int pos = nextfree(); $$ = pos; addInstrIIII(20, pos, $1, $3, $5); }
	| '(' exp ')'              { $$ = $2; }
	;

%%

/*
int yylex(void) {
	return getNextToken(&yylval.i);
}
*/

void yyerror(char const *str __attribute__ ((unused))) {
	// will be handled handled in parse()
}

