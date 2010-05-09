NAME := PleoCommandTCPIP

all: $(NAME).exe

$(NAME).exe: $(NAME).cbproj $(NAME).cpp $(NAME)Task.cpp $(NAME)Task.h
	echo "$(NAME) needs manual rebuilding: Select Project - Compile All Projects"
	rm -f $(NAME).exe
	bds -pCBuilder $<
	if not exist $(NAME).exe exit 1
	rem msbuild /t:build $<
