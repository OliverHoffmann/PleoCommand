#!/bin/bash

part=0
count=4

function check() { # filename size hash
	[ -e "$1" ] || return 1
	size=`stat -c "%s" "$1"`
	if [ "$size" != "$2" ]; then
		echo "Unexpected filesize of $1: Got $size but should be $2"
		return 1
	fi
	sum=`md5sum -b "$1" | cut -d " " -f 1`
	if [ "$sum" != "$3" ]; then
		echo "Unexpected md5-sum of $1: Got $sum but should be $3"
		return 1
	fi
	return 0
}

function dl() {
	url="$1"
	fn="$2"
	fs="$3"
	fh="$4"
	mod="$5"
	shift 5 || shift 4
	echo $[ $part * 100 / $count ] "% - Downloading $fn"
	part=$[ $part + 1 ]
	if check "$fn" "$fs" "$fh"; then
		echo "Already downloaded: Size and checksum OK."
	else
		wget "$url" --progress=dot:mega --output-document "$fn" "$@"
		if ! check "$fn" "$fs" "$fh"; then
			echo "!!! Downloading failed !!!"
			exit 1
		fi
	fi
	if [ "$mod" ]; then
		chmod "$mod" "$fn"
	fi
	echo "======================================================================"
}

dir="ext/prebuild"
prefix="http://www.fileden.com/files/2007/4/27/1021443/"

if [ "$1" == "clean" ]; then
	rm -vf "$dir/ant-bin.zip"
	rm -vf "$dir/jdk-setup-win.part1"
	rm -vf "$dir/jdk-setup-win.part2"
	rm -vf "$dir/jdk-setup-linux.part1"
	rm -vf "$dir/jdk-setup-linux.part2"
	rm -vf "$dir/jdk-setup-win.exe"
	rm -vf "$dir/jdk-setup-linux.bin"
	
else
	mkdir -p "$dir"

	# pre-build files
	dl "$prefix/ant-bin.zip" \
		"$dir/ant-bin.zip" 12088734 "c9eaa7b72e728a40ca748ff8e1fc6869"
	dl "$prefix/jdk-setup-win.part1" \
		"$dir/jdk-setup-win.part1" 42991616 "d84296ec023605520c584a4d53a5753d"
	dl "$prefix/jdk-setup-win.part2" \
		"$dir/jdk-setup-win.part2" 37406488 "9d1617a11b175060b146e2c5236c0d09"
	dl "$prefix/jdk-setup-linux.part1" \
		"$dir/jdk-setup-linux.part1" 42991616 "e3d0692e4390b424343f5a41bf3773f7"
	dl "$prefix/jdk-setup-linux.part2" \
		"$dir/jdk-setup-linux.part2" 41805351 "ad1e7e42c056e9a4605725c5215bcd2a"

	cat "$dir/jdk-setup-win.part1" "$dir/jdk-setup-win.part2" > "$dir/jdk-setup-win.exe"
	if ! check $dir/jdk-setup-win.exe 80398104 cd336cbf94b74dacfdd519b1e1c02a3b; then
		echo "!!! Concatenating files failed !!!"
		exit 1
	fi

	cat "$dir/jdk-setup-linux.part1" "$dir/jdk-setup-linux.part2" > "$dir/jdk-setup-linux.bin"
	if ! check $dir/jdk-setup-linux.bin 84796967 ffc72e433b1ef56332610d90b947a4da; then
		echo "!!! Concatenating files failed !!!"
		exit 1
	fi

	javac -version && ant -version && {
		echo "======================================================================"
		echo "Finished downloading and md5-checking of pre-build files."
		echo "You may now call 'ant fetch' or 'ant dist'."
	} || {
		echo "======================================================================"
		echo "You have to install Java JDK and/or ant from the files in $dir."
		echo "Then invoke $0 again."
	}
fi
