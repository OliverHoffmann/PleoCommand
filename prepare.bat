@ECHO OFF

SETLOCAL enabledelayedexpansion

SET part=0
SET count=20

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
	SET dir=bin\ext\prebuild

	IF X%1 == Xclean (
		DEL "%dir%\jre-6u20-windows-i586-s.exe"
		DEL "%dir%\apache-ant-1.8.0-bin.zip"
		%myexit% 0
	)
	REM pre-build files only
	CALL :DL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=39494" ^
		"%dir%\jre-6u20-windows-i586-s.exe" 16529184 "71fdde020a4920f55c96e1121a1dbd4a"
	CALL :DL "http://mirror.netcologne.de/apache.org/ant/binaries/apache-ant-1.8.0-bin.zip" ^
		"%dir%\apache-ant-1.8.0-bin.zip" 12088734 "c9eaa7b72e728a40ca748ff8e1fc6869"

	SET mustinst=0
	CALL java -version
	IF ERRORLEVEL 1 SET mustinst=1
	CALL ant -version
	IF ERRORLEVEL 1 SET mustinst=1
	IF %mustinst% == 0 (
		ECHO ======================================================================
		ECHO Finished downloading and md5-checking of pre-build files.
		ECHO You may now call 'ant dist'.
	) ELSE (
		ECHO ======================================================================
		ECHO You have to install java and/or ant from %dir%.
		ECHO Then invoke %0 again.
	)
	
	%myexit% 0
