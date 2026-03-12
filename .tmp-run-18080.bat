@echo off
cd /d C:\final-project
gradlew.bat bootRun --args=--server.port=18080 > C:\final-project\.tmp-boot-18080.log 2>&1
