@ECHO OFF

FOR /f "delims=" %a IN ('git log -n 1 --format="%h"') DO @SET rev=%a 
IF %rev%X NEQ X (
	sed "s:String VERSION_GITREV.*$:String VERSION_GITREV = ""%rev%"";:" -i "%1"
)

FOR /f "delims=" %a IN ('git log -n 1 --format="%ct"') DO @SET tstamp=%a 
IF %tstamp%X NEQ X (
	sed "s:long VERSION_DATE.*$:long VERSION_DATE = %tstamp%;:" -i "%1"
)

