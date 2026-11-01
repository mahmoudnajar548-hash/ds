# Starts CourseHub on an in-memory H2 database (no MySQL required).
#
# Use this to try the API, run the Postman collection and take screenshots.
# The graded project still runs on MySQL with `mvn spring-boot:run`.
#
# Usage:
#   pwsh -File scripts/run-h2.ps1              # port 8081
#   pwsh -File scripts/run-h2.ps1 -Port 8080   # another port
#
# Then set the Postman variable `baseUrl` to http://localhost:<port>

param(
    [int]$Port = 8081
)

$ErrorActionPreference = 'Stop'
Push-Location (Split-Path -Parent $PSScriptRoot)

try {
    Write-Host "Building with Maven..." -ForegroundColor Cyan
    mvn -B -q clean test-compile
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed - fix the compile errors above first." }

    Write-Host ""
    Write-Host "Starting CourseHub on H2 at http://localhost:$Port" -ForegroundColor Green
    Write-Host "Postman: set baseUrl to http://localhost:$Port" -ForegroundColor Green
    Write-Host "Stop with Ctrl+C." -ForegroundColor DarkGray
    Write-Host ""

    mvn -B spring-boot:test-run "-Dspring-boot.run.profiles=test" "-Dspring-boot.run.arguments=--server.port=$Port"
}
finally {
    Pop-Location
}
