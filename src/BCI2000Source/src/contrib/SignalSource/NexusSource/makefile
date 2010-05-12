NAME := NexusSource

all: $(NAME).exe

$(NAME).exe: $(NAME).cbproj $(NAME).cpp $(NAME)ADC.cpp $(NAME)ADC.h NexusEmulation.cpp NexusEmulation.h
	echo "$@ needs manual rebuilding: Select Project - Compile All Projects"
	rm -f $@
	bds -pCBuilder $< || :
	[ -f $@ ] || exit 1
