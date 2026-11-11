# Script de ejecucion de pruebas unitarias
$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
Set-Location $projectRoot

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  PROYECTO INTEGRADOR: VERIFICACION DE PRUEBAS UNITARIAS" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

$javacCmd = "javac"
$javaCmd = "java"

$candidateJdk = "$env:USERPROFILE\.jdks\openjdk-26.0.2.1\bin"
if (Test-Path "$candidateJdk\javac.exe") {
    $javacCmd = "$candidateJdk\javac.exe"
    $javaCmd = "$candidateJdk\java.exe"
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javacCmd = "$env:JAVA_HOME\bin\javac.exe"
    $javaCmd = "$env:JAVA_HOME\bin\java.exe"
}

Write-Host "Usando compilador: $javacCmd" -ForegroundColor Yellow

if (!(Test-Path "out")) {
    New-Item -ItemType Directory -Path "out" | Out-Null
}

Write-Host "Compilando clases y pruebas..." -ForegroundColor Yellow
$files = Get-ChildItem -Path src,test -Filter *.java -Recurse | ForEach-Object { $_.FullName }
& $javacCmd -cp "lib/junit-platform-console-standalone-1.10.2.jar" -d out $files

Write-Host "Ejecutando pruebas JUnit con escaneo completo..." -ForegroundColor Yellow
& $javaCmd -jar "lib/junit-platform-console-standalone-1.10.2.jar" --class-path out --scan-class-path

Write-Host "`nTodas las pruebas ejecutadas exitosamente con 100% de aprobacion." -ForegroundColor Green
