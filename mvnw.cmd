@REM Maven Wrapper launcher for Windows (downloads wrapper jar on first use)
@echo off
setlocal
set "ROOT=%~dp0"
set "WRAPPER_JAR=%ROOT%.mvn\wrapper\maven-wrapper.jar"
if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Invoke-WebRequest -UseBasicParsing -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%WRAPPER_JAR%' -ErrorAction Stop } catch { Write-Error $_; exit 1 }"
  if errorlevel 1 exit /b 1
)
if not defined JAVA_HOME (
  java -version >nul 2>&1
  if errorlevel 1 (echo Java 21 is required. & exit /b 1)
  java -cp "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%ROOT%" org.apache.maven.wrapper.MavenWrapperMain %*
) else (
  "%JAVA_HOME%\bin\java.exe" -cp "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%ROOT%" org.apache.maven.wrapper.MavenWrapperMain %*
)
exit /b %ERRORLEVEL%
