@echo off
setlocal enabledelayedexpansion
set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
set PATH=%JAVA_HOME%\bin;%PATH%

set SOURCE_DIR=C:\Users\tetsu\vscode\ProjectLibre\projectlibre-code
set BUILD_DIR=%SOURCE_DIR%\projectlibre_build\build
set DIST_DIR=%SOURCE_DIR%\projectlibre_build\dist
set CONTRIB_DIR=%SOURCE_DIR%\projectlibre_contrib

echo Building classpath...
set CP=%CONTRIB_DIR%\projectlibre-contrib.jar;%CONTRIB_DIR%\projectlibre-script.jar;%CONTRIB_DIR%\projectlibre-reports.jar

for %%f in ("%CONTRIB_DIR%\lib\*.jar") do (
    set CP=!CP!;%%f
)

for %%f in ("%CONTRIB_DIR%\lib\exchange\*.jar") do (
    set CP=!CP!;%%f
)

for %%f in ("%CONTRIB_DIR%\lib\groovy\*.jar") do (
    set CP=!CP!;%%f
)

for %%f in ("%CONTRIB_DIR%\lib\jasperreports\*.jar") do (
    set CP=!CP!;%%f
)

mkdir "%BUILD_DIR%" 2>nul

echo Compiling StartupFactory.java...
"%JAVA_HOME%\bin\javac" --release 8 -cp "%CP%" -d "%BUILD_DIR%" "%SOURCE_DIR%\projectlibre_ui\src\com\projectlibre1\pm\graphic\frames\StartupFactory.java" 2>&1 | head -1
echo Compiling Init.java...
"%JAVA_HOME%\bin\javac" --release 8 -cp "%CP%" -d "%BUILD_DIR%" "%SOURCE_DIR%\projectlibre_core\src\com\projectlibre1\init\Init.java" 2>&1 | head -1

echo Updating JAR...
cd /d "%DIST_DIR%"
jar uf projectlibre.jar -C "%BUILD_DIR%" com/projectlibre1/pm/graphic/frames/StartupFactory.class -C "%BUILD_DIR%" com/projectlibre1/init/Init.class

echo Running ProjectLibre...
java -jar projectlibre.jar