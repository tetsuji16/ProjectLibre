@echo off
setlocal
set "PROJECTLIBRE_OPTS="
if defined PROJECTLIBRE_JAVA_OPTS (
	set "PROJECTLIBRE_OPTS=%PROJECTLIBRE_JAVA_OPTS%"
) else if defined JAVA_OPTS (
	set "PROJECTLIBRE_OPTS=%JAVA_OPTS%"
)
java %PROJECTLIBRE_OPTS% -jar projectlibre.jar %*
