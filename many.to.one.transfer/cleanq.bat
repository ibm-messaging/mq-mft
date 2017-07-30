@echo off
 
REM Remove messages from the specified queue
 
REM Parameters passed to script:
REM  %1=Queue manager name
REM  %2=Queue name
 
if %1. == .  goto showhelp
if %2. == .  goto showhelp
 
echo clear qlocal(%2) | runmqsc %1
goto theend
 
rem Print help on how to use the script
:showhelp
echo Usage:
echo    cleanq.bat QMgrName QueueName
:theend
