@echo off
set SN=%1

Echo -----------------Injecting  Serial no ----------------------------
@REM genrate OTA file from hex file with extension .OTA
C:\Python27\python.exe registerDevice.py %SN%

