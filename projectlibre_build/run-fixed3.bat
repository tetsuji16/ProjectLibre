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

echo Compiling StartupFactory.java and Init.java...
"%JAVA_HOME%\bin\javac" --release 8 -cp "%CP%" -d "%BUILD_DIR%" "%SOURCE_DIR%\projectlibre_ui\src\com\projectlibre1\pm\graphic\frames\StartupFactory.java" 2>&1 | set ERROR1=
"%JAVA_HOME%\bin\javac" --release 8 -cp "%CP%" -d "%BUILD_DIR%" "%SOURCE_DIR%\projectlibre_core\src\com\projectlibre1\init\Init.java" 2>&1 | set ERROR2=

if defined ERROR1 (
    echo ERROR in StartupFactory.java: %ERROR1%
) else (
    echo StartupFactory.java compiled successfully
)

if defined ERROR2 (
    echo ERROR in Init.java: %ERROR2%
) else (
    echo Init.java compiled successfully
)

if not exist "%BUILD_DIR%\com\projectlibre1\pm\graphic\frames\StartupFactory.class" (
    echo Failed to compile StartupFactory.class
    goto :done
)
if not exist "%BUILD_DIR%\com\projectlibre1\init\Init.class" (
    echo Failed to compile Init.class
    goto :done
)

echo Updating JAR...
cd /d "%DIST_DIR%"
jar uf projectlibre.jar -C "%BUILD_DIR%" com/projectlibre1/pm/graphic/frames/StartupFactory.class -C "%BUILD_DIR%" com/projectlibre1/init/Init.class

echo Running ProjectLibre...
java -jar projectlibre.jar

:done