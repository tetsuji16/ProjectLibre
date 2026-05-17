@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
set ANT_HOME=C:\Users\tetsu\apache-ant\apache-ant-1.10.15
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%PATH%
cd /d "C:\Users\tetsu\vscode\ProjectLibre\projectlibre-code\projectlibre_build"
echo Running ant...
ant dist
