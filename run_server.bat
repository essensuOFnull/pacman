@echo off
setlocal
set JAVA_HOME=%~dp0jdk-25.0.2
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using JDK from %JAVA_HOME%
java -version
echo.
echo Building server...
call gradlew server:serverJar
echo.
echo Running server...
java -jar .\server\build\libs\server-1.0.0.jar .\levels\level1.txt
pause