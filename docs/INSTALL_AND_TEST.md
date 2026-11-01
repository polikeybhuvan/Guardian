# Install and test Guardian

This guide is for hackathon reviewers and people testing Guardian on an Android phone. Guardian is Android-only.

## Fast install options

Both options install the same Guardian APK. Start with the normal Android install prompt. Use the second option only if Play Protect blocks this verified test build.

### Option 1: Normal direct install

Download the [Guardian 1.0.5 test APK](https://github.com/polikeybhuvan/Guardian/releases/download/v1.0.5-test/Guardian-Android-Test-1.0.5.apk) from GitHub, verify its SHA-256, open it from Downloads, and follow Android’s installer prompts. If asked, allow the browser or file manager to install unknown apps.

### Option 2: Direct install if Play Protect blocks the APK

Use this only for the Guardian APK downloaded from the release link above, after verifying its SHA-256. Turn Play Protect scanning back on immediately after installation.

1. On the phone, open the **Google Play Store** and tap the profile icon.
2. Open **Play Protect**, then tap the settings gear.
3. Temporarily turn off **Scan apps with Play Protect** and confirm the prompt. Menu names can vary slightly by Android/Play Store version. This only pauses the app scan so the APK can be installed; it does not grant Guardian SMS access.
4. Open the verified Guardian APK from Downloads and install it.
5. Return to **Play Protect settings** and turn **Scan apps with Play Protect** back on.

See [Google Play Protect help](https://support.google.com/googleplay/answer/2812853?hl=en) if the menus differ. If you prefer not to change Play Protect settings, use the ADB route below.

### Allow direct SMS for an ADB test install

Android treats `SEND_SMS` as a hard-restricted permission. A browser or file-manager install may be denied direct SMS access; disabling Play Protect scanning does not grant this permission. Guardian sends directly with Android `SmsManager` when the permission is granted. If Android blocks it, Guardian reports that SMS was not sent and keeps the SOS saved locally; it does not open another messaging app. For an authorized ADB test device, connect it with USB debugging enabled and run this after installation:

```powershell
.\adb.exe shell pm grant com.aistudio.sosmesh.kxmbpv android.permission.SEND_SMS
```

On macOS or Linux, run this from the extracted `platform-tools` folder:

```bash
./adb shell pm grant com.aistudio.sosmesh.kxmbpv android.permission.SEND_SMS
```

This grants Guardian SMS access on that phone if Android permits the ADB grant. If it remains unavailable, Guardian still saves the SOS locally and reports that direct SMS is blocked. Android defines `SEND_SMS` as a hard-restricted permission that can be granted only when the installer allowlists it; see the [Android permission reference](https://developer.android.com/reference/android/Manifest.permission#SEND_SMS).

## Verify the APK

The APK is about **1.57 GiB**. Keep several GB of free space available because the app prepares the map and AI model in app storage.

Before installing, verify the downloaded file’s SHA-256 hash:

```powershell
Get-FileHash .\Guardian-Android-Test-1.0.5.apk -Algorithm SHA256
```

Expected hash:

```text
1A929685EA02BF424D4B7507F6C332BC935C8E51E06154C4BB1A006ACF686581
```

## Other installation routes

### Route 3: Install with Android Debug Bridge (ADB)

ADB is Android’s standard developer workflow for installing and testing an APK. It worked on the project owner’s Nothing phone.

1. Download [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools) on a computer and extract the folder.
2. On the phone, enable **Developer options** and **USB debugging**. Connect it by USB, unlock it, and approve the computer’s authorization prompt.
3. Open PowerShell in the extracted `platform-tools` folder. Confirm the phone is listed as `device`:

   ```powershell
   .\adb.exe devices
   ```

4. Install the APK. Replace the example path with the APK’s actual location:

   ```powershell
   .\adb.exe install -r "C:\Users\YourName\Downloads\Guardian-Android-Test-1.0.5.apk"
   ```

For macOS or Linux, use `./adb` instead of `.\adb.exe`:

```bash
./adb devices
./adb install -r ~/Downloads/Guardian-Android-Test-1.0.5.apk
```

If more than one device is connected, select the right serial with `adb -s SERIAL install -r /path/to/Guardian-Android-Test-1.0.5.apk`. `-r` updates an existing install only when Android accepts its signing key; if Android reports a signature conflict, uninstall the old copy and install again. Uninstalling removes that phone’s local Guardian data.

ADB is a developer installation route, not a Play Store review or malware certification. Play Protect can still scan apps after installation.

### Route 4: Free limited distribution for a small tester group

Google’s [Android Developer Console limited-distribution account](https://developer.android.com/developer-verification/guides/limited-distribution) is designed for students and hobbyists. It is free and supports up to **20 explicitly authorized devices**. It requires a Google account with 2-Step Verification, a linked payments profile, and a contact email; it does not require government ID.

Create the limited account, register the app package and signing key as prompted, then authorize each tester’s phone through the Console’s QR-code or link flow. Share the same GitHub APK with those authorized testers. This is limited sharing, not public Play Store publishing, and it may not suppress a separate Play Protect warning for SMS permission. If package registration or installation is blocked, use Route 3 (ADB) for the hackathon test.

For unrestricted public distribution through Google Play, a Play Console developer account is required. Google currently charges a one-time **US$25** registration fee; Guardian would also need to complete Google Play’s SMS-permission declaration and review. See [Google’s account setup requirements](https://support.google.com/googleplay/android-developer/answer/6112435?hl=en-EN).

## First launch setup

1. Open Guardian and follow the permission setup prompts. Grant only the permissions needed for the features you plan to test.
2. Add an emergency contact who has agreed to receive test messages.
3. Allow Guardian to send SMS when Android asks. If Android leaves SMS unavailable for this direct install, grant it with the ADB command above on your authorized test device. Guardian reports blocked direct SMS without launching a messaging app.
4. Confirm location is enabled and allow Guardian to use it for the SOS and map.
5. Keep a working SIM and cellular service available when testing SMS. Wi-Fi alone does not send SMS.
6. Allow the map/model setup to finish. The first map opening copies the bundled India map into app storage; AID prepares the bundled Gemma model for offline use.

## Short test checklist

Use a test contact who explicitly agreed beforehand. Put `DEMO` in the test incident. Never use **112** as a test contact or place a test call to emergency services.

| Test | Steps | Expected result |
| --- | --- | --- |
| SOS and SMS | Add the consenting test contact, start a demo SOS, let the five-second cancel window finish, and confirm the message. | Guardian shows the SMS submission result and includes available incident/location details. “Submitted” does not prove the recipient received it. |
| Offline map | Finish first map setup, enable GPS/location, turn off Wi-Fi and mobile data, and open Map. | Offline India map appears and the GPS marker updates when a location fix is available. A first GPS fix may take time outdoors. |
| AID chat | With mobile data off, ask an emergency-related question and describe the situation, symptoms, and timing. | Guardian answers from the on-device Gemma model when prepared; emergency guidance remains available offline. |
| Rescue tools | Open the single Rescue Tools section and try Siren, Morse Audio, Torch SOS, Floodlight, Medical ID, and Compass HUD. | Each tool responds to its control; stop audio/torch/screen effects after the demonstration. |
| Nearby relay | Install Guardian on two phones, enable the requested nearby permissions and radios, then send a harmless test packet. | The two Guardian devices can discover/test relay when their radios and permissions allow it. Phones without Guardian do not receive app relay messages. |

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| `adb devices` shows `unauthorized` | Unlock the phone, accept the USB debugging prompt, then run the command again. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | The installed app was signed with a different key. Uninstall it and install this build; local app data will be erased. |
| Not enough storage | Free several GB, then retry. The APK is 1.57 GiB and the app creates app-private copies of its offline assets. |
| Play Protect blocks Chrome/Files installation | Verify the APK SHA-256. Use Option 2 to pause scanning just for this install and re-enable it immediately, or use the ADB route if you prefer to keep scanning enabled. |
| Map does not appear | Confirm first-run map setup completed, location permission and GPS are enabled, and the phone has enough free storage. Mobile data is not required to render the bundled map. |
| Location is missing or inaccurate | Enable device location, grant Guardian location permission, and wait for a GPS fix; test outdoors with a clear view of the sky. |
| SMS is not submitted | Check the saved number, SMS permission, SIM, cellular signal, and carrier support. For an APK install with SMS permission unavailable, use the ADB grant command above on your authorized test device. An app submission status is not a carrier delivery receipt. |
| AID is still preparing | Keep the app open and allow time for the 584 MB model to be copied and initialized. No Gemini API key is used. |
| Relay finds no peer | Confirm Guardian is installed on the second phone, nearby permissions are granted, and Bluetooth/Wi-Fi radios are on. Test with both phones close together. |

In a real emergency in India, call **112** when possible. Guardian is a helper tool and does not dispatch official responders.
