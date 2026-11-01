# Releasing a Guardian test build

This checklist covers a direct APK release through GitHub. The current GitHub release, tagged `v1.0.5-test`, contains a debug-signed test APK for demonstrations and device testing.

## 1. Prepare the version

1. Confirm the code and manual device checks are complete. Use the steps in [Installation and testing](INSTALL_AND_TEST.md).
2. Increment `versionCode` and set the intended `versionName` in `app/build.gradle.kts`. Android requires a higher version code for an update.
3. Review permissions, map/model notices, and the feature list in `README.md` and `THIRD_PARTY_NOTICES.md`.

## 2. Choose a signing key

- **Local/demo build:** `assembleDebug` uses Android’s debug signing key. Do not describe a debug-signed APK as a production release.
- **GitHub test release:** A stable private release key makes later updates installable over the same signed build. Keep the keystore and passwords outside Git, back them up securely, and never send them with the APK.
- A phone cannot update an install signed with one key using an APK signed with another key. Test key changes on a spare device first; uninstalling the old copy deletes its local data.

The release signing block in `app/build.gradle.kts` reads `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` from the build environment and uses alias `upload`. Set those values only in your local build environment or a protected CI secret store.

If you do not already have a release keystore, create one locally. `keytool` prompts for the passwords and certificate details:

```powershell
keytool -genkeypair -v -keystore "C:\secure\guardian-upload.jks" -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

## 3. Build and verify

On Windows PowerShell, set the environment values for the current terminal and build:

```powershell
$env:KEYSTORE_PATH = "C:\secure\guardian-upload.jks"
$env:STORE_PASSWORD = "<set your actual keystore password locally>"
$env:KEY_PASSWORD = "<set your actual key password locally>"
.\gradlew.bat :app:assembleRelease
```

The APK is written to `app/build/outputs/apk/release/app-release.apk`. Before sharing it:

1. Verify its signature with Android SDK Build Tools:

   ```powershell
   apksigner verify --verbose --print-certs .\app\build\outputs\apk\release\app-release.apk
   ```

2. Check alignment:

   ```powershell
   zipalign -c 4 .\app\build\outputs\apk\release\app-release.apk
   ```

3. Generate the download checksum:

   ```powershell
   Get-FileHash .\app\build\outputs\apk\release\app-release.apk -Algorithm SHA256
   ```

4. Install and exercise the exact APK you plan to publish. Do not reuse the checksum from an older release.

## 4. Publish a GitHub Release

1. Push the reviewed source commit to `main`.
2. In GitHub, open **Releases → Draft a new release** and create a tag such as `v1.0.5-test` for the tested version.
3. Attach the verified APK as a release asset. GitHub Releases hosts the download; do not commit the APK or signing key to the source tree.
4. Add concise notes: app version, what changed, APK size, SHA-256, and the installation guide link.
5. Mark it as a prerelease/test build until it is intended for general users.
6. Update the APK link and checksum in `README.md` and `docs/INSTALL_AND_TEST.md`.

## Google Play distribution

Google Play publication is a separate path and needs a Play Console developer account and its policy declarations. Guardian uses `SEND_SMS` for user-triggered emergency alerts; Google’s physical-safety/emergency-alert use case is subject to declaration and review. Students can use Android Developer Console limited distribution for up to 20 authorized devices without the Play Console fee; see [Installation and testing](INSTALL_AND_TEST.md).
