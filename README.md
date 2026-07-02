# MatchMate 

MatchMate is an Android app that simulates a matrimonial app by displaying match profile cards. Users can accept or decline matches, with all decisions persisted locally for offline access.

## Demo

<p align="center">
  <img src="demo/MatchMade_demo.gif" alt="MatchMate demo" width="300" />
</p>

## Features

- **API Integration:** Fetches user data from the Random User API
- **Match Cards:** Clean matrimonial card design with profile images
- **Accept/Decline:** Users can accept or decline matches with one tap
- **Local Storage:** Profiles and decisions are persisted using Room
- **Offline Mode:** App works offline with cached match profiles
- **Pagination:** Loads more profiles as the user scrolls
- **MVVM Architecture:** Clean separation of UI, state, data, and network layers
- **Dependency Injection:** Uses Hilt for app-level dependencies

## Project Structure

```text
src/main/
├── java/com/example/matchmate/
│   ├── MatchMateApplication.kt
│   │
│   ├── database/
│   │   ├── MatchMateDatabase.kt
│   │   ├── MatchProfileDao.kt
│   │   └── MatchProfileEntity.kt
│   │
│   ├── di/
│   │   └── NetworkModule.kt
│   │
│   ├── model/
│   │   ├── DecisionStatus.kt
│   │   ├── MatchUiState.kt
│   │   └── RandomUserModels.kt
│   │
│   ├── network/
│   │   └── RandomUserApi.kt
│   │
│   ├── repository/
│   │   └── MatchRepository.kt
│   │
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   └── MatchProfileAdapter.kt
│   │
│   └── viewmodel/
│       └── MatchViewModel.kt
│
├── res/
│   ├── drawable/
│   ├── layout/
│   ├── mipmap-*/
│   └── values/
│
└── AndroidManifest.xml
```

## Dependencies

- **Kotlin:** Primary development language
- **AndroidX AppCompat/Core KTX:** Android compatibility and Kotlin extensions
- **ConstraintLayout:** XML layout composition
- **RecyclerView:** Match card list rendering
- **Material Components:** Material-styled UI elements
- **Lifecycle ViewModel and LiveData:** Reactive UI state management
- **Kotlin Coroutines:** Asynchronous API/database work
- **Retrofit:** API communication
- **Gson Converter:** JSON parsing for Retrofit responses
- **Glide:** Async profile image loading
- **Room:** Local database persistence
- **Hilt:** Dependency injection
- **KSP:** Annotation processing for Room and Hilt
- **AndroidX SplashScreen:** Launch screen support

## Architecture

The app follows MVVM architecture:

- **Models:** API response models, UI state, and decision constants
- **ViewModel:** Business logic, pagination, offline handling, and user actions
- **Views:** Activity, XML layouts, and RecyclerView adapter for UI rendering
- **Repository:** Coordinates network results and local database updates
- **Database:** Room entity, DAO, and database setup
- **Network:** Retrofit API definition and dependency setup

Room acts as the local source of truth. The UI observes cached profiles through `LiveData`, so saved accept/decline decisions remain visible after app restart.

## Installation

1. Clone the repository.
2. Open the project in Android Studio.
3. Make sure Android Studio uses JDK 11 or newer.
4. Sync Gradle.
5. Build and run the `app` configuration on an emulator or Android device.

## API

The app fetches user data from:

```text
Endpoint: https://randomuser.me/api/
```

The request uses a fixed seed so paginated results remain stable across app launches.

## Data Flow

```text
App launches -> Observe cached Room data -> Check network connectivity
If online -> Fetch users from API -> Save to Room -> Display cards
If offline -> Load profiles from Room cache -> Display cards
User accepts/declines -> Update Room -> Update UI state
User scrolls near bottom -> Fetch next page -> Cache and display more profiles
```

## Error Handling

The app displays user-friendly toast messages for accept, decline, offline, and error states. Repository operations return `Result` values so failures can be handled by the ViewModel before updating the UI.

## License

This project is for demonstration purposes.
