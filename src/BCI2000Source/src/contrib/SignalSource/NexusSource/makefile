NAME := NexusSource

all: $(NAME).exe

$(NAME).exe: $(NAME).cbproj $(NAME).cpp $(NAME)ADC.cpp $(NAME)ADC.h NexusEmulation.cpp NexusEmulation.h
	echo "$(NAME) needs manual rebuilding: Select Project - Compile All Projects"
	bds -pCBuilder $<
	exit 1
	# msbuild /t:build $<
