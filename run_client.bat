@echo off
setlocal
set JAVA_HOME=%~dp0jdk-25.0.2
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using JDK from %JAVA_HOME%
java -version
echo.
echo Building client...
call gradlew lwjgl3:dist
echo.
echo Running client...
java -jar lwjgl3\build\libs\lwjgl3-*.jar
pause