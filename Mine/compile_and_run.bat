@echo off
set JAVAC=javac
set JAVA=java
set JARPATH=..\..\..\jar\
set JNAJAR=%JARPATH%jna.jar
set LJUDJAR=%JARPATH%ljud.jar
set CLASSPATH=.;%JNAJAR%;%LJUDJAR%

rem Compile all .java files in current directory.
echo Compile example (javac).
%JAVAC% -classpath %CLASSPATH% *.java || goto :done

rem Run first found .class file. LJUD examples only create one class file.
echo.
echo Run example (java).
for %%i in (*.class) do (
	%JAVA% -classpath %CLASSPATH% %%~ni
	goto :done
)
echo Couldn't find a .class file to run. 

:done
echo.
pause
