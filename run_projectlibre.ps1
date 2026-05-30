# ProjectLibre launcher script
# Usage: .\run_projectlibre.ps1

$ErrorActionPreference = "Stop"

Write-Host "Starting ProjectLibre..." -ForegroundColor Cyan

# Kill any existing Java processes
$javaProcs = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcs) {
    Write-Host "Stopping existing Java processes..." -ForegroundColor Yellow
    Stop-Process -Name java -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

# Run via Gradle
$gradleCmd = ".\gradlew.bat"
$gradleArgs = @(":projectlibre_ui:run")

Write-Host "Executing: $gradleCmd $gradleArgs" -ForegroundColor Gray

# Start Gradle in background and capture output
$process = Start-Process -FilePath $gradleCmd -ArgumentList $gradleArgs -NoNewWindow -PassThru -Wait

# Give the app a moment to start
Start-Sleep -Seconds 3

# Try to bring window to foreground
$javaProcs = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcs) {
    foreach ($proc in $javaProcs) {
        if ($proc.MainWindowHandle -ne 0) {
            try {
                $sig = @"
[DllImport("user32.dll")]
public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
[DllImport("user32.dll")]
public static extern bool SetForegroundWindow(IntPtr hWnd);
"@
                $win32 = Add-Type -MemberDefinition $sig -Name "Win32" -Namespace "Win32" -PassThru
                $win32::ShowWindow($proc.MainWindowHandle, 9)  # SW_RESTORE
                $win32::SetForegroundWindow($proc.MainWindowHandle)
            } catch {
                Write-Host "Note: Could not activate window (this is normal if already visible)" -ForegroundColor Gray
            }
            break
        }
    }
    Write-Host "ProjectLibre is running." -ForegroundColor Green
} else {
    Write-Host "Warning: ProjectLibre may not have started. Check for errors above." -ForegroundColor Yellow
}