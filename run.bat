@echo off
rem =============================================================================
rem CampusFind - Windows Desktop Application Launcher
rem =============================================================================

if exist "C:\Program Files\Java\jdk-21.0.12.1" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
) else if exist "C:\Program Files\Java\jdk-25.0.4.1" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0"
echo Memulai CampusFind Desktop App (Petugas)...

rem Build CP from local .m2-repo
set "CP=target\classes;.m2-repo\com\google\zxing\core\3.5.3\core-3.5.3.jar;.m2-repo\com\google\zxing\javase\3.5.3\javase-3.5.3.jar;.m2-repo\com\beust\jcommander\1.82\jcommander-1.82.jar;.m2-repo\com\github\jai-imageio\jai-imageio-core\1.4.0\jai-imageio-core-1.4.0.jar"

if not exist "target\classes" mkdir "target\classes"

javac -cp "%CP%" -sourcepath src\main\java -d target\classes src\main\java\Main.java src\main\java\com\campusfind\App.java

java -cp "%CP%" Main
pause
