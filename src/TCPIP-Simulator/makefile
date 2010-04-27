FLAGS_UNIX=-O2 -DUNIX
FLAGS_WIN=-O2 -DWIN
WARNS=-Wall -Wextra -Wdisabled-optimization \
	-Wcast-qual -Wcast-align -Wpointer-arith -Wundef -Wredundant-decls \
	-Wunused-macros -Wswitch-default -Wconversion -Wfloat-equal \
	-Wold-style-cast -Weffc++ 

build:
	echo "Use 'make unix' or 'make win'"
	exit 1

unix: TCPIPSimulator

win: TCPIPSimulator.exe

TCPIPSimulator: main-unix.o
	g++ -o $@ $^

TCPIPSimulator.exe: main-win.o
	g++ -o $@ $^ -lwsock32

main-unix.o: main.cpp
	g++ -c -o $@ $< $(FLAGS_UNIX) $(WARNS)

main-win.o: main.cpp
	g++ -c -o $@ $< $(FLAGS_WIN) $(WARNS)

clean:
	rm -vf TCPIPSimulator TCPIPSimulator.exe *.o
