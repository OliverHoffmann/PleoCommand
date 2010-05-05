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

if [ "$1" == "clean" ]; then
	rm -vf "$dir/jdk-setup.bin"
	rm -vf "$dir/jdk-setup.exe"
	rm -vf "$dir/ant-bin.zip"
	
else
	mkdir -p "ext/rt"
	mkdir -p "$dir"

	# pre-build files
	dl "http://mirror.netcologne.de/apache.org/ant/binaries/apache-ant-1.8.0-bin.zip" \
		"$dir/ant-bin.zip" 12088734 "c9eaa7b72e728a40ca748ff8e1fc6869"
	dl "http://www.java.net/download/jdk6/6u21/promoted/b02/binaries/jdk-6u20-ea-bin-b02-linux-i586-01_apr_2010.bin" \
		"$dir/jdk-setup.bin" 85095082 "3167dd9663469022d937eecde22fd568" +x
	dl "http://www.java.net/download/jdk6/6u21/promoted/b02/binaries/jdk-6u20-ea-bin-b02-windows-i586-01_apr_2010.exe" \
		"$dir/jdk-setup.exe" 80701208 "385ebd92bd83c2bd04fdd787c352487a" +x

	java -version && ant -version && {
		echo "======================================================================"
		echo "Finished downloading and md5-checking of pre-build files."
		echo "You may now call 'ant fetch' or 'ant dist'."
	} || {
		echo "======================================================================"
		echo "You have to install java and/or ant from $dir."
		echo "Then invoke $0 again."
	}
fi
