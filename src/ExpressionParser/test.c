#include "exprparser.h"

int main(int argc, char ** argv) {
	if (argc == 1) {
		fprintf(stderr,
		        "Syntax: %s [-v] <Expression> [<Channel 1> <Channel 2> ...]\n",
		        argv[0]);
		fprintf(stderr, "Example: %s \"25 * sin(10)\"\n", argv[0]);
		fprintf(stderr, "Example: %s \"3 + (#0 * 2)\" 100\n", argv[0]);
		fprintf(stderr, "Set -v as first argument for verbose output\n");
		return 1;
	}

	int verbose = 0, idx = 1;
	if (!strcmp(argv[1], "-v") || !strcmp(argv[1], "--verbose")) {
		verbose = 1;
		++idx;
	}
	const char *expr = argv[idx];
	instrlist *il = parse(expr);
	if (il->lastError != ERROR_SUCCESS) {
		switch (il->lastError) {
		case ERROR_SYNTAX:
			fprintf(stderr, "Syntax error");
			break;
		case ERROR_FUNCTION_UNKNOWN:
			fprintf(stderr, "Name of function unknown");
			break;
		case ERROR_SYMB_TOO_LONG:
			fprintf(stderr, "Name of symbol too long");
			break;
		default:
			fprintf(stderr, "Unknown error");
			break;
		}
		int pos = il->lastErrorPos;
		fprintf(stderr, " at '%c', position %d in '%s'\n", pos >= 0 && pos
		        < strlen(expr) ? expr[pos] : '?', pos, expr);

		freeInstrList(il);
		return 1;
	}
	if (verbose) {
		char *pc = printAll(il);
		fprintf(stderr, "%s", pc);
		free(pc);
	}

	double in[32];
	memset(in, 0, sizeof(in));
	int i;
	for (++idx, i = 0; idx < argc && i < 32; ++idx, ++i)
		in[i] = atof(argv[idx]);
	fprintf(stderr, "%f\n", execute(il, in, 32));

	freeInstrList(il);
	return 0;
}
