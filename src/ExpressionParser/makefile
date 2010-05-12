FLAGS_UNIX=-O2 -fPIC -DPIC -DUNIX
FLAGS_WIN=-O2 -DWIN
WARNS=-Wall -Wextra -Wdisabled-optimization \
	-Wcast-qual -Wcast-align -Wpointer-arith -Wundef -Wredundant-decls \
	-Wunused-macros -Wswitch-default -Wconversion -Wfloat-equal

build:
	echo "Use 'make unix' or 'make win'"
	exit 1

unix: exprparser libExprParser.so

win: exprparser.exe ExprParser.dll

exprparser: test-unix.o exprparser-unix.o parserrules-unix.o parserjni-unix.o
	gcc -lm -o $@ $^

exprparser.exe: test-win.o exprparser-win.o parserrules-win.o parserjni-win.o
	gcc -lm -o $@ $^

libExprParser.so: test-unix.o exprparser-unix.o parserrules-unix.o parserjni-unix.o
	gcc -shared -Wl,-soname,$@.1 -o $@ $^ -lm

ExprParser.dll: test-win.o exprparser-win.o parserrules-win.o parserjni-win.o
	gcc -shared -o $@ $^ -lm -lstdc++ -Wl,--add-stdcall-alias

test-unix.o: test.c exprparser.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

test-win.o: test.c exprparser.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS)

parserrules.c: parserrules.y exprparser.h
	bison -o $@ $<

parserrules-unix.o: parserrules.c exprparser.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

parserrules-win.o: parserrules.c exprparser.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS)

exprparser-unix.o: exprparser.c exprparser.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

exprparser-win.o: exprparser.c exprparser.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS)

parserjni-unix.o: parserjni.c parserjni.h exprparser.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

parserjni-win.o: parserjni.c parserjni.h exprparser.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS) -I . -I ./win32

clean:
	rm -rf exprparser *.so *.dll *.exe *.o parserrules.c jni.h

