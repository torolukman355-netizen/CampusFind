#!/bin/bash
# =============================================================================
# CampusFind - Linux Desktop Application Launcher
# Menjalankan CampusFind langsung tanpa perlu setup manual Maven/IDE
# =============================================================================

set -e
cd "$(dirname "$0")"

# 1. Cari JDK yang terpasang di sistem
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/home/bp007/.jdks/semeru-25.0.2" ]; then
        export JAVA_HOME="/home/bp007/.jdks/semeru-25.0.2"
    elif [ -d "/home/bp007/.jdks/openjdk-27" ]; then
        export JAVA_HOME="/home/bp007/.jdks/openjdk-27"
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# 2. Susun classpath dari .m2-repo lokal
CP=$(find .m2-repo -name "*.jar" | tr '\n' ':'):target/classes

echo "==============================================================="
echo "        MEMULAI CAMPUSFIND DESKTOP APP (ROLE PETUGAS)"
echo "==============================================================="

# 3. Pastikan kelas & resource terkompilasi
mkdir -p target/classes
javac -cp "$CP" -d target/classes $(find src/main/java -name "*.java")
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* target/classes/
fi

# 4. Cek Wayland/X11 display untuk lingkungan Linux GUI
if [ -z "$DISPLAY" ] && [ -n "$WAYLAND_DISPLAY" ]; then
    export DISPLAY=:0
fi

# 5. Jalankan aplikasi
echo "[OK] Menjalankan Main (Dashboard Petugas Bintang)..."
java -cp "$CP" Main "$@"
