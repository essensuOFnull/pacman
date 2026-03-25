@echo off
setlocal

echo Current directory: %CD%
echo Setting up JDK...
set JAVA_HOME=%~dp0jdk-25.0.2
set PATH=%JAVA_HOME%\bin;%PATH%

echo Java version:
java -version
echo.

echo Looking for gradlew...
if exist gradlew.bat (
    echo gradlew.bat found in root.
    set GRADLE_CMD=gradlew.bat
) else if exist client\gradlew.bat (
    echo gradlew.bat found in client folder.
    cd client
    set GRADLE_CMD=gradlew.bat
) else (
    echo ERROR: gradlew.bat not found!
    pause
    exit /b 1
)

echo Running gradlew tasks...
%GRADLE_CMD% tasks
echo Gradlew finished with exit code: %ERRORLEVEL%

echo.
pause