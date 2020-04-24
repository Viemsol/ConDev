@echo off
set TYPE=%1
set VERSION=%2
set INFO=%3
IF [%4] == [] GOTO Label1
set IMAGEDIRAPPOTAHEXPATH=%4
GOTO Call
:Label1
set IMAGEDIRAPPOTAHEXPATH=..\Image\App.OTA
:Call
Echo -----------------Publishing image to server ----------------------------
@REM genrate OTA file from hex file with extension .OTA
C:\Python27\python.exe fbOtaImageUpdate.py %IMAGEDIRAPPOTAHEXPATH% %TYPE% %VERSION% %INFO%

