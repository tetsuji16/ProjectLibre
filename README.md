# ProjectLibre Development

ProjectLibre now targets `Java 21` or later for both compilation and runtime. Java 8 is no longer supported.

## Requirements

- JDK 21+
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

Helper scripts live under `projectlibre_build` and also assume JDK 21+:

- `run-fixed.bat`: compile startup classes, update `dist/projectlibre.jar`, and launch the app
- `run-fixed2.bat`: compile startup classes and update `dist/projectlibre.jar` without launching
- `run-fixed3.bat`: compile startup classes only and report success/failure
- `test-build.bat`: compile startup classes and exit nonzero on failure

## Notes

- The Swing UI code may use Java 9+ APIs such as `java.awt.desktop` and `Taskbar`.
- The app is tested against Java 21 as the minimum target and can also run on newer JDKs such as Java 26.
- Launchers now use the JVM's default heap ergonomics; set `PROJECTLIBRE_JAVA_OPTS` or `JAVA_OPTS` if you need custom JVM flags.
- If `JAVA_HOME` is not already set to JDK 21+, update the helper scripts or your shell environment before building.

## Gantt Controls

- In the Gantt and Tracking Gantt views, `Ctrl + mouse wheel` zooms the time scale.
- Zooming uses the currently visible left edge of the gantt pane as its anchor.
- If you zoom out and then zoom back in without horizontally scrolling or dragging gantt bars, each zoom-in step restores the previous left-edge date.
