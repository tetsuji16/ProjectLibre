param(
    [ValidateSet("msi", "app-image")]
    [string]$PackageType = "msi",
    [string]$OutputDir = "app",
    [string]$JavaHome = $env:JAVA_HOME
)

$AppVersion = "@version@"
$RuntimeModules = "@jpackage_modules@"
$BundledJavaHome = "@bundled_jdk_windows@"

if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    $JavaHome = $BundledJavaHome
}

$JpackagePath = Join-Path $JavaHome "bin\jpackage.exe"

if (-not (Test-Path $JpackagePath)) {
    Write-Error "jpackage not found. Make sure JAVA_HOME is set to a valid JDK 14+ path."
    exit 1
}

# --- Create Output Directory ---
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$jpackageArgs = @(
    "--type", $PackageType,
    "--name", "ProjectLibre",
    "--app-version", $AppVersion,
    "--input", "source",
    "--main-jar", "projectlibre-$AppVersion.jar",
    "--icon", "source/projectlibre.ico",
    "--license-file", "source/license/license.txt",
    "--add-modules", $RuntimeModules,
    "--dest", $OutputDir,
    "--verbose"
)

if ($PackageType -eq "msi") {
    $jpackageArgs += @(
        "--file-associations", "pod.properties",
        "--file-associations", "mpp.properties",
        "--file-associations", "xml.properties",
        "--win-menu",
        "--win-shortcut",
        "--win-dir-chooser"
    )
}

& $JpackagePath @jpackageArgs

Write-Host "$PackageType package created in '$OutputDir'" -ForegroundColor Green
