@ECHO OFF

SETLOCAL enabledelayedexpansion

SET part=0
SET count=18

SET myexit=EXIT /B
if X%1 == Xfrom-ant (
	SHIFT
	SET myexit=EXIT
)

GOTO MAIN

:CHECK
	SET checkres=0
	IF NOT EXIST %1 (
		ECHO %1 does not exist
		GOTO:EOF
	)
	SET size=%~z1
	IF X%size% NEQ X%2 (
		ECHO Unexpected filesize of %1: Got %size% but should be %2
		GOTO:EOF
	)
	FOR /F "tokens=1" %%A IN ('md5sum -b %1') DO SET sum=%%A
	IF X%sum:~0,1% == X\ SET sum=%sum:~1%
	IF X%sum:~0,1% == X* SET sum=%sum:~1%
	IF X"%sum%" NEQ X%3 (
		ECHO Unexpected md5-sum of %1: Got "%sum%" but should be %3
		GOTO:EOF
	)
	SET checkres=1
GOTO:EOF

:DL
	SET url=%1
	SET fn=%2
	SET fs=%3
	SET fh=%4
	SET /A "perc=%part%*100/%count%"
	ECHO %perc%%% - Downloading %fn%
	SET /A "part=%part%+1"
	CALL :CHECK %fn% %fs% %fh%
	IF %checkres% == 1 (
		ECHO Already downloaded: Size and checksum OK.
	) ELSE (
		wget %url% --progress=dot:mega --output-document %fn% %5 %6 %7 %8 %9
		CALL :CHECK %fn% %fs% %fh%
		IF !checkres! == 0 (
			ECHO "!!! Downloading failed !!!"
			%myexit% 1
		)
	)
	ECHO ======================================================================
GOTO:EOF

