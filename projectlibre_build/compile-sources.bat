@echo off
setlocal
set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "C:\Users\tetsu\vscode\ProjectLibre\projectlibre-code"

echo Compiling modified source files...

set CLASSPATH=projectlibre_contrib\projectlibre-contrib.jar;projectlibre_contrib\projectlibre-script.jar;projectlibre_contrib\projectlibre-reports.jar

echo Compiling StartupFactory.java...
"%JAVA_HOME%\bin\javac" -cp "%CLASSPATH%" -d projectlibre_build\build projectlibre_ui\src\com\projectlibre1\pm\graphic\frames\StartupFactory.java
if errorlevel 1 echo Failed to compile StartupFactory.java

echo Compiling Init.java...
"%JAVA_HOME%\bin\javac" -cp "%CLASSPATH%" -d projectlibre_build\build projectlibre_core\src\com\projectlibre1\init\Init.java
if errorlevel 1 echo Failed to compile Init.java

echo Done compiling modified files.