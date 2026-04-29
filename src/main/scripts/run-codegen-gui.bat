@echo off
setlocal
set DIR=%~dp0
set "JAR=%DIR%zero-codegen-gui.jar"
if exist "%JAR%" goto run

for %%F in ("%DIR%..\zero-codegen-*-gui.jar") do (
  if exist "%%~fF" (
    set "JAR=%%~fF"
    goto run
  )
)

echo Cannot find a GUI jar near "%DIR%".
echo Expected one of:
echo   %DIR%zero-codegen-gui.jar
echo   %DIR%..\zero-codegen-*-gui.jar
echo Please run "mvn -pl zero-codegen -am package" first.
exit /b 1

:run
echo Starting Zero SI Codegen GUI ...
java -jar "%JAR%"
endlocal
