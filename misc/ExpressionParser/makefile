FLAGS=-O2 -fPIC -DPIC
WARNS=-Wall -Wextra -Wdisabled-optimization \
	-Wcast-qual -Wcast-align -Wpointer-arith -Wundef -Wredundant-decls \
	-Wunused-macros -Wswitch-default -Wconversion -Wfloat-equal \
	-Wold-style-cast -Weffc++ 

build:
	echo "Use 'make unix' or 'make win'"
	exit 1

unix: exprparser libExprParser.so

win: exprparser.exe ExprParser.dll

exprparser: test.o exprparser.o parserrules.o parserjni.o
	gcc -lm -o $@ $^

exprparser.exe: test.o exprparser.o parserrules.o parserjni.o
	gcc -lm -o $@ $^

libExprParser.so: test.o exprparser.o parserrules.o parserjni.o
	gcc -shared -Wl,-soname,$@.1 -o $@ $^ -lm

ExprParser.dll: test.o exprparser.o parserrules.o parserjni.o
	gcc -shared -Wl,-soname,$@.1 -o $@ $^ -lm -lstdc++ -Wl,--add-stdcall-alias

test.o: test.c exprparser.h
	gcc -c -o $@ $< $(FLAGS)

parserrules.c: parserrules.y exprparser.h
	bison -o $@ $<

parserrules.o: parserrules.c exprparser.h
	gcc -c -o $@ $< $(FLAGS)

exprparser.o: exprparser.c exprparser.h
	gcc -c -o $@ $< $(FLAGS)

parserjni.o: parserjni.c parserjni.h exprparser.h
	gcc -c -o $@ $< $(FLAGS)

clean:
	rm -rf exprparser *.so *.dll *.o parserrules.c

test: all
	echo "(5 + #1) / hypot(sin(-283754 * 7.035 + #2), 12)" | ./exprparser
