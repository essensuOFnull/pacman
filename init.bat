@echo off
setlocal

echo Setting up JDK from local folder...
set JAVA_HOME=%~dp0jdk-25.0.2
set PATH=%JAVA_HOME%\bin;%PATH%

echo Checking Java version:
java -version

echo.
set LIFTOFF_JAR=
for %%f in (gdx-liftoff-*.jar) do (
    if exist "%%f" set LIFTOFF_JAR=%%f
)

if "%LIFTOFF_JAR%"=="" (
    echo ERROR: gdx-liftoff-*.jar not found in current folder!
    echo Please download the cross-platform JAR from:
    echo https://github.com/libgdx/gdx-liftoff/releases
    echo and place it here.
    pause
    exit /b 1
)

echo Starting LibGDX Liftoff project setup...
echo.
echo Press any key to open setup GUI...
pause > nul

java -jar "%LIFTOFF_JAR%"

echo.
echo Setup completed.
echo Next steps:
echo 1. If you created project in a subfolder, move its contents to the current folder or copy jdk inside it.
echo 2. Add a 'server' module as described.
echo 3. Use .\gradlew desktop:run to test the client, .\gradlew :server:run to run server.
pause