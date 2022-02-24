@echo off
REM
REM Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

REM Workfile:@(#)securermidemo.bat	1.18
REM Version:1.18
REM Modified:09/29/03 17:06:16

@echo on
@echo This demo requires a cref or jcwde with com.sun.javacard.SecureRMIDemo
@echo applet installed and file opencard.properties in the current directory

@echo Start cref or jcwde before running this demo
@echo off

setlocal

 if "%JAVA_HOME%" == "" goto warning1
 if "%JC_HOME%" == "" goto warning2


set JC_RMICPATH=%JC_HOME%\lib\base-core.jar;%JC_HOME%\lib\base-opt.jar;%JC_HOME%\lib\jcrmiclientframework.jar;%JC_HOME%\lib\jcclientsamples.jar;%JC_HOME%\lib\apduio.jar;%JC_HOME%\samples\classes

%JAVA_HOME%\bin\java -classpath %JC_RMICPATH%;%CLASSPATH% com.sun.javacard.clientsamples.securepurseclient.SecurePurseClient %1

 goto quit

:warning1
 echo Set environment variable JAVA_HOME
 goto quit

:warning2
 echo Set environment variable JC_HOME
 goto quit

:quit
 endlocal
