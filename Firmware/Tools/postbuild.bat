Echo Postbuilding..
set GENOTA=%1
set HEXFILE=%2
set DEVTYPE=%3
@REM genrate OTA file from hex file with extension .OTA
%GENOTA% %HEXFILE% %DEVTYPE%
