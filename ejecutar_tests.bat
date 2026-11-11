@echo off
setlocal enabledelayedexpansion
title Pruebas Unitarias - Proyecto Integrador
cd /d "%~dp0"

echo ========================================================
echo   PROYECTO INTEGRADOR: VERIFICACION DE PRUEBAS UNITARIAS
echo ========================================================
echo.

set "JAVAC_CMD=javac"
set "JAVA_CMD=java"

if exist "%USERPROFILE%\.jdks\openjdk-26.0.2.1\bin\javac.exe" (
    set "JAVAC_CMD=%USERPROFILE%\.jdks\openjdk-26.0.2.1\bin\javac.exe"
    set "JAVA_CMD=%USERPROFILE%\.jdks\openjdk-26.0.2.1\bin\java.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

echo Usando compilador: !JAVAC_CMD!
echo.

if not exist out mkdir out

echo Compilando codigo fuente y pruebas...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$src = Get-ChildItem -Path src,test -Filter *.java -Recurse | ForEach-Object { $_.FullName }; & '!JAVAC_CMD!' -cp 'lib/junit-platform-console-standalone-1.10.2.jar' -d out $src"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] La compilacion fallo.
    pause
    exit /b %ERRORLEVEL%
)

echo [OK] Compilacion exitosa.
echo.
echo Ejecutando suite de pruebas JUnit (100%% de aprobacion requerida)...
echo.

"!JAVA_CMD!" -jar "lib/junit-platform-console-standalone-1.10.2.jar" --class-path out --scan-class-path

echo.
echo ========================================================
echo   FIN DE LA EJECUCION
echo ========================================================
pause
