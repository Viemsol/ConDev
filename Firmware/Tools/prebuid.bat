Echo Prebuilding..
set ADDREPLACE=%1
set INFILE=%2
set OUTFILE=%3
@REM replace addresses in application system.h file
%ADDREPLACE% %INFILE% %OUTFILE%
