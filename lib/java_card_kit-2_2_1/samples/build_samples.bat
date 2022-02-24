@echo off
REM
REM Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

REM Workfile:@(#)build_samples.bat	1.44
REM Version:1.44
REM Modified:09/29/03 17:06:16

 setlocal

 if "%JAVA_HOME%" == "" goto warning1
 if "%JC_HOME%" == "" goto warning2

:: Help

 if "%1" == "help" goto help
 if "%1" == "-help" goto help

 cd /d %JC_HOME%\samples

:: Clean

 if exist classes\nul rmdir /s/q classes
 if exist src\demo\demo2\demo2.scr del /f src\demo\demo2\demo2.scr
 if exist src\demo\demo2\demo2crypto.scr del /f src\demo\demo2\demo2crypto.scr
 if exist src\demo\demo2\javapurse\_tmp\JavaLoyalty.scr del /f src\demo\demo2\javapurse\_tmp\JavaLoyalty.scr
 if exist src\demo\demo2\javapurse\_tmp\JavaPurse.scr del /f src\demo\demo2\javapurse\_tmp\JavaPurse.scr
 if exist src\demo\demo2\javapurse\_tmp\JavaPurseCrypto.scr del /f src\demo\demo2\javapurse\_tmp\JavaPurseCrypto.scr
 if exist src\demo\demo2\javapurse\_tmp\SampleLibrary.scr del /f src\demo\demo2\javapurse\_tmp\SampleLibrary.scr
 if exist src\demo\demo2\wallet\_tmp\Wallet.scr del /f src\demo\demo2\wallet\_tmp\Wallet.scr
 if exist src\demo\demo2\rmi\_tmp\RMIDemo.scr del /f src\demo\demo2\rmi\_tmp\RMIDemo.scr
 if exist src\demo\elliptic_curve_rmi\ECDemo.scr del /f src\demo\elliptic_curve_rmi\ECDemo.scr
 if exist src\demo\demo2\rmi\_tmp\SecureRMIDemo.scr del /f src\demo\demo2\rmi\_tmp\SecureRMIDemo.scr
 if exist src\demo\photocard\_tmp\photocard.scr del /f src\demo\photocard\_tmp\photocard.scr
 if exist src\demo\object_deletion\odDemo1-1.scr del /f src\demo\object_deletion\odDemo1-1.scr
 if exist src\demo\object_deletion\odDemo1-2.scr del /f src\demo\object_deletion\odDemo1-2.scr
 if exist src\demo\object_deletion\odDemo1-3.scr del /f src\demo\object_deletion\odDemo1-3.scr
 if exist src\demo\object_deletion\odDemo2.scr del /f src\demo\object_deletion\odDemo2.scr
 if exist src\demo\object_deletion\_tmp\packageA.scr del /f src\demo\object_deletion\_tmp\packageA.scr
 if exist src\demo\object_deletion\_tmp\packageB.scr del /f src\demo\object_deletion\_tmp\packageB.scr
 if exist src\demo\object_deletion\_tmp\packageC.scr del /f src\demo\object_deletion\_tmp\packageC.scr
 if exist src\demo\logical_channels\channelDemo.scr del /f src\demo\logical_channels\channelDemo.scr
 if exist src\demo\logical_channels\_tmp\ChnDemo.scr del /f src\demo\logical_channels\_tmp\ChnDemo.scr

 if "%1" == "clean" goto quit
 if "%1" == "-clean" goto quit

 set CL_DIR=%JC_HOME%\samples\classes

 set JC_PATH=.;%CL_DIR%;%JC_HOME%\lib\api.jar
 set JCFLAGS=-g -d %CL_DIR% -classpath "%JC_PATH%"

 set REMOTE_CLASSES=com.sun.javacard.samples.RMIDemo.PurseImpl com.sun.javacard.samples.SecureRMIDemo.SecurePurseImpl com.sun.javacard.samples.photocard.PhotoCardImpl
 set JAVAC_CP=%JC_HOME%\lib\javacardframework.jar;%CL_DIR%
 set CLIENT_CLASSPATH=%JC_HOME%\lib\jcrmiclientframework.jar;%CL_DIR%;%JC_HOME%\lib\base-core.jar;%JC_HOME%\lib\base-opt.jar
 set CLIENT_FILES=%JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\purseclient\*.java %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\securepurseclient\*.java %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\photocardclient\*.java 
 set CL_JAR_NAME=%JC_HOME%\lib\jcclientsamples.jar

:: Copy export files
 if not exist classes mkdir classes
 xcopy /s %JC_HOME%\api_export_files\*.* classes\

:: Compile samples

 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\HelloWorld\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\SampleLibrary\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\JavaLoyalty\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\JavaPurse\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\NullApp\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\wallet\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\RMIDemo\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\SecureRMIDemo\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\photocard\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\odSample\packageA\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\odSample\libPackageC\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\odSample\packageB\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\ChannelsDemo\*.java
 if exist src\com\sun\javacard\samples\JavaPurseCrypto\nul %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\JavaPurseCrypto\*.java
 if exist src\com\sun\javacard\samples\eccrmi\nul %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\eccrmi\*.java

:: Convert samples

 cd classes
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\HelloWorld\HelloWorld.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\SampleLibrary\SampleLibrary.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\JavaLoyalty\JavaLoyalty.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\JavaPurse\JavaPurse.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\NullApp\NullApp.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\wallet\Wallet.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\RMIDemo\RMIDemo.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\SecureRMIDemo\SecureRMIDemo.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\photocard\photocard.opt
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\odSample\packageA\packageA.opt 
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\odSample\libPackageC\libPackageC.opt 
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\odSample\packageB\packageB.opt 
 call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\ChannelsDemo\ChannelsDemo.opt 
 if exist ..\src\com\sun\javacard\samples\JavaPurseCrypto\nul call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\JavaPurseCrypto\JavaPurseCrypto.opt
 if exist ..\src\com\sun\javacard\samples\eccrmi\nul call %JC_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\eccrmi\ECPurse.opt
 cd ..

:: Create SCR for demo2 in cref mode

 cd src\demo\demo2


 call %JC_HOME%\bin\scriptgen -o javapurse\_tmp\JavaLoyalty.scr ..\..\..\classes\com\sun\javacard\samples\JavaLoyalty\javacard\JavaLoyalty.cap
 call %JC_HOME%\bin\scriptgen -o javapurse\_tmp\JavaPurse.scr ..\..\..\classes\com\sun\javacard\samples\JavaPurse\javacard\JavaPurse.cap
 call %JC_HOME%\bin\scriptgen -o javapurse\_tmp\SampleLibrary.scr ..\..\..\classes\com\sun\javacard\samples\SampleLibrary\javacard\SampleLibrary.cap
 call %JC_HOME%\bin\scriptgen -o wallet\_tmp\Wallet.scr ..\..\..\classes\com\sun\javacard\samples\wallet\javacard\wallet.cap

 call %JC_HOME%\bin\scriptgen -o rmi\_tmp\RMIDemo.scr ..\..\..\classes\com\sun\javacard\samples\RMIDemo\javacard\RMIDemo.cap

 call %JC_HOME%\bin\scriptgen -o rmi\_tmp\SecureRMIDemo.scr ..\..\..\classes\com\sun\javacard\samples\SecureRMIDemo\javacard\SecureRMIDemo.cap

 call %JC_HOME%\bin\scriptgen -o photocard\_tmp\photocard.scr ..\..\..\classes\com\sun\javacard\samples\photocard\javacard\photocard.cap
 cd ..

 copy /b misc\Header.scr+demo2\javapurse\_tmp\SampleLibrary.scr+demo2\javapurse\_tmp\JavaLoyalty.scr+demo2\javapurse\_tmp\JavaPurse.scr+demo2\wallet\_tmp\Wallet.scr+demo2\rmi\_tmp\RMIDemo.scr+demo2\rmi\_tmp\SecureRMIDemo.scr+demo2\photocard\_tmp\photocard.scr+demo2\javapurse\AppletTest.scr+misc\Footer.scr demo2\demo2.scr

 call %JC_HOME%\bin\scriptgen -o object_deletion\_tmp\packageA.scr ..\..\classes\com\sun\javacard\samples\odSample\packageA\javacard\packageA.cap
 call %JC_HOME%\bin\scriptgen -o object_deletion\_tmp\packageB.scr ..\..\classes\com\sun\javacard\samples\odSample\packageB\javacard\packageB.cap
 call %JC_HOME%\bin\scriptgen -o object_deletion\_tmp\packageC.scr ..\..\classes\com\sun\javacard\samples\odSample\libPackageC\javacard\libPackageC.cap
 call %JC_HOME%\bin\scriptgen -o logical_channels\_tmp\ChnDemo.scr ..\..\classes\com\sun\javacard\samples\ChannelsDemo\javacard\ChannelsDemo.cap

 copy /b misc\Header.scr+object_deletion\_tmp\packageA.scr+object_deletion\_tmp\packageC.scr+object_deletion\_tmp\packageB.scr+object_deletion\od1.scr+misc\Footer.scr object_deletion\odDemo1-1.scr

 copy /b misc\Header.scr+object_deletion\od2.scr+misc\Footer.scr object_deletion\odDemo1-2.scr

 copy /b misc\Header.scr+object_deletion\od2-2.scr+misc\Footer.scr object_deletion\odDemo1-3.scr

 copy /b misc\Header.scr+object_deletion\_tmp\packageA.scr+object_deletion\od3.scr+object_deletion\_tmp\packageC.scr+object_deletion\od3-2.scr+misc\Footer.scr object_deletion\odDemo2.scr

 copy /b misc\Header.scr+logical_channels\_tmp\ChnDemo.scr+logical_channels\channel.scr+misc\Footer.scr logical_channels\channelDemo.scr

 if exist ..\..\classes\com\sun\javacard\samples\JavaPurseCrypto\nul call %JC_HOME%\bin\scriptgen -o demo2\javapurse\_tmp\JavaPurseCrypto.scr ..\..\classes\com\sun\javacard\samples\JavaPurseCrypto\javacard\JavaPurseCrypto.cap
 if exist demo2\javapurse\_tmp\JavaPurseCrypto.scr copy /b misc\Header.scr+demo2\javapurse\_tmp\SampleLibrary.scr+demo2\javapurse\_tmp\JavaLoyalty.scr+demo2\javapurse\_tmp\JavaPurseCrypto.scr+demo2\wallet\_tmp\Wallet.scr+demo2\rmi\_tmp\RMIDemo.scr+demo2\rmi\_tmp\SecureRMIDemo.scr+demo2\photocard\_tmp\photocard.scr+demo2\javapurse\AppletTestCrypto.scr+misc\Footer.scr demo2\demo2crypto.scr


if exist ..\..\classes\com\sun\javacard\samples\eccrmi\nul call %JC_HOME%\bin\scriptgen -o elliptic_curve_rmi\_tmp\ECRMIDemo.scr ..\..\classes\com\sun\javacard\samples\eccrmi\javacard\eccrmi.cap 

if exist elliptic_curve_rmi\nul copy /b misc\Header.scr+elliptic_curve_rmi\_tmp\ECRMIDemo.scr+elliptic_curve_rmi\ECFooter.scr elliptic_curve_rmi\ECDemo.scr


 echo Building the client part of RMI samples...

 %JAVA_HOME%\bin\rmic -v1.2 -d %CL_DIR% -classpath %JAVAC_CP% %REMOTE_CLASSES%
 %JAVA_HOME%\bin\javac -classpath %CLIENT_CLASSPATH% -d %CL_DIR% %CLIENT_FILES%
 copy /A %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\purseclient\*.properties %CL_DIR%\com\sun\javacard\clientsamples\purseclient\
 copy /A %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\securepurseclient\*.properties %CL_DIR%\com\sun\javacard\clientsamples\securepurseclient\
 copy /A %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\photocardclient\*.properties %CL_DIR%\com\sun\javacard\clientsamples\photocardclient\

  if exist %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\ecpurseclient\nul %JAVA_HOME%\bin\rmic -v1.2 -d %CL_DIR% -classpath %JAVAC_CP% com.sun.javacard.samples.eccrmi.ECPurseImpl

  if exist %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\ecpurseclient\nul %JAVA_HOME%\bin\javac -classpath %CLIENT_CLASSPATH% -d %CL_DIR% %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\ecpurseclient\*.java

  if exist %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\ecpurseclient\nul copy /A %JC_HOME%\samples\src_client\com\sun\javacard\clientsamples\ecpurseclient\*.properties %CL_DIR%\com\sun\javacard\clientsamples\ecpurseclient\


  xcopy /E /Q %CL_DIR%\com\sun\javacard\clientsamples\* %CL_DIR%\tmp_client\com\sun\javacard\clientsamples\
  %JAVA_HOME%\bin\jar -cf %CL_JAR_NAME% -C %CL_DIR%\tmp_client com
  rmdir /S /Q %CL_DIR%\tmp_client

 cd ..\..

 goto quit

:warning1
 echo Set environment variable JAVA_HOME
 goto quit

:warning2
 echo Set environment variable JC_HOME
 goto quit

:help
 echo Usage: build_samples [options]
 echo Where options include:
 echo        -help     print out this message
 echo        -clean    remove all produced files
 goto quit





:quit
 endlocal
