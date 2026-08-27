Architecture starter template (multi-module)
==================

This template is compatible with the latest **stable** version of Android Studio.

![Screenshot](https://github.com/android/architecture-templates/raw/main/screenshots.png)

## Features

* Offline-first data layer (Room as the single source of truth, background sync via WorkManager)
* Room Database
* Hilt
* Domain layer (use cases) + ViewModel, read+write
* UI in Compose, list + write (Material3), with a reusable design system
* Navigation3
* Repository and data source
* Kotlin Coroutines and Flow (unidirectional data flow)
* Unit tests
* UI tests using fake data with Hilt
* Macrobenchmark + Baseline Profile

See [DEVELOPMENT_MANUAL.md](DEVELOPMENT_MANUAL.md) for the full architecture guide.

## Modules
The following module types are used:

- `app` — the application shell (single activity, navigation host, theme).
- `core:*` — shared building blocks used by one or more feature modules (`model`, `database`, `network`, `data`, `domain`, `designsystem`, `ui`, `navigation`, `common`, `testing`, `data-test`).
- `feature:greeting:impl` — the `greeting` feature (UI + ViewModel).
- `feature:greeting:api` — the navigation keys for the `greeting` feature. This lets other modules navigate to `greeting`'s screens without depending on `feature:greeting:impl` (an [api / implementation split](https://developer.android.com/topic/modularization/patterns#dependency_injection)).
- `sync:work` — WorkManager-based sync (`sync:sync-test` provides sync test doubles).
- `benchmarks` — Macrobenchmark + Baseline Profile generation.
- `ui-test-hilt-manifest` — a Hilt host activity for UI tests.

Check the [modularization guidance](https://developer.android.com/topic/modularization) for more information.

## Usage

1. Clone this branch

```
git clone https://github.com/android/architecture-templates.git --branch multimodule
```


2. Run the customizer script:

```
./customizer.sh your.package.name DataItemType [MyApplication]
```

Where `your.package.name` is your app ID (should be lowercase) and `DataItemType` is used for the
name of the screen, exposed state and data base entity (should be PascalCase). You can add an optional application name.

# License

Now in Android is distributed under the terms of the Apache License (Version 2.0). See the
[license](LICENSE) for more information.
