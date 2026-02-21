# Start Backend Server Script
# This script starts the Spring Boot backend with the correct Java configuration

Write-Host "🍃 Starting Spring Boot Backend..." -ForegroundColor Green
Write-Host "Port: 8080" -ForegroundColor Cyan
Write-Host ""

# Set JAVA_HOME for this session
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"

# Navigate to backend directory and start
Set-Location -Path "$PSScriptRoot\backend"
.\mvnw.cmd spring-boot:run
