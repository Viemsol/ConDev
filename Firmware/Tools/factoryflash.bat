@echo off
set SN=%1
IF [%2] == [] GOTO Label1
set IMAGEDIRAPPBOOTHEXPATH=%2
GOTO Call
:Label1
set IMAGEDIRAPPBOOTHEXPATH=..\Image\AppBootCombine.Hex
:Call
set "mppath=C:\Program Files\Microchip\MPLABX\v4.05\mplab_ipe"
set "pk3execpath=%mppath%\pk3cmd.exe"
@REM set PATH=C:\Program Files\Microchip\MPLABX\v4.05\mplab_ipe;%PATH%

set IMAGEDIRAPPBOOTHEXFACTPROGPATH=.\..\..\Image\AppBootCombineFactory.Hex

Echo -----------------Injecting  Serial no ----------------------------
@REM genrate OTA file from hex file with extension .OTA
C:\Python27\python.exe InjectSnSetFect.py %IMAGEDIRAPPBOOTHEXPATH% %SN%

Echo ----------------programming Combined hex file---------------------
cd %APPWORKINGDIR%
"%pk3execpath%" -P16F18313 -V5.000 -B -C
"%pk3execpath%" -P16F18313 -F%IMAGEDIRAPPBOOTHEXFACTPROGPATH% -V5.000 -M -Y
cd %TOOLWORKINGDIRRELATIV%

