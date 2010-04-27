#!/bin/bash

part=0
count=20

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

if [ "$1" == "clean" ]; then
	rm -vf "runtime/BCI2000Setup_091110.exe"
	rm -vf "runtime/jre-6u20-windows-i586-s.exe"
	rm -vf "runtime/MySkit-v1.3-Installer.exe"
	rm -vf "runtime/PleoSDSoftware_1.1.zip"
	rm -vf "development/jdk-6u20-windows-i586.exe"
	rm -vf "development/eclipse-java-galileo-SR2-win32.zip"
	rm -vf "development/pawn-3.3.4127.package"
	rm -vf "development/PleoDevelopmentKit.zip"
	rm -vf "development/apache-ant-1.8.0-bin.zip"
	rm -vf "development/make-3.81.exe"
	rm -vf "development/bison-2.4.1-setup.exe"
	rm -vf "development/coreutils-5.3.0.exe"
	rm -vf "development/wget-1.11.4-1-setup.exe"
	rm -vf "development/MinGW-5.1.6.exe"
	rm -vf "development/Setup-Subversion-1.6.6.msi"
	rm -vf "development/xsltproc/iconv-1.9.2.win32.zip"
	rm -vf "development/xsltproc/libxml2-2.7.6.win32.zip"
	rm -vf "development/xsltproc/libxmlsec-1.2.13.win32.zip"
	rm -vf "development/xsltproc/libxslt-1.1.26.win32.zip"
	rm -vf "development/xsltproc/zlib-1.2.3.win32.zip"
else
	# runtime
	dl "http://bci2000.org/downloads/bin/BCI2000Setup_091110.exe" \
		"runtime/BCI2000Setup_091110.exe" 35760134 "54198c14540d012c9b403c749bc725cb" +x \
		--user "ascheck" --password "eibah7cohB"
	dl "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39494" \
		"runtime/jre-6u20-windows-i586-s.exe" 16529184 "71fdde020a4920f55c96e1121a1dbd4a" +x
	dl "http://www.dogsbodynet.com/myskit/downloads/MySkit-v1.3-Installer.exe" \
		"runtime/MySkit-v1.3-Installer.exe" 3666219 "880a163f157d377ea8fb7bb203e3dbc0" +x
	dl "http://www.pleoworld.com/downloads/PleoSDSoftware_1.1.zip" \
		"runtime/PleoSDSoftware_1.1.zip" 2273358 "3812d55f0d9d5c88066b9ee16793523f"

	# development
	dl "http://pearl.plunder.com/x/\$1OGj-51GptNM3Uy7aA0CbyFGQfPF0yYW/dd68920451/?/jdk-6u20-windows-i586.exe" \
		"development/jdk-6u20-windows-i586.exe" 80398104 "cd336cbf94b74dacfdd519b1e1c02a3b"
	dl "http://mirror.netcologne.de/apache.org/ant/binaries/apache-ant-1.8.0-bin.zip" \
		"development/apache-ant-1.8.0-bin.zip" 12088734 "c9eaa7b72e728a40ca748ff8e1fc6869"
	dl "http://heanet.dl.sourceforge.net/project/gnuwin32/make/3.81/make-3.81.exe" \
		"development/make-3.81.exe" 3384653 "8ae51379d1f3eef8360df4e674f17d6d"
	dl "http://garr.dl.sourceforge.net/project/gnuwin32/bison/2.4.1/bison-2.4.1-setup.exe" \
		"development/bison-2.4.1-setup.exe" 3840350 "d24ed4f8a3b156899db96079fb869cbf"
	dl "http://ignum.dl.sourceforge.net/project/gnuwin32/coreutils/5.3.0/coreutils-5.3.0.exe" \
		"development/coreutils-5.3.0.exe" 6439882 "5a3e9d30b906dadf54de0635522fd62c" +x
	dl "http://surfnet.dl.sourceforge.net/project/gnuwin32/wget/1.11.4-1/wget-1.11.4-1-setup.exe" \
		"development/wget-1.11.4-1-setup.exe" 3012464 "b4679ac6f7757b35435ec711c6c8d912" +x
	dl "http://downloads.sourceforge.net/project/mingw/Automated%20MinGW%20Installer/MinGW%205.1.6/MinGW-5.1.6.exe" \
		"development/MinGW-5.1.6.exe" 158842 "9cf4ab0b4c9f858d32f5d5c89009c4dc" +x
	dl "http://subversion.tigris.org/files/documents/15/46906/Setup-Subversion-1.6.6.msi" \
		"development/Setup-Subversion-1.6.6.msi" 4834672 "379fbeb45067f98ebd830affd4afdcc1" +x
	dl "ftp://ftp.zlatkovic.com/libxml/iconv-1.9.2.win32.zip" \
		"development/xsltproc/iconv-1.9.2.win32.zip" 1320616 "da5f3164bbd21e7830faef7e5a7ea5e6"
	dl "ftp://ftp.zlatkovic.com/libxml/libxml2-2.7.6.win32.zip" \
		"development/xsltproc/libxml2-2.7.6.win32.zip" 2731818 "d7b1f133b286751cdf5a50f39dbb6ddf"
	dl "ftp://ftp.zlatkovic.com/libxml/libxmlsec-1.2.13.win32.zip" \
		"development/xsltproc/libxmlsec-1.2.13.win32.zip" 954228 "c6bafb19bb02eb733f7f3f672e736766"
	dl "ftp://ftp.zlatkovic.com/libxml/libxslt-1.1.26.win32.zip" \
		"development/xsltproc/libxslt-1.1.26.win32.zip" 408001 "36fb352ea5b6309305476bcb8cabc31e"
	dl "ftp://ftp.zlatkovic.com/libxml/zlib-1.2.3.win32.zip" \
		"development/xsltproc/zlib-1.2.3.win32.zip" 126580 "61f7b91539b0532eea3c30e3281dc424"
	dl "http://mirror.selfnet.de/eclipse/technology/epp/downloads/release/galileo/SR2/eclipse-java-galileo-SR2-win32.zip" \
		"development/eclipse-java-galileo-SR2-win32.zip" 97290250 "9e3048a9f26386130334134f4062232a"
	dl "http://www.compuphase.com/pawn/pawn-3.3.4127.package" \
		"development/pawn-3.3.4127.package" 3523216 "0b429bf95ed11762cb5840906b622a8a" +x
	dl "http://www.pleoworld.com/downloads/PleoDevelopmentKit.zip" \
		"development/PleoDevelopmentKit.zip" 53194900 "cfc20448351b6b13c3908f36ca40f5c5"

	echo "Finished downloading and md5-checking of all files."
fi
