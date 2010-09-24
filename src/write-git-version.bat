@ECHO OFF

FOR /F "tokens=1" %%A IN ('git log -n 1 --format="%h"') DO (
	SET rev=%%A
)
IF %rev%X NEQ X (
	sed "s:String VERSION_GITREV.*$:String VERSION_GITREV = ""%rev%"";:" -i "%1"
)

FOR /F "tokens=1" %%A IN ('git log -n 1 --format="%ct"') DO (
	SET tstamp=%%A
)
IF %tstamp%X NEQ X (
	sed "s:long VERSION_DATE.*$:long VERSION_DATE = %tstamp%;:" -i "%1"
)

