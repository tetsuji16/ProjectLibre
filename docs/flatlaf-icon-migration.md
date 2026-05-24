# FlatLaf SVG Icon Migration Notes

This repository now supports loading `.svg` icons through `IconManager`, with raster images as a fallback.

## Priority 1

- `menu.new`
- `menu.open`
- `menu.save`
- `menu.print`
- `menu.undo`
- `menu.redo`
- `menu.cut`
- `menu.copy`
- `menu.paste`
- `menu.delete`
- `menu.find`
- `menu.link`
- `menu.unlink`

## Priority 2

- `view.gantt`
- `view.calendar`
- `view.network`
- `view.histogram`
- `view.charts`
- `view.resources`
- `view.projects`

## Priority 3

- `dialog.ok`
- `dialog.cancel`
- `timescale.zoomIn.icon`
- `timescale.zoomOut.icon`
- `application.icon`

## Notes

- Keep the existing raster resources in place until the SVG versions are verified on HiDPI displays.
- Prefer replacing the most frequently used toolbar icons first, because they affect the largest part of the UI.
- If an SVG is missing, the runtime will continue to use the existing PNG/GIF resource automatically.
