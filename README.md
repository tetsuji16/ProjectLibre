# ProjectLibre Development Fork

This repository is an unofficial development fork of ProjectLibre. It keeps the original desktop planning tool as its base and extends it with practical improvements for day-to-day scheduling, collaboration, import/export, and Gantt usability.

![ProjectLibre Gantt view](docs/images/gantt-view.png)

## What This Fork Is For

- Keep ProjectLibre usable on a modern JDK and packaging toolchain
- Improve real-world planning workflows instead of only preserving legacy behavior
- Add collaboration-oriented features for teams sharing project files locally
- Make the Gantt and spreadsheet views smoother to use for large schedules

## Baseline And Update Ratio

The comparison baseline in this fork is the initial commit:

- Commit: `d2fa3c20a28902fd21046085a1f5b0c9e02e14dc`
- Date: `2012-08-22`
- Subject: `Initial commit`

The figures below describe cumulative change volume since that initial snapshot. Because the repository has grown substantially through added files, rewrites, and renames, these ratios can exceed `100%`.

- Changed tracked file paths since the initial commit: `8049 / 4561` (`176.5%` of the initial tracked file count)
- Changed tracked text lines since the initial commit: `1370893 / 570682` (`240.2%` of the initial tracked text-line count)

## What Has Been Added Or Improved

- XLSX import/export support and related collaboration refresh behavior
- Local collaboration support for shared project files
- Gantt and Tracking Gantt progress-line improvements
- Gantt zoom restore behavior
- Mouse-wheel and horizontal scrolling refinements
- Startup and initial view loading fixes
- Logging cleanup, resource-leak fixes, and modernized build/CI setup

## Repository Layout

- `projectlibre_core`: scheduling engine, data model, collaboration logic, and configuration
- `projectlibre_ui`: Swing UI, Gantt rendering, spreadsheet views, menus, and startup flow
- `projectlibre_exchange`: file exchange, import/export, and format integration code
- `projectlibre_reports`: report-related code and templates
- `projectlibre_contrib`: shared third-party dependencies built into the app distribution
- `projectlibre_build`: Ant build files, jpackage helpers, packaging assets, and licenses
- `sample data`: sample project files for screenshots and manual verification

## Requirements

- Windows with a full JDK that includes `jpackage`
- JDK 21 or newer recommended
- Apache Ant 1.10+
- WiX Toolset on `PATH` for MSI packaging

If `JAVA_HOME` is not set, pass a JDK path explicitly when running the MSI packaging script.

## Build The App

From the repository root:

```powershell
cd projectlibre_build
ant compile
ant dist
```

This does two important things:

- Rebuilds the contrib JARs so stale or damaged local artifacts do not block packaging
- Produces the runnable desktop layout in `projectlibre_build/dist`

The main runnable JAR is:

- `projectlibre_build/dist/projectlibre.jar`

## Build The Windows Release

Generate the MSI packaging input:

```powershell
cd projectlibre_build
ant jpackage-msi
```

Then create the MSI itself:

```powershell
cd projectlibre_build
powershell -ExecutionPolicy Bypass -File .\packages\jpackage-msi\make.ps1 -PackageType msi -OutputDir .\packages\jpackage-msi\app -JavaHome "C:\Program Files\Java\jdk-26.0.1"
```

The official Windows release artifact is:

- `projectlibre_build/packages/jpackage-msi/app/ProjectLibre-1.9.8.1.msi`

If WiX was installed per-user rather than system-wide, make sure its `bin` directory is on `PATH` before running `make.ps1`. For example:

```powershell
$env:PATH = "$env:LOCALAPPDATA\Programs\WiX Toolset v7.0\bin;$env:PATH"
```

## Screenshot Procedure Used In This Repository

The README screenshot is intentionally captured so that only the application UI is visible.

- Launch the app by itself, not the whole desktop workspace
- Open `sampledata.mpp`
- Arrange the main Gantt view at a readable zoom level
- Capture only the app window client area so title-bar paths, taskbar items, IDE windows, notifications, and personal information do not appear

## Quick Verification

- `ant compile`: compile succeeds
- `ant dist`: distribution JAR and runtime libs are refreshed
- `ant jpackage-msi`: MSI input layout is generated
- `make.ps1`: MSI installer is generated
- App launch check: the packaged app opens and reaches the Gantt screen with sample data

## License

This fork builds on ProjectLibre and keeps the original license materials and third-party notices in `projectlibre_build/license`.
