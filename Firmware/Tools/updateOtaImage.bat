@echo off
set IMAGEDIRAPPBOOTHEXPATH=%1
set TYPE=%2
set VERSION=%3
set INFO=%4
Echo -----------------Publishing image to server ----------------------------
@REM genrate OTA file from hex file with extension .OTA
C:\Python27\python.exe fbOtaImageUpdate.py %IMAGEDIRAPPBOOTHEXPATH% %TYPE% %VERSION% %INFO%

