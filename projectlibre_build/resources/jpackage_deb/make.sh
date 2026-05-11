#!/bin/bash

APP_VERSION="@version@"

rm -rf app
mkdir -p app
rm -f ProjectLibre-${APP_VERSION}.rpm
jpackage --type deb --input source --dest app --name ProjectLibre --main-jar projectlibre-${APP_VERSION}.jar --icon source/projectlibre.png --app-version ${APP_VERSION} --license-file source/license/license.txt --vendor "ProjectLibre" --linux-package-name projectlibre --linux-shortcut --linux-menu-group "Utility"
