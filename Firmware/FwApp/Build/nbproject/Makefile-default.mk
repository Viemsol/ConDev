#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Include project Makefile
ifeq "${IGNORE_LOCAL}" "TRUE"
# do not include local makefile. User is passing all local related variables already
else
include Makefile
# Include makefile containing local settings
ifeq "$(wildcard nbproject/Makefile-local-default.mk)" "nbproject/Makefile-local-default.mk"
include nbproject/Makefile-local-default.mk
endif
endif

# Environment
MKDIR=gnumkdir -p
RM=rm -f 
MV=mv 
CP=cp 

# Macros
CND_CONF=default
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
IMAGE_TYPE=debug
OUTPUT_SUFFIX=elf
DEBUGGABLE_SUFFIX=elf
FINAL_IMAGE=dist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.${OUTPUT_SUFFIX}
else
IMAGE_TYPE=production
OUTPUT_SUFFIX=hex
DEBUGGABLE_SUFFIX=elf
FINAL_IMAGE=dist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.${OUTPUT_SUFFIX}
endif

ifeq ($(COMPARE_BUILD), true)
COMPARISON_BUILD=--mafrlcsj
else
COMPARISON_BUILD=
endif

ifdef SUB_IMAGE_ADDRESS

else
SUB_IMAGE_ADDRESS_COMMAND=
endif

# Object Directory
OBJECTDIR=build/${CND_CONF}/${IMAGE_TYPE}

# Distribution Directory
DISTDIR=dist/${CND_CONF}/${IMAGE_TYPE}

# Source Files Quoted if spaced
SOURCEFILES_QUOTED_IF_SPACED=../Src/app.c ../Src/os.c ../Src/system.c ../Src/uart.c

# Object Files Quoted if spaced
OBJECTFILES_QUOTED_IF_SPACED=${OBJECTDIR}/_ext/1360906485/app.p1 ${OBJECTDIR}/_ext/1360906485/os.p1 ${OBJECTDIR}/_ext/1360906485/system.p1 ${OBJECTDIR}/_ext/1360906485/uart.p1
POSSIBLE_DEPFILES=${OBJECTDIR}/_ext/1360906485/app.p1.d ${OBJECTDIR}/_ext/1360906485/os.p1.d ${OBJECTDIR}/_ext/1360906485/system.p1.d ${OBJECTDIR}/_ext/1360906485/uart.p1.d

# Object Files
OBJECTFILES=${OBJECTDIR}/_ext/1360906485/app.p1 ${OBJECTDIR}/_ext/1360906485/os.p1 ${OBJECTDIR}/_ext/1360906485/system.p1 ${OBJECTDIR}/_ext/1360906485/uart.p1

# Source Files
SOURCEFILES=../Src/app.c ../Src/os.c ../Src/system.c ../Src/uart.c


CFLAGS=
ASFLAGS=
LDLIBSOPTIONS=

############# Tool locations ##########################################
# If you copy a project from one host to another, the path where the  #
# compiler is installed may be different.                             #
# If you open this project with MPLAB X in the new host, this         #
# makefile will be regenerated and the paths will be corrected.       #
#######################################################################
# fixDeps replaces a bunch of sed/cat/printf statements that slow down the build
FIXDEPS=fixDeps

# The following macros may be used in the pre and post step lines
Device=PIC16F18313
ProjectDir="C:\Users\acer\Desktop\_GitConDev\Firmware\FwApp\Build"
ConfName=default
ImagePath="dist\default\${IMAGE_TYPE}\Build.${IMAGE_TYPE}.${OUTPUT_SUFFIX}"
ImageDir="dist\default\${IMAGE_TYPE}"
ImageName="Build.${IMAGE_TYPE}.${OUTPUT_SUFFIX}"
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
IsDebug="true"
else
IsDebug="false"
endif

.build-conf:  .pre ${BUILD_SUBPROJECTS}
ifneq ($(INFORMATION_MESSAGE), )
	@echo $(INFORMATION_MESSAGE)
