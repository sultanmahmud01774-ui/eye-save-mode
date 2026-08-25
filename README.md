<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/803176f1-f318-4a7f-b369-f1e865c193bb

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

### Note on the debug keystore

The `debug` build variant is signed with `debug.keystore` at the project root
(matching what CI generates). This keystore is git-ignored by convention, so a
fresh checkout may not have one. If Android Studio reports
`Keystore file '.../debug.keystore' not found for signing config 'debugConfig'`,
generate one yourself with:

```bash
keytool -genkey -v -keystore debug.keystore -alias androiddebugkey \
  -keyalg RSA -keysize 2048 -validity 10000 -storepass android \
  -keypass android -dname "CN=Android Debug,O=Android,C=US"
```

The `gradlew` / `gradlew.bat` wrapper scripts and `gradle-wrapper.jar` are
included in this export so `./gradlew assembleDebug` works out of the box.
