NAME := PleoCommandTCPIP

all: $(NAME).exe

$(NAME).exe: $(NAME).cbproj $(NAME).cpp $(NAME)Task.cpp $(NAME)Task.h
	echo "$@ needs manual rebuilding: Select Project - Compile All Projects"
	rm -f $@
	bds -pCBuilder $< || :
	[ -f $@ ] || exit 1
