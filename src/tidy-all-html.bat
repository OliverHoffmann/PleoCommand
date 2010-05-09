@ECHO OFF

GOTO LOOP

:TIDYDIR
	CD %2
	FOR /F %%f IN ('dir /b *.html') DO (
		ECHO Processing %%f
		%1\tidy -c -i -m -q -u -w 120 -ashtml -utf8 %%f
	)
GOTO:EOF

:LOOP
SET tidyloc=%1
SHIFT
IF X%1 NEQ X (
	CALL :TIDYDIR %tidyloc% %1
	SHIFT
	GOTO LOOP
)

ECHO Done
