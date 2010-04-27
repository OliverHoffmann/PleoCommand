#include "exprparser.h"

int main(void) {
	double in[3];
	in[0] = 20;
	in[1] = 4;
	in[2] = 14;
	instrlist *il;

	il = parse("(5 + #1) / hypot(sin(-283754 * 7.035 + #2), 12)");
	if (!il)
		return 1;
	printAll(il);
	fprintf(stderr, "execute(): %f\n", execute(il, in, 3));
	fprintf(stderr, "execute(): %f\n", execute(il, in, 3));
	freeInstrList(il);

	il = parse("(4 + #0) / 8");
	if (!il)
		return 1;
	printAll(il);
	fprintf(stderr, "execute(): %f\n", execute(il, in, 3));
	freeInstrList(il);

	freeInstrList(parse("(4 + #0)) / 8"));

	freeInstrList(parse("(4 + #0) / idontexit(8)"));

	return 0;
}
