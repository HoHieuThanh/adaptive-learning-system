@REM ----------------------------------------------------------------------------
@REM Maven Wrapper script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "DIR=%~dp0"
set "MAVEN_CMD=mvn"

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 goto use_path_mvn

if exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.15-bin\4rlcemksed9vjmkvgss0jpc4po\apache-maven-3.9.15\bin\mvn.cmd" (
    call "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.15-bin\4rlcemksed9vjmkvgss0jpc4po\apache-maven-3.9.15\bin\mvn.cmd" %*
    goto end
)

echo [ERROR] Maven not found in PATH or standard wrapper location.
echo Please install Apache Maven or set M2_HOME.
exit /b 1

:use_path_mvn
call "%MAVEN_CMD%" %*
goto end

:end
exit /b %ERRORLEVEL%

