#!/bin/bash

part=0
count=8

function check() { # filename size hash
	[ -e "$1" ] || return 1
	size=`stat -c "%s" "$1"`
	if [ "$size" != "$2" ]; then
		echo "Unexpected filesize of $1: Got $size but should be $2"
		return 1
	fi
	sum=`md5sum "$1" | cut -d " " -f 1`
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

if [ "$1" == "clean" ]; then
	rm -vf "runtime/BCI2000Setup_091110.exe"
	rm -vf "runtime/jre-6u20-linux-i586.bin"
	rm -vf "runtime/jre-6u20-windows-i586-s.exe"
	rm -vf "runtime/MySkit-v1.3-Installer.exe"
	rm -vf "runtime/PleoSDSoftware_1.1.zip"
	rm -vf "development/eclipse-java-galileo-SR2-win32.zip"
	rm -vf "development/pawn-3.3.4127.package"
	rm -vf "development/PleoDevelopmentKit.zip"
else
	#runtime
	dl "http://bci2000.org/downloads/bin/BCI2000Setup_091110.exe" \
		"runtime/BCI2000Setup_091110.exe" 35760134 "54198c14540d012c9b403c749bc725cb" +x \
		--user "ascheck" --password "eibah7cohB"
	dl "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39485" \
		"runtime/jre-6u20-linux-i586.bin" 21079390  "a6d7381cbca6ffcb1670f5e7eea1d41b" +x
	dl "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39494" \
		"runtime/jre-6u20-windows-i586-s.exe" 16529184 "71fdde020a4920f55c96e1121a1dbd4a" +x
	dl "http://www.dogsbodynet.com/myskit/downloads/MySkit-v1.3-Installer.exe" \
		"runtime/MySkit-v1.3-Installer.exe" 3666219 "880a163f157d377ea8fb7bb203e3dbc0" +x
	dl "http://www.pleoworld.com/downloads/PleoSDSoftware_1.1.zip" \
		"runtime/PleoSDSoftware_1.1.zip" 2273358 "3812d55f0d9d5c88066b9ee16793523f"

	# development
	dl "http://mirror.selfnet.de/eclipse/technology/epp/downloads/release/galileo/SR2/eclipse-java-galileo-SR2-win32.zip" \
		"development/eclipse-java-galileo-SR2-win32.zip" 97290250 "9e3048a9f26386130334134f4062232a"
	dl "http://www.compuphase.com/pawn/pawn-3.3.4127.package" \
		"development/pawn-3.3.4127.package" 3523216 "0b429bf95ed11762cb5840906b622a8a" +x
	dl "http://www.pleoworld.com/downloads/PleoDevelopmentKit.zip" \
		"development/PleoDevelopmentKit.zip" 53194900 "cfc20448351b6b13c3908f36ca40f5c5"

	echo "Finished downloading and md5-checking of all files."
fi