endif
	${MAKE}  -f nbproject/Makefile-default.mk dist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.${OUTPUT_SUFFIX}
	@echo "--------------------------------------"
	@echo "User defined post-build step: [.\..\..\Tools\postbuild.bat .\..\..\Tools\OtaBuilderMplab.py .\..\FwApp\Build\dist\default\production\Build.production.hex 01]"
	@.\..\..\Tools\postbuild.bat .\..\..\Tools\OtaBuilderMplab.py .\..\FwApp\Build\dist\default\production\Build.production.hex 01
	@echo "--------------------------------------"

MP_PROCESSOR_OPTION=16F18313
# ------------------------------------------------------------------------------------
# Rules for buildStep: compile
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
${OBJECTDIR}/_ext/1360906485/app.p1: ../Src/app.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/_ext/1360906485" 
	@${RM} ${OBJECTDIR}/_ext/1360906485/app.p1.d 
	@${RM} ${OBJECTDIR}/_ext/1360906485/app.p1 
	${MP_CC} --pass1 $(MP_EXTRA_CC_PRE) --chip=$(MP_PROCESSOR_OPTION) -Q -G  -D__DEBUG=1 --debugger=pickit3  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib $(COMPARISON_BUILD)  --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"    -o${OBJECTDIR}/_ext/1360906485/app.p1  ../Src/app.c 
	@-${MV} ${OBJECTDIR}/_ext/1360906485/app.d ${OBJECTDIR}/_ext/1360906485/app.p1.d 
	@${FIXDEPS} ${OBJECTDIR}/_ext/1360906485/app.p1.d $(SILENT) -rsi ${MP_CC_DIR}../  
	
${OBJECTDIR}/_ext/1360906485/os.p1: ../Src/os.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/_ext/1360906485" 
	@${RM} ${OBJECTDIR}/_ext/1360906485/os.p1.d 
	@${RM} ${OBJECTDIR}/_ext/1360906485/os.p1 
	${MP_CC} --pass1 $(MP_EXTRA_CC_PRE) --chip=$(MP_PROCESSOR_OPTION) -Q -G  -D__DEBUG=1 --debugger=pickit3  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib $(COMPARISON_BUILD)  --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"    -o${OBJECTDIR}/_ext/1360906485/os.p1  ../Src/os.c 
	@-${MV} ${OBJECTDIR}/_ext/1360906485/os.d ${OBJECTDIR}/_ext/1360906485/os.p1.d 
	@${FIXDEPS} ${OBJECTDIR}/_ext/1360906485/os.p1.d $(SILENT) -rsi ${MP_CC_DIR}../  
	
${OBJECTDIR}/_ext/1360906485/system.p1: ../Src/system.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/_ext/1360906485" 
	@${RM} ${OBJECTDIR}/_ext/1360906485/system.p1.d 
	@${RM} ${OBJECTDIR}/_ext/1360906485/system.p1 
	${MP_CC} --pass1 $(MP_EXTRA_CC_PRE) --chip=$(MP_PROCESSOR_OPTION) -Q -G  -D__DEBUG=1 --debugger=pickit3  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib $(COMPARISON_BUILD)  --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"    -o${OBJECTDIR}/_ext/1360906485/system.p1  ../Src/system.c 
	@-${MV} ${OBJECTDIR}/_ext/1360906485/system.d ${OBJECTDIR}/_ext/1360906485/system.p1.d 
	@${FIXDEPS} ${OBJECTDIR}/_ext/1360906485/system.p1.d $(SILENT) -rsi ${MP_CC_DIR}../  
	
