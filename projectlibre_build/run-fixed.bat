@echo off
setlocal enabledelayedexpansion
rem Compile the startup entry points with JDK 21, update dist\projectlibre.jar, and launch ProjectLibre.

set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"

for %%I in ("%~dp0..") do set "SOURCE_DIR=%%~fI"
set "BUILD_DIR=%SOURCE_DIR%\projectlibre_build\build"
set "DIST_DIR=%SOURCE_DIR%\projectlibre_build\dist"
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

if not exist "%DIST_DIR%\projectlibre.jar" (
    echo Missing "%DIST_DIR%\projectlibre.jar"
    exit /b 1
)

echo Updating projectlibre.jar...
cd /d "%DIST_DIR%"
"%JAVA_HOME%\bin\jar" uf projectlibre.jar -C "%BUILD_DIR%" com/projectlibre1/pm/graphic/frames/StartupFactory.class -C "%BUILD_DIR%" com/projectlibre1/init/Init.class
if errorlevel 1 exit /b 1

echo Running ProjectLibre...
"%JAVA_HOME%\bin\java" -XX:ReservedCodeCacheSize=256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=384m -XX:+UseG1GC -Xms256m -Xmx2048m -jar projectlibre.jar
exit /b %ERRORLEVEL%
