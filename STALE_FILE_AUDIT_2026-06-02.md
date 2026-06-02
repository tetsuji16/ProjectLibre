# Stale File Audit 2026-06-02

Cutoff rule:

- A file is considered stale when its last Git commit date is earlier than `2025-06-02`

Summary:

- Total stale tracked paths found: `1247`
- Classification approach: `delete`, `modernize`, `keep`
- This audit intentionally avoids deleting old-but-active Java source files in `projectlibre_core`, `projectlibre_ui`, and `projectlibre_exchange`

## Counts By Top-Level Area

| Area | Count |
| --- | ---: |
| `projectlibre_core` | 523 |
| `projectlibre_ui` | 430 |
| `projectlibre_exchange` | 104 |
| `projectlibre_build` | 74 |
| `projectlibre_contrib` | 60 |
| `.idea` | 47 |
| `projectlibre_reports` | 7 |
| `.project` | 1 |
| `projectlibre_desktop.iml` | 1 |

## Delete

Tracked IDE metadata removed from version control:

- `.idea/**`
- `.project`
- `projectlibre_desktop.iml`
- `projectlibre_build/.classpath`
- `projectlibre_build/.project`
- `projectlibre_build/.settings/**`
- `projectlibre_contrib/.classpath`
- `projectlibre_contrib/.project`
- `projectlibre_contrib/.settings/**`
- `projectlibre_core/.classpath`
- `projectlibre_core/.project`
- `projectlibre_core/.settings/**`
- `projectlibre_exchange/.classpath`
- `projectlibre_exchange/.project`
- `projectlibre_exchange/.settings/**`
- `projectlibre_reports/.classpath`
- `projectlibre_reports/.project`
- `projectlibre_reports/.settings/**`
- `projectlibre_ui/.classpath`
- `projectlibre_ui/.project`
- `projectlibre_ui/.settings/**`

Tracked and local release binaries removed from `docs/downloads`:

- `docs/downloads/ProjectLibre-0.0.1-app-image.zip`
- `docs/downloads/ProjectLibre-0.0.1.msi`
- `docs/downloads/ProjectLibre-0.0.2-app-image.zip`
- `docs/downloads/ProjectLibre-0.0.2.msi`
- `docs/downloads/ProjectLibre-1.9.8.1-app-image.zip`
- `docs/downloads/ProjectLibre-1.9.8.1.exe`
- `docs/downloads/ProjectLibre-1.9.8.1.msi`

Residual local split EXE parts:

- `docs/downloads/ProjectLibre-0.0.2.exe.part*`
- These files are untracked local artifacts. Some were locked by the OS during cleanup, so they are now ignored to keep them out of future commits.

Legacy packaging assets removed because the current Gradle flow does not reference them:

- `projectlibre_build/.cvsignore`
- `projectlibre_build/resources/README_SF.txt`
- `projectlibre_build/resources/deb/**`
- `projectlibre_build/resources/fx/**`
- `projectlibre_build/resources/jpackage_deb/**`
- `projectlibre_build/resources/mac/**`
- `projectlibre_build/resources/projectlibre`
- `projectlibre_build/resources/projectlibre.bat`
- `projectlibre_build/resources/projectlibre.desktop`
- `projectlibre_build/resources/projectlibre.png`
- `projectlibre_build/resources/projectlibre.sh`
- `projectlibre_build/resources/projectlibre.xml`
- `projectlibre_build/resources/readme.html`
- `projectlibre_build/resources/readme.txt`
- `projectlibre_build/resources/rpm/**`
- `projectlibre_build/resources/samples/**`
- `projectlibre_build/resources/win/**`
- `projectlibre_build/resources/x-projectlibre.desktop`
- `projectlibre_build/src/projectlibre-1.9.1.tar.gz`

## Modernize

Files retained but clarified as historical or current staging only:

- `README.md`
  - Documents Gradle as the only supported build path
  - Clarifies that `docs/downloads/` is a local staging area, not a committed binary archive
- `docs/index.html`
  - Removes repository-hosted legacy binary links
  - Points all public download traffic to GitHub Releases
- `.gitignore`
  - Prevents future release binaries from being re-added under `docs/downloads/`
- `projectlibre_build/build.xml`
  - Explicitly treated as historical reference only

## Keep

Stale paths intentionally preserved:

- `projectlibre_core/**`
- `projectlibre_ui/**`
- `projectlibre_exchange/**`
- `projectlibre_reports/**`
- `projectlibre_contrib/lib/**`
- `projectlibre_contrib/build.gradle.kts`
- `projectlibre_build/license/**`
- `projectlibre_build/resources/wix/msi_images/projectlibre.ico`

Reasoning:

- These files are either active source code, active dependency inputs, or assets still referenced by the current Gradle and `jpackage` workflow.
