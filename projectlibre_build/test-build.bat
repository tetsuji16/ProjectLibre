@echo off
setlocal enabledelayedexpansion
rem CI-style compile check for the startup entry points. Exits nonzero on failure.

set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"

for %%I in ("%~dp0..") do set "SOURCE_DIR=%%~fI"
set "BUILD_DIR=%SOURCE_DIR%\projectlibre_build\build"
set "CONTRIB_DIR=%SOURCE_DIR%\projectlibre_contrib"
set "SOURCEPATH=%SOURCE_DIR%\projectlibre_ui\src;%SOURCE_DIR%\projectlibre_core\src;%SOURCE_DIR%\projectlibre_exchange\src;%SOURCE_DIR%\projectlibre_reports\src;%SOURCE_DIR%\projectlibre_contrib\src"

echo Building classpath...
set "CP=%CONTRIB_DIR%\projectlibre-contrib.jar;%CONTRIB_DIR%\projectlibre-script.jar;%CONTRIB_DIR%\projectlibre-reports.jar"
for %%f in ("%CONTRIB_DIR%\lib\*.jar") do set "CP=!CP!;%%~ff"
for %%f in ("%CONTRIB_DIR%\lib\exchange\*.jar") do set "CP=!CP!;%%~ff"
for %%f in ("%CONTRIB_DIR%\lib\groovy\*.jar") do set "CP=!CP!;%%~ff"
for %%f in ("%CONTRIB_DIR%\lib\jasperreports\*.jar") do set "CP=!CP!;%%~ff"

mkdir "%BUILD_DIR%" 2>nul

echo Compiling StartupFactory.java with Java 21...
"%JAVA_HOME%\bin\javac" --release 21 -cp "%CP%" -sourcepath "%SOURCEPATH%" -d "%BUILD_DIR%" "%SOURCE_DIR%\projectlibre_ui\src\com\projectlibre1\pm\graphic\frames\StartupFactory.java"
if errorlevel 1 exit /b 1

echo Compiling Init.java with Java 21...
"%JAVA_HOME%\bin\javac" --release 21 -cp "%CP%" -sourcepath "%SOURCEPATH%" -d "%BUILD_DIR%" "%SOURCE_DIR%\projectlibre_core\src\com\projectlibre1\init\Init.java"
if errorlevel 1 exit /b 1

if not exist "%BUILD_DIR%\com\projectlibre1\pm\graphic\frames\StartupFactory.class" exit /b 1
if not exist "%BUILD_DIR%\com\projectlibre1\init\Init.class" exit /b 1

echo Startup entry point compile check passed.
exit /b 0
