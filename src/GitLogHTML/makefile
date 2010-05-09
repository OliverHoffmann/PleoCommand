FLAGS_UNIX=-O2 -DUNIX
FLAGS_WIN=-O2 -DWIN
WARNS=-Wall -Wextra -Wdisabled-optimization \
	-Wcast-qual -Wcast-align -Wpointer-arith -Wundef -Wredundant-decls \
	-Wunused-macros -Wswitch-default -Wconversion -Wfloat-equal

build:
	echo "Use 'make unix' or 'make win'"
	exit 1

unix: GitLogHTML

win: GitLogHTML.exe

GitLogHTML: main-unix.o list-unix.o parser-unix.o printer-unix.o mingw_compat-unix.o
	gcc -o $@ $^

GitLogHTML.exe: main-win.o list-win.o parser-win.o printer-win.o mingw_compat-win.o
	gcc -o $@ $^ -liberty

main-unix.o: main.c main.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

main-win.o: main.c main.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS)

list-unix.o: list.c main.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

list-win.o: list.c main.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS)

printer-unix.o: printer.c main.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

printer-win.o: printer.c main.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS)

parser-unix.o: parser.c main.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

parser-win.o: parser.c main.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS)

mingw_compat-unix.o: mingw_compat.c main.h
	gcc -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

mingw_compat-win.o: mingw_compat.c main.h
	gcc -c -o $@ $< $(FLAGS_WIN) $(WARNS)

clean:
	rm -vf GitLogHTML GitLogHTML.exe *.o
