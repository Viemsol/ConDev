@echo off
@REM build boot loader, build application, put boot loader hex , application hex , combined hex and OTA file Image folder
@REM build bootloader
set "mppath=C:\Program Files\Microchip\MPLABX\v4.05\gnuBins\GnuWin32\bin"
set "makeexecpath=%mppath%\make.exe"
@REM set PATH=C:\Program Files\Microchip\MPLABX\v4.05\mplab_ipe;%PATH%

set BOOTLOWORKINGDIR=./../FwBootloader/Build
set APPWORKINGDIR=./../FwApp/Build
set APPDIR=./../FwApp
set TOOLWORKINGDIRRELATIV=./../../Tools

set APPOTADIR=.\..\FwApp\Build\dist\default\production\Build.production.OTA
set APPTLV8DIR=.\..\FwApp\Build\dist\default\production\Build.production.TLV8
set APPHEXDIR=.\..\FwApp\Build\dist\default\production\Build.production.hex
set BOOTHEXDIR=.\..\FwBootloader\Build\dist\default\production\Build.production.hex

set IMAGEDIROTAPATH=.\..\Image\App.OTA
set IMAGEDIRTLV8PATH=.\..\Image\App.TLV8
set IMAGEDIRAPPHEXPATH=.\..\Image\App.Hex
set IMAGEDIRBOOTHEXPATH=.\..\Image\Boot.Hex
set IMAGEDIRAPPBOOTHEXPATH=.\..\Image\AppBootCombine.Hex
set IMAGEDIRAPPBOOTHEXPROGPATH=.\..\..\Image\AppBootCombine.Hex
@REM addpath variable ony if not added , inject or combine eep hex file

@REM build Bootloader : will contain config as : Bl version , Device ID: "1235"
@echo off
Echo ---------------------Building Boot.hex---------------------------------
cd %BOOTLOWORKINGDIR%
"%makeexecpath%" -f nbproject/Makefile-default.mk SUBPROJECTS= .build-conf
cd %TOOLWORKINGDIRRELATIV%


@REM build Application 
@REM App only OTA( will contain config as :  Appversion, Devicetyp , write checksum command Noooo app valid command 
@REM fectory build combine file(BL,APP) config as :all bl conigs + Appversion, Devicetyp , checksum and appvalid =devtype
Echo ----------------Building App.hex and App.OTA---------------------------
cd %APPWORKINGDIR%
call "%makeexecpath%" -f nbproject/Makefile-default.mk SUBPROJECTS= .build-conf
cd %TOOLWORKINGDIRRELATIV%
@REM Echo Creating Combined file
Echo ----------------Cleaning Image folder ----------------------------
IF EXIST %IMAGEDIROTAPATH% DEL /F %IMAGEDIROTAPATH%
IF EXIST %IMAGEDIRTLV8PATH% DEL /F %IMAGEDIRTLV8PATH%
IF EXIST %IMAGEDIRAPPHEXPATH% DEL /F %IMAGEDIRAPPHEXPATH%
IF EXIST %IMAGEDIRBOOTHEXPATH% DEL /F %IMAGEDIRBOOTHEXPATH%
IF EXIST %IMAGEDIRAPPBOOTHEXPATH% DEL /F %IMAGEDIRAPPBOOTHEXPATH%
IF EXIST %IMAGEDIRAPPBOOTHEXPROGPATH% DEL /F %IMAGEDIRAPPBOOTHEXPROGPATH%
Echo ----------------Building AppBootCombine.Hex ----------------------------
call hexmate -O%IMAGEDIRAPPBOOTHEXPATH% %APPHEXDIR% +%BOOTHEXDIR%
Echo ----------------Copying File to Image Folder ---------------------------
@REM Copy Files to Image folder

COPY %APPOTADIR% %IMAGEDIROTAPATH%
COPY %APPTLV8DIR% %IMAGEDIRTLV8PATH%
COPY %APPHEXDIR% %IMAGEDIRAPPHEXPATH%
COPY %BOOTHEXDIR% %IMAGEDIRBOOTHEXPATH%

@REM simply wait
timeout /t 2 /nobreak

Echo -----Injecting CRC and App Valid flag to EEP of AppBootCombine.Hex------
call C:\Python27\python.exe InjectCRCAppValid.py %IMAGEDIRAPPBOOTHEXPATH% %IMAGEDIROTAPATH% %APPDIR%
Echo -------------------------Sucess ----------------------------------------

