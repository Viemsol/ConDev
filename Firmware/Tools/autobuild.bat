
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
set IMAGEDIRAPPBOOTHEXPROGPATH=.\..\..\Image\AppBootCombine.Hex
@REM addpath variable ony if not added , inject or combine eep hex file
@REM build Bootloader
Echo Building bootloader
cd %BOOTLOWORKINGDIR%
@REM make -f nbproject/Makefile-default.mk SUBPROJECTS= .build-conf
cd %TOOLWORKINGDIRRELATIV%


@REM build Application and OTA
Echo Building Application and OTA file
cd %APPWORKINGDIR%
@REM make -f nbproject/Makefile-default.mk SUBPROJECTS= .build-conf
cd %TOOLWORKINGDIRRELATIV%

Echo Creating Combined file
@REM hexmate -O%IMAGEDIRAPPBOOTHEXPATH% %APPHEXDIR% +%BOOTHEXDIR%

@REM Copy Files to Image folder
Echo Updating Image folder
COPY %APPOTADIR% %IMAGEDIROTAPATH%
COPY %APPHEXDIR% %IMAGEDIRAPPHEXPATH%
COPY %BOOTHEXDIR% %IMAGEDIRBOOTHEXPATH%

Echo Injecting  Serial no and Device type

Echo programming Combined hex file
cd %APPWORKINGDIR%
PK3CMD -P16F18313 -FC:\AppBootCombine.Hex -V5.000 -M -Y
cd %TOOLWORKINGDIRRELATIV%
@REM  make -f %APPLICATIONMAKEPATH% 'SUBPROJECTS=' '.build-conf'
@REM hexmate boot.hex firmware.hex -Ocombined.hex
