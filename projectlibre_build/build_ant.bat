@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
set ANT_HOME=%CD%
set PATH=%ANT_HOME%\bin;%JAVA_HOME%\bin;%PATH%

echo Building ProjectLibre with Ant...
echo JAVA_HOME=%JAVA_HOME%
echo ANT_HOME=%ANT_HOME%
echo PATH=%PATH%

java -cp "projectlibre_contrib/ant-lib/ant-contrib-1.jar;projectlibre_contrib/lib/groovy/ant-1.9.15.jar;projectlibre_contrib/lib/groovy/ant-antlr-1.9.15.jar" org.apache.tools.ant.launch.Main -buildfile build.xml dist