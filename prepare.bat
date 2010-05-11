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
			PAUSE
			%myexit% 1
		)
	)
	ECHO ======================================================================
GOTO:EOF

:MAIN
	SET dir=ext\prebuild
	SET prefix=http://www.fileden.com/files/2007/4/27/1021443/

	IF X%1 == Xclean (
		DEL "%dir%\ant-bin.zip"
		DEL "%dir%\jdk-setup-win.part1"
		DEL "%dir%\jdk-setup-win.part2"
		DEL "%dir%\jdk-setup-linux.part1"
		DEL "%dir%\jdk-setup-linux.part2"
		DEL "%dir%\jdk-setup-win.exe"
		DEL "%dir%\jdk-setup-linux.bin"
		PAUSE
		%myexit% 0
	)
	MD ext
	MD %dir%

	REM pre-build files
	CALL :DL "%prefix%/ant-bin.zip" ^
		"%dir%\ant-bin.zip" 12088734 "c9eaa7b72e728a40ca748ff8e1fc6869"
	CALL :DL "%prefix%/jdk-setup-win.part1" ^
		"%dir%\jdk-setup-win.part1" 42991616 "d84296ec023605520c584a4d53a5753d"
	CALL :DL "%prefix%/jdk-setup-win.part2" ^
		"%dir%\jdk-setup-win.part2" 37406488 "9d1617a11b175060b146e2c5236c0d09"
	CALL :DL "%prefix%/jdk-setup-linux.part1" ^
		"%dir%\jdk-setup-linux.part1" 42991616 "e3d0692e4390b424343f5a41bf3773f7"
	CALL :DL "%prefix%/jdk-setup-linux.part2" ^
		"%dir%\jdk-setup-linux.part2" 41805351 "ad1e7e42c056e9a4605725c5215bcd2a"

	TYPE "%dir%\jdk-setup-win.part1" "%dir%\jdk-setup-win.part2" > "%dir%\jdk-setup-win.exe"
	CALL :CHECK %dir%\jdk-setup-win.exe 80398104 "cd336cbf94b74dacfdd519b1e1c02a3b"
	IF %checkres% == 0 (
		ECHO "!!! Concatenating files failed !!!"
		PAUSE
		%myexit% 1
	)

	TYPE "%dir%\jdk-setup-linux.part1" "%dir%\jdk-setup-linux.part2" > "%dir%\jdk-setup-linux.bin"
	CALL :CHECK %dir%\jdk-setup-linux.bin 84796967 "ffc72e433b1ef56332610d90b947a4da"
	IF %checkres% == 0 (
		ECHO "!!! Concatenating files failed !!!"
		PAUSE
		%myexit% 1
	)

	SET mustinst=0
	CALL javac -version
	IF ERRORLEVEL 1 SET mustinst=1
	CALL ant -version
	IF ERRORLEVEL 1 SET mustinst=1
	IF %mustinst% == 0 (
		ECHO ======================================================================
		ECHO Finished downloading and md5-checking of pre-build files.
		ECHO You may now call 'ant fetch' or 'ant dist'.
	) ELSE (
		ECHO ======================================================================
		ECHO You have to install Java JDK and/or ant from the files in %dir%.
		ECHO Then invoke %0 again.
	)

	PAUSE
	%myexit% 0