${OBJECTDIR}/_ext/1360906485/uart.p1: ../Src/uart.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/_ext/1360906485" 
	@${RM} ${OBJECTDIR}/_ext/1360906485/uart.p1.d 
	@${RM} ${OBJECTDIR}/_ext/1360906485/uart.p1 
	${MP_CC} --pass1 $(MP_EXTRA_CC_PRE) --chip=$(MP_PROCESSOR_OPTION) -Q -G  -D__DEBUG=1 --debugger=pickit3  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib $(COMPARISON_BUILD)  --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"    -o${OBJECTDIR}/_ext/1360906485/uart.p1  ../Src/uart.c 
	@-${MV} ${OBJECTDIR}/_ext/1360906485/uart.d ${OBJECTDIR}/_ext/1360906485/uart.p1.d 
	@${FIXDEPS} ${OBJECTDIR}/_ext/1360906485/uart.p1.d $(SILENT) -rsi ${MP_CC_DIR}../  
	
else
${OBJECTDIR}/_ext/1360906485/app.p1: ../Src/app.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/_ext/1360906485" 
	@${RM} ${OBJECTDIR}/_ext/1360906485/app.p1.d 
	@${RM} ${OBJECTDIR}/_ext/1360906485/app.p1 
	${MP_CC} --pass1 $(MP_EXTRA_CC_PRE) --chip=$(MP_PROCESSOR_OPTION) -Q -G  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib $(COMPARISON_BUILD)  --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"    -o${OBJECTDIR}/_ext/1360906485/app.p1  ../Src/app.c 
	@-${MV} ${OBJECTDIR}/_ext/1360906485/app.d ${OBJECTDIR}/_ext/1360906485/app.p1.d 
	@${FIXDEPS} ${OBJECTDIR}/_ext/1360906485/app.p1.d $(SILENT) -rsi ${MP_CC_DIR}../  
	
${OBJECTDIR}/_ext/1360906485/os.p1: ../Src/os.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/_ext/1360906485" 
	@${RM} ${OBJECTDIR}/_ext/1360906485/os.p1.d 
	@${RM} ${OBJECTDIR}/_ext/1360906485/os.p1 
	${MP_CC} --pass1 $(MP_EXTRA_CC_PRE) --chip=$(MP_PROCESSOR_OPTION) -Q -G  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib $(COMPARISON_BUILD)  --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"    -o${OBJECTDIR}/_ext/1360906485/os.p1  ../Src/os.c 
	@-${MV} ${OBJECTDIR}/_ext/1360906485/os.d ${OBJECTDIR}/_ext/1360906485/os.p1.d 
	@${FIXDEPS} ${OBJECTDIR}/_ext/1360906485/os.p1.d $(SILENT) -rsi ${MP_CC_DIR}../  
	
${OBJECTDIR}/_ext/1360906485/system.p1: ../Src/system.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/_ext/1360906485" 
	@${RM} ${OBJECTDIR}/_ext/1360906485/system.p1.d 
	@${RM} ${OBJECTDIR}/_ext/1360906485/system.p1 
	${MP_CC} --pass1 $(MP_EXTRA_CC_PRE) --chip=$(MP_PROCESSOR_OPTION) -Q -G  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib $(COMPARISON_BUILD)  --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"    -o${OBJECTDIR}/_ext/1360906485/system.p1  ../Src/system.c 
	@-${MV} ${OBJECTDIR}/_ext/1360906485/system.d ${OBJECTDIR}/_ext/1360906485/system.p1.d 
	@${FIXDEPS} ${OBJECTDIR}/_ext/1360906485/system.p1.d $(SILENT) -rsi ${MP_CC_DIR}../  
	
${OBJECTDIR}/_ext/1360906485/uart.p1: ../Src/uart.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/_ext/1360906485" 
	@${RM} ${OBJECTDIR}/_ext/1360906485/uart.p1.d 
	@${RM} ${OBJECTDIR}/_ext/1360906485/uart.p1 
	${MP_CC} --pass1 $(MP_EXTRA_CC_PRE) --chip=$(MP_PROCESSOR_OPTION) -Q -G  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib $(COMPARISON_BUILD)  --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"    -o${OBJECTDIR}/_ext/1360906485/uart.p1  ../Src/uart.c 
	@-${MV} ${OBJECTDIR}/_ext/1360906485/uart.d ${OBJECTDIR}/_ext/1360906485/uart.p1.d 
	@${FIXDEPS} ${OBJECTDIR}/_ext/1360906485/uart.p1.d $(SILENT) -rsi ${MP_CC_DIR}../  
	
