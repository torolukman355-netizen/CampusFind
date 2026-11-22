@echo off
if exist "C:\Program Files\Java\jdk-21.0.12.1" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
) else if exist "C:\Program Files\Java\jdk-25.0.4.1" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
)
set "PATH=%JAVA_HOME%\bin;%PATH%;C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\plugins\maven-plugin\lib\maven3\bin"
cd /d "%~dp0"
echo Memulai CampusFind Desktop App...
call mvn compile exec:java
pause
