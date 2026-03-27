# Resident Android App

This folder contains the native Android Studio project for the resident side of the Barangay system.

## What is included
- Login
- Registration
- Resident dashboard
- Embedded document request form
- Embedded complaint form
- Announcements screen
- Profile update
- Change password
- Logout

## Backend connection
The app talks to the existing Node/Express backend and SQLite database.

Current default emulator base URL:

`http://10.0.2.2:3000/`

That works only for the Android emulator.

For a real phone, change this line in:
`/Users/user/Downloads/barangay management system/android-app/app/build.gradle.kts`

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/\"")
```

Example for a phone on the same Wi-Fi:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.51:3000/\"")
```

Then Sync Gradle and rebuild the app.

## Open in Android Studio
1. Open `/Users/user/Downloads/barangay management system/android-app` in Android Studio.
2. Let Gradle sync.
3. Start the backend from `/Users/user/Downloads/barangay management system` with `npm start`.
4. If testing on emulator, keep `10.0.2.2`.
5. If testing on a real phone, replace `10.0.2.2` with your computer LAN IP and rebuild.

## Notes
- This app is resident-only. Admin accounts are blocked from signing into the Android app.
- The Android app uses the same backend session-cookie auth as the web system.
