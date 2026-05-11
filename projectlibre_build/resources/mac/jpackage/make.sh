#!/bin/bash

APP_VERSION="@version@"

rm -rf app
mkdir -p app
rm -f ProjectLibre-${APP_VERSION}.dmg
jpackage --type app-image --input source --dest app --name ProjectLibre --main-jar projectlibre-${APP_VERSION}.jar --icon source/projectlibre.icns --app-version ${APP_VERSION}
cp Info.plist app/ProjectLibre.app/Contents/

# 1. Sign all native Mach-O files (except jspawnhelper and libjli.dylib):
find app/ProjectLibre.app/Contents/runtime -type f \
  -exec file "{}" \; | grep "Mach-O" | cut -d: -f1 \
| while read -r file; do
  echo "Signing $file"
  codesign --force --timestamp --options runtime --options runtime --entitlements entitlements.plist \
    --sign "Developer ID Application: ProjectLibre Inc. ($APPLE_TEAM_ID)" "$file"
done

codesign --force --timestamp --options runtime \
  --entitlements entitlements.plist \
  --sign "Developer ID Application: ProjectLibre Inc. ($APPLE_TEAM_ID)" "app/ProjectLibre.app/Contents/MacOS/ProjectLibre"


codesign --force --timestamp --options runtime \
  --entitlements entitlements.plist \
  --preserve-metadata=entitlements,requirements,flags \
  --sign "Developer ID Application: ProjectLibre Inc. ($APPLE_TEAM_ID)" "app/ProjectLibre.app/Contents/MacOS/ProjectLibre"



create-dmg --volname "ProjectLibre" --background "background.png" --volicon "source/projectlibre.icns" --window-pos 200 120 --window-size 600 350 --icon-size 100 --icon "ProjectLibre.app" 180 140 --hide-extension "ProjectLibre.app" --app-drop-link 420 140 --eula source/license/license.txt "ProjectLibre-${APP_VERSION}.dmg" "app"
codesign --force --timestamp --options runtime --sign "Developer ID Application: ProjectLibre Inc. ($APPLE_TEAM_ID)" ProjectLibre-${APP_VERSION}.dmg
xcrun notarytool submit ProjectLibre-${APP_VERSION}.dmg --keychain-profile "notary-profile" --wait
