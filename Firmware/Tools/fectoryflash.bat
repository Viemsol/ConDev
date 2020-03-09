Echo key injecting
set SN=%1
set DEVTYPE=%2
@REM build boot loader, build application, put boot loader hex , application hex , combined hex and OTA file Image folder
@REM build bootloader
@REM NOTE --------------ADD BELOW TO ENV VARIABLE--------------------------ie JUST ENABLE IT ONCE
@REM set PATH=C:\Program Files\Microchip\MPLABX\vx.xx\gnuBins\GnuWin32\bin;%PATH%
@REM set PATH=C:\Program Files\Microchip\MPLABX\v4.05\mplab_ipe;%PATH%

set BOOTLOWORKINGDIR=./../FwBootloader/Build
set APPWORKINGDIR=./../FwApp/Build
set TOOLWORKINGDIRRELATIV=./../../Tools

set APPOTADIR=.\..\FwApp\Build\dist\default\production\Build.production.OTA
set APPHEXDIR=.\..\FwApp\Build\dist\default\production\Build.production.hex
set BOOTHEXDIR=.\..\FwBootloader\Build\dist\default\production\Build.production.hex

set IMAGEDIROTAPATH=.\..\Image\App.OTA
set IMAGEDIRAPPHEXPATH=.\..\Image\App.Hex
set IMAGEDIRBOOTHEXPATH=.\..\Image\Boot.Hex
set IMAGEDIRAPPBOOTHEXPATH=.\..\Image\AppBootCombine.Hex
set IMAGEDIRAPPBOOTHEXFACTPROGPATH=.\..\..\Image\AppBootCombineFactory.Hex

Echo Injecting  Serial no and Device type
@REM genrate OTA file from hex file with extension .OTA
C:\Python27\python.exe InjectSnSetFect.py %IMAGEDIRAPPBOOTHEXPATH% %SN% %DEVTYPE%

Echo programming Combined hex file
cd %APPWORKINGDIR%
PK3CMD -P16F18313 -F%IMAGEDIRAPPBOOTHEXFACTPROGPATH% -V5.000 -M -Y
cd %TOOLWORKINGDIRRELATIV%

