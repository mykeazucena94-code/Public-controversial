@echo off
setlocal
set "VERSION=8.9"
set "CACHE_DIR=%USERPROFILE%\.gradle\screen-ai-bootstrap"
set "GRADLE_BIN=%CACHE_DIR%\gradle-%VERSION%\bin\gradle.bat"
if not exist "%GRADLE_BIN%" (
  if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip' -OutFile '%CACHE_DIR%\gradle-%VERSION%-bin.zip'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%CACHE_DIR%\gradle-%VERSION%-bin.zip' '%CACHE_DIR%'"
)
call "%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%
