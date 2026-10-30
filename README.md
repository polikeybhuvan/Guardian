# Guardian

**Prepared to act when help is still on the way.** Guardian is an Android emergency-support prototype combining contact-based SOS, an offline India map, on-device emergency guidance, and practical rescue tools.

[Installation and testing](docs/INSTALL_AND_TEST.md) · [Hackathon brief and demo](docs/HACKATHON.md)

## The problem

During a disaster or personal emergency, people may have limited internet, need to share their location quickly, and be unsure what to do next. Emergency contacts, offline maps, first-aid guidance, and signaling tools are often spread across separate apps.

## What Guardian does

Guardian brings practical first steps together for emergencies in India, including disasters, medical incidents, and personal-safety situations.

| Need | Guardian feature |
| --- | --- |
| Alert trusted people | SOS flow has a five-second cancel window, then sends an emergency message and available location to contacts the user saved. |
| Navigate without mobile data | Bundled India map with the phone’s GPS position. |
| Get emergency-focused guidance | AID chat runs Gemma 3 1B IT locally; curated emergency guidance is available offline. |
| Signal or orient yourself | Siren, Morse Audio, Torch SOS, Floodlight, Medical ID, Compass HUD, and Link Emergency Contacts. |
| Relay between nearby users | Nearby relay can connect Guardian phones when the required radios, permissions, and connectivity are available. |

Guardian complements official emergency response. It sends SOS messages to saved contacts; it does not directly dispatch police, fire, or ambulance services. For immediate danger in India, call **112** when possible.

## Example

If someone is stranded during flooding with mobile data unavailable, they can check their position on the offline map, send an SOS and location by SMS to saved contacts when cellular SMS is available, consult offline guidance, and use a torch or Morse signal to attract attention.

## Download the test build

The GitHub pre-release is a **debug-signed test build** for Android phones. It bundles the India map and Gemma model and is about **1.57 GiB**; allow extra space while the app prepares the offline assets. Read [Installation and testing](docs/INSTALL_AND_TEST.md) before sharing it. Keep Play Protect enabled; some phones may block direct sideloading because the app requests SMS permission.

- [Download the current Guardian test APK](https://github.com/polikeybhuvan/Guardian/releases/download/v1.0.2-test/Guardian-Android-Test-1.0.2.apk)
- [View release notes](https://github.com/polikeybhuvan/Guardian/releases/tag/v1.0.2-test)
- SHA-256: `2481800A48AF0EA6E2D0F31A77DB0A61C83557C6332C4ED5FF0915857B8C63D4`

## Build from source

### Requirements

- Android Studio and Android SDK Platform 36 (API 36.1)
- JDK 25 (the Gradle daemon toolchain is configured for Java 25)
- Git and Git LFS
- An Android 7.0 (API 24) or newer device or emulator
- At least several GB of free disk space for the source assets and app setup

The map and Gemma model are stored in Git LFS. A full checkout downloads about 2.2 GB of large assets.

```bash
git lfs install
git clone https://github.com/polikeybhuvan/Guardian.git
cd Guardian
git lfs pull
./gradlew :app:assembleDebug
```

On Windows PowerShell:

```powershell
git lfs install
git clone https://github.com/polikeybhuvan/Guardian.git
Set-Location Guardian
git lfs pull
.\gradlew.bat :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. For Android Studio, open the cloned `Guardian` folder, let Gradle sync finish, select the `app` run configuration, and run it on a connected device. The map is copied into app storage the first time its screen is opened; the offline model is prepared for local use on first launch/use.

See [Installation and testing](docs/INSTALL_AND_TEST.md) for the USB/ADB procedure, offline checks, and troubleshooting. See [Releasing a test build](docs/RELEASING.md) before preparing an APK for GitHub Releases.

## Technical overview

- **UI:** Kotlin and Jetpack Compose
- **Local data:** Room database and app-private storage
- **Offline map:** Mapsforge with OpenStreetMap map data and device GPS
- **On-device AI:** LiteRT-LM with Gemma 3 1B IT; no Gemini API key or cloud AI request is needed for AID chat
- **Nearby relay:** Google Nearby Connections between Guardian phones
- **Safety actions:** Android SMS, location, camera torch, audio, and sensor APIs

## Hackathon track

Guardian is presented for **Best Open-Source AI Project**. This repository bundles **Gemma 3 1B IT** for on-device emergency chat; it is not a Gemma 4 build. See the [project brief and demo flow](docs/HACKATHON.md).

## Privacy and safety

- Emergency contacts, Medical ID, and SOS history are stored in local app storage; Android backup is disabled.
- The user chooses saved contacts and starts the SOS flow. Android reporting that an SMS was submitted does not confirm delivery to the recipient.
- Location, SMS, and device-tool behavior depends on permissions and hardware. SMS requires a working SIM, carrier service, and a compatible device.
- Nearby relay works between Guardian phones only; it cannot alert nearby phones that do not have Guardian installed.
- AID is informational emergency guidance, not a medical diagnosis. Contact emergency services in an emergency.

## Data, model, and source licenses

Guardian’s original source code is licensed under Apache-2.0; see [LICENSE](LICENSE). The bundled India map data is © OpenStreetMap contributors and distributed under ODbL. The Gemma model is distributed under the Gemma Terms of Use and Prohibited Use Policy. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and the notices bundled in `app/src/main/assets/`.

## Project documents

- [Installation and testing guide](docs/INSTALL_AND_TEST.md)
- [Hackathon problem, impact, and demo brief](docs/HACKATHON.md)
- [Test-build release checklist](docs/RELEASING.md)
- [Contribution guidelines](CONTRIBUTING.md)

## Links

- [Builder’s LinkedIn selection announcement](https://lnkd.in/p/dWxtn-v5)
- [India emergency response system: 112](https://112.gov.in/)
- [Gemma 3 1B IT model](https://huggingface.co/litert-community/Gemma3-1B-IT)
