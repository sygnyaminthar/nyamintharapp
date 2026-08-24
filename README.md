# Nyaminthar Android App

Android WebView app for `https://nyaminthar.com/?app=1`.

## Features

- WebView website
- JavaScript + DOM storage
- Android back button navigation
- No WebView popup windows
- Blocks a list of common ad/tracker domains
- Hides common ad/banner/popunder elements with injected CSS/JS
- External normal HTTP(S) links open in the phone browser
- GitHub Actions builds the APK automatically

## GitHub setup

1. Upload this entire project to `sygnyaminthar/nyamintharapp`.
2. Push to the `main` branch.
3. Open **Actions**.
4. Select **Build Android APK**.
5. Wait for the workflow to finish.
6. Open the completed workflow run and download the `nyaminthar-debug-apk` artifact.

## Important

This app uses WebView-side filtering. It cannot guarantee that every future ad network or ad script will be blocked.

For the most reliable result, make the website itself detect `?app=1` and skip ad scripts when that parameter is present. That keeps the normal website monetized while the app version is ad-free.