endif

# ------------------------------------------------------------------------------------
# Rules for buildStep: assemble
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
else
endif

# ------------------------------------------------------------------------------------
# Rules for buildStep: link
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
dist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.${OUTPUT_SUFFIX}: ${OBJECTFILES}  nbproject/Makefile-${CND_CONF}.mk    
	@${MKDIR} dist/${CND_CONF}/${IMAGE_TYPE} 
	${MP_CC} $(MP_EXTRA_LD_PRE) --chip=$(MP_PROCESSOR_OPTION) -G -mdist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.map  -D__DEBUG=1 --debugger=pickit3  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"        $(COMPARISON_BUILD) --memorysummary dist/${CND_CONF}/${IMAGE_TYPE}/memoryfile.xml -odist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}  ${OBJECTFILES_QUOTED_IF_SPACED}     
	@${RM} dist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.hex 
	
else
dist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.${OUTPUT_SUFFIX}: ${OBJECTFILES}  nbproject/Makefile-${CND_CONF}.mk   
	@${MKDIR} dist/${CND_CONF}/${IMAGE_TYPE} 
	${MP_CC} $(MP_EXTRA_LD_PRE) --chip=$(MP_PROCESSOR_OPTION) -G -mdist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.map  --double=24 --float=24 --rom=2A0-7FF --opt=+asm,+asmfile,-speed,+space,-debug,-local --addrqual=require --mode=pro -P -N255 --warn=-3 --asmlist -DXPRJ_default=$(CND_CONF)  --summary=default,-psect,-class,+mem,-hex,+file --html --fill=0xFF@0x2A0:0x7FF --codeoffset=0x2A0 --output=default,-inhx032 --runtime=default,+clear,+init,-keep,-no_startup,-osccal,-resetbits,-download,-stackcall,+clib --output=-mcof,+elf:multilocs --stack=hybrid:auto:auto "--errformat=%f:%l: error: (%n) %s" "--warnformat=%f:%l: warning: (%n) %s" "--msgformat=%f:%l: advisory: (%n) %s"     $(COMPARISON_BUILD) --memorysummary dist/${CND_CONF}/${IMAGE_TYPE}/memoryfile.xml -odist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}  ${OBJECTFILES_QUOTED_IF_SPACED}     
	
	@echo Normalizing hex file
	@"C:/Program Files/Microchip/MPLABX/v4.05/mplab_ide/platform/../mplab_ide/modules/../../bin/hexmate" --edf="C:/Program Files/Microchip/MPLABX/v4.05/mplab_ide/platform/../mplab_ide/modules/../../dat/en_msgs.txt" dist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.hex -odist/${CND_CONF}/${IMAGE_TYPE}/Build.${IMAGE_TYPE}.hex

endif

.pre:
	@echo "--------------------------------------"
	@echo "User defined pre-build step: [.\..\..\Tools\prebuid.bat .\..\..\Tools\ReplaceAdd.py .\..\FwBootloader\Build\dist\default\production\Build.production.map .\..\FwApp\Src\system.h]"
	@.\..\..\Tools\prebuid.bat .\..\..\Tools\ReplaceAdd.py .\..\FwBootloader\Build\dist\default\production\Build.production.map .\..\FwApp\Src\system.h
	@echo "--------------------------------------"

# Subprojects
.build-subprojects:


# Subprojects
.clean-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r build/default
	${RM} -r dist/default

# Enable dependency checking
.dep.inc: .depcheck-impl

DEPFILES=$(shell mplabwildcard ${POSSIBLE_DEPFILES})
ifneq (${DEPFILES},)
include ${DEPFILES}
endif
