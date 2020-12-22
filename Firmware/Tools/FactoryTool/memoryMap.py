FLASH_WORD_SIZE    =   2       # each address hold two bytes of data in flash
FLASH_START_ADD    =   0x0000
BL_START_ADD       =   FLASH_START_ADD
BL_END_ADD         =   0x029F           # bootloader start from 0x00 and end at 0x29F
APP_START_ADD      =   BL_END_ADD + 1    # app strtas from 0x2A0
APP_END_ADD        =   0x07DF
KEY_START_ADD      =    APP_END_ADD + 1  # App starts at 2A0 and end at 0x7CF
FLASH_END_ADD      =   0x07FF
KEY_END_ADD        =   FLASH_END_ADD

CONFIG_START_ADD   =   0x8007
CONFIG_END_ADD     =   0x800A

EEP_START_ADD      =   0xF000
EEP_APP_VALID_ADD  =   0xF003
EEP_KEY_START_ADD  =   0xF004
EEP_END_ADD        =   0xF0FF


FLASH_SIZE         =   (FLASH_END_ADD - FLASH_START_ADD + 1)*2
EEP_SIZE           =   (EEP_END_ADD - EEP_START_ADD + 1)           # eeprom one byte is unused 
CONFIG_SIZE        =   (CONFIG_END_ADD - CONFIG_START_ADD + 1)*2

BL_SIZE            =   (BL_END_ADD - BL_START_ADD + 1)*FLASH_WORD_SIZE
APP_SIZE           =   (APP_END_ADD - APP_START_ADD + 1)*FLASH_WORD_SIZE
KEY_SIZE           =   (KEY_END_ADD - KEY_START_ADD + 1)*FLASH_WORD_SIZE

BL_VER_ADD         =   BL_END_ADD
APP_VER_ADDRESS    =   KEY_START_ADD - 3
APP_CRC_ADDRESS    =   (KEY_START_ADD - 2)