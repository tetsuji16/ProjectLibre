# ProjectLibre Development

This is an unofficial fork of the original ProjectLibre project.

We are using this repository to build a better ProjectLibre with practical improvements such as enhanced Gantt features, including the "inazuma-sen" line, UI refinements, and other usability-focused changes.

ProjectLibre is a desktop project management application built with Java and Ant. This repository contains the application sources, build scripts, packaging assets, and sample data used to build and run the app from source.

ProjectLibre now targets `Java 21` or later for both compilation and runtime. Java 8 is no longer supported.

## What Is In This Repository

- An active development fork of ProjectLibre, not the official upstream repository
- Ongoing UI and workflow improvements aimed at making day-to-day scheduling work smoother
- Feature work around Gantt interactions, including the inazuma-sen line
- `projectlibre_core`: core scheduling and model logic
- `projectlibre_ui`: Swing user interface code
- `projectlibre_reports`: reporting-related code and templates
- `projectlibre_exchange`: import/export and exchange integrations
- `projectlibre_contrib`: third-party and shared build dependencies
- `projectlibre_build`: Ant build files, launch scripts, and packaging resources
- `sample data`: example project files, including `sampledata.mpp`

## Requirements

- JDK 21 or newer
- Ant 1.10+ recommended
- A shell or terminal that can run the Ant build scripts

If `JAVA_HOME` is not already set to a JDK 21+ installation, update your environment before building or running the helper scripts.

## Quick Start

From the repository root:

```powershell
cd projectlibre_build
ant dist
```

This compiles the application and produces the main distributable output under `projectlibre_build/dist`.

To clean generated output:

```powershell
cd projectlibre_build
ant clean
```

## Common Build Targets

The main Ant file is `projectlibre_build/build.xml`.

- `ant compile`: compile Java sources
- `ant build`: compile sources and copy non-Java resources into the build output
- `ant dist`: create the main distributable JAR layout
- `ant clean`: remove generated build, dist, and package output

Additional packaging targets are also available, including archive and installer-oriented outputs such as `zip`, `tar`, `deb`, `rpm`, `mac-new`, `mac-old`, `mac-embedded`, `jpackage-win`, `jpackage-deb`, `jpackage-dmg`, and `jpackage-msi`.

## Helper Scripts

Helper scripts live under `projectlibre_build` and assume JDK 21+:

- `run-fixed.bat`: compile startup classes, update `dist/projectlibre.jar`, and launch the app
- `run-fixed2.bat`: compile startup classes and update `dist/projectlibre.jar` without launching
- `run-fixed3.bat`: compile startup classes only and report success or failure
- `test-build.bat`: compile startup classes and exit nonzero on failure
- `build_ant.bat`: run the Ant `dist` target from Windows
- `compile-sources.bat`: compile sources with the local classpath setup

## Runtime Notes

- The Swing UI may use Java 9+ APIs such as `java.awt.desktop` and `Taskbar`.
- The app has been tested against Java 21 as the minimum target and also runs on newer JDKs.
- Launchers use the JVM's default heap ergonomics unless you override them.
- Set `PROJECTLIBRE_JAVA_OPTS` or `JAVA_OPTS` if you need custom JVM flags.

## Distribution Output

Generated files are written to the build directories managed by Ant:

- `build`: compiled classes and copied resources
- `dist`: the runnable application layout
- `packages`: archives and installer artifacts

For packaged distributions, the build also includes bundled license material from `projectlibre_build/license`.

## Working With Sample Data

- `sampledata.mpp` is a ready-to-open example project file in the repository root
- Additional sample assets used by packaging live under `projectlibre_build/resources`

## Gantt Controls

- In the Gantt and Tracking Gantt views, `Ctrl + mouse wheel` zooms the time scale.
- Zooming uses the currently visible left edge of the Gantt pane as its anchor.
- If you zoom out and then zoom back in without horizontally scrolling or dragging Gantt bars, each zoom-in step restores the previous left-edge date.

## Troubleshooting

- If the build fails immediately, confirm that `java -version` reports JDK 21 or newer.
- If the helper scripts cannot find Java, confirm that `JAVA_HOME` points to a full JDK, not just a JRE.
- If packaging output looks stale, run `ant clean` before rebuilding.
- For build and packaging details, see `projectlibre_build/doc/building.html`.

## License

ProjectLibre is distributed under its project license and includes bundled third-party notices. See `projectlibre_build/license` for the full license material.