:MAIN
	IF X%1 == Xclean (
		DEL "runtime\BCI2000Setup_091110.exe" 
		DEL "runtime\jre-6u20-linux-i586.bin"
		DEL "runtime\jre-6u20-windows-i586-s.exe"
		DEL "runtime\MySkit-v1.3-Installer.exe"
		DEL "runtime\PleoSDSoftware_1.1.zip"
		DEL "development\eclipse-java-galileo-SR2-win32.zip"
		DEL "development\pawn-3.3.4127.package"
		DEL "development\PleoDevelopmentKit.zip"
		DEL "development\apache-ant-1.8.0-bin.zip"
		DEL "development\coreutils-5.3.0.exe"
		DEL "development\wget-1.11.4-1-setup.exe"
		DEL "development\MinGW-5.1.6.exe"
		DEL "development\Setup-Subversion-1.6.6.msi"
		DEL "development\xsltproc\iconv-1.9.2.win32.zip"
		DEL "development\xsltproc\libxml2-2.7.6.win32.zip"
		DEL "development\xsltproc\libxmlsec-1.2.13.win32.zip"
		DEL "development\xsltproc\libxslt-1.1.26.win32.zip"
		DEL "development\xsltproc\zlib-1.2.3.win32.zip"
		%myexit% 0
	)
	REM runtime
	CALL :DL "http://bci2000.org/downloads/bin/BCI2000Setup_091110.exe" ^
		"runtime\BCI2000Setup_091110.exe" 35760134 "54198c14540d012c9b403c749bc725cb" ^
		--user "ascheck" --password "eibah7cohB"
	CALL :DL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39485" ^
		"runtime\jre-6u20-linux-i586.bin" 21079390  "a6d7381cbca6ffcb1670f5e7eea1d41b"
	CALL :DL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39494" ^
		"runtime\jre-6u20-windows-i586-s.exe" 16529184 "71fdde020a4920f55c96e1121a1dbd4a"
	CALL :DL "http://www.dogsbodynet.com/myskit/downloads/MySkit-v1.3-Installer.exe" ^
		"runtime\MySkit-v1.3-Installer.exe" 3666219 "880a163f157d377ea8fb7bb203e3dbc0"
	CALL :DL "http://www.pleoworld.com/downloads/PleoSDSoftware_1.1.zip" ^
		"runtime\PleoSDSoftware_1.1.zip" 2273358 "3812d55f0d9d5c88066b9ee16793523f"

	REM development
	CALL :DL "http://mirror.netcologne.de/apache.org/ant/binaries/apache-ant-1.8.0-bin.zip" ^
		"development/apache-ant-1.8.0-bin.zip" 12088734 "c9eaa7b72e728a40ca748ff8e1fc6869"
	CALL :DL "http://ignum.dl.sourceforge.net/project/gnuwin32/coreutils/5.3.0/coreutils-5.3.0.exe" ^
		"development/coreutils-5.3.0.exe" 6439882 "5a3e9d30b906dadf54de0635522fd62c"
	CALL :DL "http://surfnet.dl.sourceforge.net/project/gnuwin32/wget/1.11.4-1/wget-1.11.4-1-setup.exe" ^
		"development/wget-1.11.4-1-setup.exe" 3012464 "b4679ac6f7757b35435ec711c6c8d912"
	CALL :DL "http://downloads.sourceforge.net/project/mingw/Automated%%%%20MinGW%%%%20Installer/MinGW%%%%205.1.6/MinGW-5.1.6.exe" ^
		"development/MinGW-5.1.6.exe" 158842 "9cf4ab0b4c9f858d32f5d5c89009c4dc"
	CALL :DL "http://subversion.tigris.org/files/documents/15/46906/Setup-Subversion-1.6.6.msi" ^
		"development/Setup-Subversion-1.6.6.msi" 4834672 "379fbeb45067f98ebd830affd4afdcc1"
	CALL :DL "ftp://ftp.zlatkovic.com/libxml/iconv-1.9.2.win32.zip" ^
		"development/xsltproc/iconv-1.9.2.win32.zip" 1320616 "da5f3164bbd21e7830faef7e5a7ea5e6"
	CALL :DL "ftp://ftp.zlatkovic.com/libxml/libxml2-2.7.6.win32.zip" ^
		"development/xsltproc/libxml2-2.7.6.win32.zip" 2731818 "d7b1f133b286751cdf5a50f39dbb6ddf"
	CALL :DL "ftp://ftp.zlatkovic.com/libxml/libxmlsec-1.2.13.win32.zip" ^
		"development/xsltproc/libxmlsec-1.2.13.win32.zip" 954228 "c6bafb19bb02eb733f7f3f672e736766"
	CALL :DL "ftp://ftp.zlatkovic.com/libxml/libxslt-1.1.26.win32.zip" ^
		"development/xsltproc/libxslt-1.1.26.win32.zip" 408001 "36fb352ea5b6309305476bcb8cabc31e"
	CALL :DL "ftp://ftp.zlatkovic.com/libxml/zlib-1.2.3.win32.zip" ^
		"development/xsltproc/zlib-1.2.3.win32.zip" 126580 "61f7b91539b0532eea3c30e3281dc424"
	CALL :DL "http://mirror.selfnet.de/eclipse/technology/epp/downloads/release/galileo/SR2/eclipse-java-galileo-SR2-win32.zip" ^
		"development\eclipse-java-galileo-SR2-win32.zip" 97290250 "9e3048a9f26386130334134f4062232a"
	CALL :DL "http://www.compuphase.com/pawn/pawn-3.3.4127.package" ^
		"development\pawn-3.3.4127.package" 3523216 "0b429bf95ed11762cb5840906b622a8a"
	CALL :DL "http://www.pleoworld.com/downloads/PleoDevelopmentKit.zip" ^
		"development\PleoDevelopmentKit.zip" 53194900 "cfc20448351b6b13c3908f36ca40f5c5"

	ECHO "Finished downloading and md5-checking of all files."
	%myexit% 0
