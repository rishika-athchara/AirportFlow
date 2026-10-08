@echo off
rem Windows build: compile main + tests into out\
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java test\*.java > sources.txt
javac -d out @sources.txt && del sources.txt && echo Build OK. Run: java -cp out airportflow.Main
