#!/bin/bash

part=0
count=2

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

dir="bin/ext/prebuild"

if [ "$1" == "clean" ]; then
	rm -vf "$dir/jre-6u20-windows-i586-s.exe"
	rm -vf "$dir/apache-ant-1.8.0-bin.zip"
	
else
	# pre-build files only
	dl "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39494" \
		"$dir/jre-6u20-windows-i586-s.exe" 16529184 "71fdde020a4920f55c96e1121a1dbd4a" +x
	dl "http://mirror.netcologne.de/apache.org/ant/binaries/apache-ant-1.8.0-bin.zip" \
		"$dir/apache-ant-1.8.0-bin.zip" 12088734 "c9eaa7b72e728a40ca748ff8e1fc6869"

	echo "Finished downloading and md5-checking of files needed for 'ant build'."
fi
