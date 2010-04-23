build:
	echo "Use 'make unix' or 'make win'"
	exit 1

unix: TCPIPSimulator-unix

win: TCPIPSimulator-win

TCPIPSimulator-unix: main.cpp
	g++ -O2 main.cpp -o TCPIPSimulator -Wall -Wextra -Wdisabled-optimization \
	-Wcast-qual -Wcast-align -Wpointer-arith -Wundef -Wredundant-decls \
	-Wunused-macros -Wswitch-default -Wconversion -Wfloat-equal \
	-Wold-style-cast -Weffc++ -DUNIX

TCPIPSimulator-win: main.cpp
	g++ -O2 main.cpp -o TCPIPSimulator -Wall -Wextra -Wdisabled-optimization \
	-Wcast-qual -Wcast-align -Wpointer-arith -Wundef -Wredundant-decls \
	-Wunused-macros -Wswitch-default -Wconversion -Wfloat-equal \
	-Wold-style-cast -Weffc++ -lwsock32 -DWIN32

clean:
	rm -vf TCPIPSimulator
