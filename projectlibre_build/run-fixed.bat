@echo off
setlocal
set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
set PATH=%JAVA_HOME%\bin;%PATH%

set SOURCE_DIR=C:\Users\tetsu\vscode\ProjectLibre\projectlibre-code
set BUILD_DIR=%SOURCE_DIR%\projectlibre_build\build
set DIST_DIR=%SOURCE_DIR%\projectlibre_build\dist
set CONTRIB_DIR=%SOURCE_DIR%\projectlibre_contrib

echo Setting up classpath...
set CLASSPATH=%CONTRIB_DIR%\projectlibre-contrib.jar;%CONTRIB_DIR%\projectlibre-script.jar;%CONTRIB_DIR%\projectlibre-reports.jar

for %%f in ("%CONTRIB_DIR%\lib\*.jar") do (
    set CLASSPATH=!CLASSPATH!;%%f
)

for %%f in ("%CONTRIB_DIR%\lib\exchange\*.jar") do (
    set CLASSPATH=!CLASSPATH!;%%f
)

for %%f in ("%CONTRIB_DIR%\lib\groovy\*.jar") do (
    set CLASSPATH=!CLASSPATH!;%%f
)

for %%f in ("%CONTRIB_DIR%\lib\jasperreports\*.jar") do (
    set CLASSPATH=!CLASSPATH!;%%f
)

echo Classpath: %CLASSPATH%

mkdir "%BUILD_DIR%" 2>nul

echo Compiling StartupFactory.java...
"%JAVA_HOME%\bin\javac" -source 8 -target 8 -cp "%CLASSPATH%" -d "%BUILD_DIR%" "%SOURCE_DIR%\projectlibre_ui\src\com\projectlibre1\pm\graphic\frames\StartupFactory.java" 2>&1 | head -20

echo Compiling Init.java...
"%JAVA_HOME%\bin\javac" -source 8 -target 8 -cp "%CLASSPATH%" -d "%BUILD_DIR%" "%SOURCE_DIR%\projectlibre_core\src\com\projectlibre1\init\Init.java" 2>&1 | head -20

if exist "%BUILD_DIR%\com\projectlibre1\pm\graphic\frames\StartupFactory.class" (
    echo StartupFactory.class created successfully
) else (
    echo FAILED to create StartupFactory.class
)

if exist "%BUILD_DIR%\com\projectlibre1\init\Init.class" (
    echo Init.class created successfully
) else (
    echo FAILED to create Init.class
)

echo Updating JAR...
cd /d "%DIST_DIR%"
jar uf projectlibre.jar -C "%BUILD_DIR%" com/projectlibre1/pm/graphic/frames/StartupFactory.class -C "%BUILD_DIR%" com/projectlibre1/init/Init.class

echo Running ProjectLibre...
java -jar projectlibre.jar