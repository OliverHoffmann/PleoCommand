@ECHO OFF

SET part=0
SET count=8
SETLOCAL enabledelayedexpansion
SET "failed=0"

GOTO MAIN

:CHECK
	SET "checkres=0"
	IF NOT EXIST %1 (
		ECHO %1 does not exist
		GOTO:EOF
	)
	SET size=%~z1
	IF X%size% NEQ X%2 (
		ECHO Unexpected filesize of %1: Got %size% but should be %2
		GOTO:EOF
	)
	FOR /F "tokens=1" %%A IN ('md5sum %1') DO SET sum=%%A
	SET sum=%sum:~1%
	IF X"%sum%" NEQ X%3 (
		ECHO Unexpected md5-sum of %1: Got "%sum%" but should be %3
		GOTO:EOF
	)
	SET "checkres=1"
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
			SET "failed=1"
			ECHO "!!! Downloading failed !!!"
			GOTO:EOF
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
		EXIT 0
	)
	REM runtime
	CALL :DL "http://bci2000.org/downloads/bin/BCI2000Setup_091110.exe" ^
		"runtime\BCI2000Setup_091110.exe" 35760134 "54198c14540d012c9b403c749bc725cb" ^
		--user "ascheck" --password "eibah7cohB"
	IF %failed% == 1 EXIT 1
	CALL :DL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39485" ^
		"runtime\jre-6u20-linux-i586.bin" 21079390  "a6d7381cbca6ffcb1670f5e7eea1d41b"
	IF %failed% == 1 EXIT 1
	CALL :DL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39494" ^
		"runtime\jre-6u20-windows-i586-s.exe" 16529184 "71fdde020a4920f55c96e1121a1dbd4a"
	IF %failed% == 1 EXIT 1
	CALL :DL "http://www.dogsbodynet.com/myskit/downloads/MySkit-v1.3-Installer.exe" ^
		"runtime\MySkit-v1.3-Installer.exe" 3666219 "880a163f157d377ea8fb7bb203e3dbc0"
	IF %failed% == 1 EXIT 1
	CALL :DL "http://www.pleoworld.com/downloads/PleoSDSoftware_1.1.zip" ^
		"runtime\PleoSDSoftware_1.1.zip" 2273358 "3812d55f0d9d5c88066b9ee16793523f"
	IF %failed% == 1 EXIT 1

	REM development
	CALL :DL "http://mirror.selfnet.de/eclipse/technology/epp/downloads/release/galileo/SR2/eclipse-java-galileo-SR2-win32.zip" ^
		"development\eclipse-java-galileo-SR2-win32.zip" 97290250 "9e3048a9f26386130334134f4062232a"
	IF %failed% == 1 EXIT 1
	CALL :DL "http://www.compuphase.com/pawn/pawn-3.3.4127.package" ^
		"development\pawn-3.3.4127.package" 3523216 "0b429bf95ed11762cb5840906b622a8a"
	IF %failed% == 1 EXIT 1
	CALL :DL "http://www.pleoworld.com/downloads/PleoDevelopmentKit.zip" ^
		"development\PleoDevelopmentKit.zip" 53194900 "cfc20448351b6b13c3908f36ca40f5c5"
	IF %failed% == 1 EXIT 1

	ECHO "Finished downloading and md5-checking of all files."
	EXIT 0
