# ProjectLibre Development

ProjectLibre now targets `Java 26` for both compilation and runtime. Java 8 is no longer supported.

## Requirements

- JDK 26
- Ant

## Standard Build

Use the main Ant build from `projectlibre_build/build.xml`.

```powershell
cd projectlibre_build
ant build
```

To produce the distributable JAR:

```powershell
cd projectlibre_build
ant dist
```

## Helper Scripts

Helper scripts live under `projectlibre_build` and also assume JDK 26:

- `run-fixed.bat`: compile startup classes, update `dist/projectlibre.jar`, and launch the app
- `run-fixed2.bat`: compile startup classes and update `dist/projectlibre.jar` without launching
- `run-fixed3.bat`: compile startup classes only and report success/failure
- `test-build.bat`: compile startup classes and exit nonzero on failure

## Notes

- The Swing UI code may use Java 9+ APIs such as `java.awt.desktop` and `Taskbar`.
- If `JAVA_HOME` is not already set to JDK 26, update the helper scripts or your shell environment before building.
