@echo off
if exist "C:\Program Files\Java\jdk-21.0.12.1" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
) else if exist "C:\Program Files\Java\jdk-25.0.4.1" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
)
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0"
echo Mengompilasi CampusFind...

set "CP=target\classes;.m2-repo\com\google\zxing\core\3.5.3\core-3.5.3.jar;.m2-repo\com\google\zxing\javase\3.5.3\javase-3.5.3.jar;.m2-repo\com\beust\jcommander\1.82\jcommander-1.82.jar;.m2-repo\com\github\jai-imageio\jai-imageio-core\1.4.0\jai-imageio-core-1.4.0.jar;.m2-repo\org\postgresql\postgresql\42.7.4\postgresql-42.7.4.jar"

if not exist "target\classes" mkdir "target\classes"

powershell -Command "Get-ChildItem -Path src\main -Filter *.java -Recurse | ForEach-Object { '\"' + ($_.FullName -replace '\\', '/') + '\"' } | Set-Content -Encoding ASCII sources.txt"
javac -encoding UTF-8 -cp "%CP%" -d target\classes @sources.txt
del sources.txt

if %errorlevel% neq 0 (
    echo Kompilasi GAGAL! Cek pesan error di atas.
    pause
    exit /b %errorlevel%
)

echo Kompilasi BERHASIL! Membuka aplikasi...
java -cp "%CP%" com.campusfind.App
pause