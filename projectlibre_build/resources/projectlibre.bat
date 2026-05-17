@echo off
setlocal

rem JVM stability defaults for large Swing applications:
rem - CodeCache: prevent JIT compilation OOM (arena.cpp crashes)
rem - Metaspace: cap class metadata to avoid unbounded growth
rem - G1GC: better pause times for UI responsiveness
rem - Heap: let ergonomics decide, but set sane floor/ceiling
set "DEFAULT_JVM_OPTS=-XX:ReservedCodeCacheSize=256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=384m -XX:+UseG1GC -Xms256m -Xmx2048m"

set "PROJECTLIBRE_OPTS="
if defined PROJECTLIBRE_JAVA_OPTS (
	set "PROJECTLIBRE_OPTS=%PROJECTLIBRE_JAVA_OPTS%"
) else if defined JAVA_OPTS (
	set "PROJECTLIBRE_OPTS=%JAVA_OPTS%"
) else (
	set "PROJECTLIBRE_OPTS=%DEFAULT_JVM_OPTS%"
)
java %PROJECTLIBRE_OPTS% -jar projectlibre.jar %*
