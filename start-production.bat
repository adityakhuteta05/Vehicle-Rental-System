@echo off
echo Starting DriveSense Platform in Production Mode...
set PORT=8080
java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar target/drivesense-platform-1.0.0.jar
pause
