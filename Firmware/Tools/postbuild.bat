@echo off
set GENOTA=%1
set APP_FOLDER=%2
@REM genrate OTA file from hex file with extension .OTA
%GENOTA% %APP_FOLDER%
