@echo off
echo =========================================================
echo    DriveSense Platform - Production Runner (MySQL)
echo =========================================================
echo.

REM Set custom configuration (or customize to match your MySQL server)
set PORT=8080
set SPRING_PROFILES_ACTIVE=mysql
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=drivesense
set DB_USER=root
set DB_PASSWORD=root

echo [1/2] Connecting to MySQL at %DB_HOST%:%DB_PORT%/%DB_NAME%...
echo [2/2] Starting Spring Boot application on port %PORT%...
echo.
echo Application will be accessible at: http://localhost:%PORT%/
echo Press Ctrl+C to stop the server anytime.
echo.

java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar target/drivesense-platform-1.0.0.jar

pause
