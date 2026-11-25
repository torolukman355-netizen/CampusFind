@echo off
rem Compile dan jalankan CampusFind tanpa Maven (butuh JDK 17+ dan zxing di ~/.m2).
cd /d "%~dp0"
set "Z=%USERPROFILE%\.m2\repository\com\google\zxing"
set "CP=%Z%\core\3.5.3\core-3.5.3.jar;%Z%\javase\3.5.3\javase-3.5.3.jar"
set "OUT=%~dp0out-javac"
if not exist "%OUT%" mkdir "%OUT%"
dir /s /b src\main\java\*.java > "%OUT%\sources.txt"
javac -encoding UTF-8 --release 17 -cp "%CP%" -d "%OUT%" @"%OUT%\sources.txt"
if errorlevel 1 (
    echo Compile gagal.
    pause
    exit /b 1
)
echo Memulai CampusFind...
java -cp "%OUT%;%CP%" com.campusfind.App
pause
