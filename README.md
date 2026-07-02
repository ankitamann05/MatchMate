# MatchMate

MatchMate is an Android app that displays match profile cards and lets the user accept or decline each profile. Profiles are loaded from the Random User API, cached locally with Room, and shown in a RecyclerView with pagination.

## Demo

<p align="center">
  <img src="demo/MatchMade_demo.gif" alt="MatchMate demo" width="300" />
</p>

## Functionality

- Displays match cards with profile photo, name, age, location, and email.
- Provides Accept and Decline actions for each profile.
- Saves the selected decision locally in the Room database.
- Keeps accepted or declined state visible after app restart.
- Shows cached profiles when the device is offline.
- Loads more profiles as the user scrolls near the bottom of the list.
- Shows toast messages for accept, decline, offline, and error states.
- Uses a splash screen and custom MatchMate app icon/theme colors.

## Architecture

The project is separated into packages by responsibility:

- `database`: Room database, DAO, and entity classes.
- `repository`: Data layer that coordinates API results and local storage.
- `network`: Retrofit API definitions.
- `model`: UI state, decision constants, and API response models.
- `di`: Hilt dependency providers.
- `ui`: Activity and RecyclerView adapter.
- `viewmodel`: Screen state and business logic.

The app follows a simple MVVM-style structure:

- `MainActivity` renders the UI and observes one `MatchUiState`.
- `MatchViewModel` handles pagination, online/offline checks, and user actions.
- `MatchRepository` loads profiles and saves decisions.
- Room is the local source of truth for displayed profiles.

## Libraries Used

- Kotlin
- AndroidX AppCompat
- AndroidX Core KTX
- AndroidX Activity
- ConstraintLayout
- RecyclerView
- Material Components
- Lifecycle ViewModel and LiveData
- Kotlin Coroutines
- Retrofit
- Gson Converter for Retrofit
- Glide
- Room
- Hilt
- KSP
- AndroidX SplashScreen

## API

The app uses the public Random User API:

```text
https://randomuser.me/api/
```

The request uses a fixed seed so paginated results are stable for the app.

## Offline Behavior

Profiles are cached in Room after they are loaded from the API. When the device is offline, the app continues showing cached profiles. Accept and decline decisions are stored locally.

## Running the App

1. Open the project in Android Studio.
2. Make sure Android Studio uses JDK 11 or newer.
3. Sync Gradle.
4. Run the `app` configuration on an emulator or Android device.

Minimum SDK: 24  
Target SDK: 36  
Compile SDK: 36
