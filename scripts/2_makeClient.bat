@echo off

call .\setenv.bat

rem opencard.core.*
set CLASSES=%CLASSES%;%OCF_HOME%\lib\base-core.jar

rem opencard.opt.util
set CLASSES=%CLASSES%;%OCF_HOME%\lib\base-opt.jar

rem bouncy castle (crypto provider)
set CLASSES=%CLASSES%;%MISC%\bcprov-jdk15on-150.jar

set CLASSES=%CLASSES%;%MISC%\commons-codec-1.3.jar


IF NOT EXIST %OUT%\%PROJECT% MD %OUT%\%PROJECT% 

echo Compilation...
dir /s /B %SRC%\%PROJECT%\%PKGCLIENT%\*.java > sources.txt
powershell -Command "(gc sources.txt) -replace '\\', '\\' | Out-File -encoding ASCII sources.txt"

copy sources.txt sources2.txt
type nul > sources.txt
for /f "tokens=*" %%a in (sources2.txt) do (
  echo "%%a" >> sources.txt
)

del sources2.txt

%JAVA_HOME%\bin\javac.exe -cp %CLASSES% -g -d %OUT%\%PROJECT% @sources.txt
rem %JAVA_HOME%\bin\jar.exe -cvfe ClientChat.jar com.github.MrrRaph.corosechat.client.ClientChat -C %OUT%\%PROJECT% .
if errorlevel 1 goto error
echo %CLIENT%.class compiled: OK
echo .

goto end

:error
echo ***************
echo    ERROR !
echo ***************
pause
goto end

:end
del sources.txt
cls
echo Successfully compiled client class !
