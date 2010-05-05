NAME := PleoCommandTCPIP

all: $(NAME).exe

$(NAME).exe: $(NAME).cbproj $(NAME).cpp $(NAME)Task.cpp $(NAME)Task.h
	echo "$(NAME) needs manual rebuilding: Select Project - Compile All Projects"
	bds -pCBuilder $<
	exit 1
	# msbuild /t:build $<
