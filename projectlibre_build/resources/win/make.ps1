$AppVersion = "@version@"
$OutputDir = "app"

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"


$JpackagePath = Join-Path $env:JAVA_HOME "bin\jpackage.exe"

if (-not (Test-Path $JpackagePath)) {
    Write-Error "jpackage not found. Make sure JAVA_HOME is set to a valid JDK 14+ path."
    exit 1
}

# --- Create Output Directory ---
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

& $JpackagePath `
    --type msi `
    --name ProjectLibre `
    --app-version $AppVersion `
    --input source `
    --main-jar projectlibre-$AppVersion.jar `
    --icon source/projectlibre.ico `
    --license-file source/license/license.txt `
    --file-associations "pod.properties" `
    --file-associations "mpp.properties" `
    --file-associations "xml.properties" `
    --dest $OutputDir `
    --win-menu `
    --win-shortcut `
    --win-dir-chooser `
    --verbose

Write-Host "MSI installer created in '$OutputDir'" -ForegroundColor Green