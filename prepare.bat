@ECHO OFF

SETLOCAL enabledelayedexpansion

SET part=0
SET count=4

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
	FOR /F "tokens=1" %%A IN ('src\ext\md5sum -b %1') DO SET sum=%%A
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
		src\ext\wget %url% --progress=dot:mega --output-document %fn% %5 %6 %7 %8 %9
		CALL :CHECK %fn% %fs% %fh%
		IF !checkres! == 0 (
			ECHO "!!! Downloading failed !!!"
			%myexit% 1
		)
	)
	ECHO ======================================================================
GOTO:EOF

:MAIN
	SET dir=ext\prebuild

	IF X%1 == Xclean (
		DEL "%dir%\jdk-setup.bin"
		DEL "%dir%\jdk-setup.exe"
		DEL "%dir%\ant-bin.zip"
		%myexit% 0
	)
	MD ext
	MD ext\rt
	MD %dir%

	REM pre-build files
	CALL :DL "http://mirror.netcologne.de/apache.org/ant/binaries/apache-ant-1.8.0-bin.zip" ^
		"$dir/ant-bin.zip" 12088734 "c9eaa7b72e728a40ca748ff8e1fc6869"
	CALL :DL "http://www.java.net/download/jdk6/6u21/promoted/b02/binaries/jdk-6u20-ea-bin-b02-linux-i586-01_apr_2010.bin" ^
		"$dir/jdk-setup.bin" 85095082 "3167dd9663469022d937eecde22fd568"
	CALL :DL "http://www.java.net/download/jdk6/6u21/promoted/b02/binaries/jdk-6u20-ea-bin-b02-windows-i586-01_apr_2010.exe" ^
		"$dir/jdk-setup.exe" 80701208 "385ebd92bd83c2bd04fdd787c352487a"
		
	REM need to use wget because ant's downloader can't handle the URL
	CALL :DL "http://bci2000.org/downloads/bin/BCI2000Setup_091110.exe" ^
		"ext/rt/BCI2000-setup.exe" 35760134 "54198c14540d012c9b403c749bc725cb" ^
		--user "ascheck" --password "eibah7cohB"

	SET mustinst=0
	CALL java -version
	IF ERRORLEVEL 1 SET mustinst=1
	CALL ant -version
	IF ERRORLEVEL 1 SET mustinst=1
	IF %mustinst% == 0 (
		ECHO ======================================================================
		ECHO Finished downloading and md5-checking of pre-build files.
		ECHO You may now call 'ant fetch' or 'ant dist'.
	) ELSE (
		ECHO ======================================================================
		ECHO You have to install java and/or ant from %dir%.
		ECHO Then invoke %0 again.
	)
	
	%myexit% 0
