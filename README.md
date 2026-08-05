
<p align="center">
  <img src="https://raw.githubusercontent.com/mkaomwakuni/Weaver-News-App/feature/weaver-rebrand/core/designsystem/src/main/res/drawable/ic_logo.xml" alt="Logo" width="128" height="128">
</p>

<h1 align="center">Weaver News</h1>

<p align="center">
  <b>A global news application built with Jetpack Compose and Clean Architecture.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.2-blue.svg?style=for-the-badge&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Architecture-Clean-green.svg?style=for-the-badge" alt="Architecture">
  <img src="https://img.shields.io/badge/UI-Compose-orange.svg?style=for-the-badge&logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/License-Apache_2.0-red.svg?style=for-the-badge" alt="License">
</p>

---

## 📖 About the Project

**Weaver News** is a modern mobile platform that allows users to discover, read, and manage global news in real time. The app provides a seamless reading experience where users can explore featured stories, view detailed article content, track categories, and bookmark news for offline access.

The platform supports multiple topics such as **Technology**, **Sports**, **Entertainment**, and **Science**, allowing users to easily browse and filter articles based on their interests. Each article listing includes comprehensive details such as high-quality images, publication metadata, HTML-parsed content, and sharing capabilities.

Built with **Jetpack Compose** and **Clean Architecture**, the application leverages real-time data synchronization via the GNews and World News APIs to ensure the latest global updates are reflected instantly across devices.

## 📸 Screenshots

<table>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/c2e9d3e2-bb65-4e0d-bf5a-58edca0fc56f" width="250" alt="Home"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/95b413b8-9370-40fe-8283-e3b46b5295fc" width="250" alt="Categories"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/27657eda-a6f8-40ac-a7f0-08f6bad46680" width="250" alt="Search"></td>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/cbeded64-be2c-4eb1-b7b3-39cfd75e6b84" width="250" alt="Settings"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/58e93298-0097-4951-b9f7-de0e7d012101" width="250" alt="Bookmarks"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/b5344d42-5034-4ee2-bffa-dc73d778a9d5" width="250" alt="Details"></td>
  </tr>
</table>

## ✨ Key Features
- **Global News Hub**: Access news from thousands of magazines and blogs across 60+ countries.
- **Adaptive Material 3 UI**: Beautiful, responsive design that adapts to phones, tablets, and foldables.
- **TikTok-style Video Feed**: Immersive, full-screen video player for breaking news clips.
- **Advanced Bookmarking**: Save articles to a local Room database for offline reading.
- **Real-time Search**: Instant search results powered by GNews and World News providers.
- **Home Screen Widgets**: Quick access to top headlines directly from the Android launcher.
- **Performance Optimized**: Near-zero jank using Baseline Profiles and Macrobenchmarking.
- **Customizable Theming**: Full support for Light and Dark modes.

## 🏗️ Structural Design Pattern
The app follows the **Model-View-ViewModel (MVVM)** pattern, enhanced with principles from **Clean Architecture** to ensure better separation of concerns and maintainability.

- **Models**: Represent data and core business logic (Domain Entities). These are pure Kotlin objects.
- **Views**: Handle the UI layer and display visual elements using **Jetpack Compose**.
- **ViewModels**: Serve as the bridge between views and data, transforming raw state into view-ready formats.

By combining MVVM with clean architecture layers (such as **Use Cases**, **Repositories**, and **Data Sources**), the codebase stays modular, testable, and easy to scale as the app grows.

---

## 🛠️ Tech Stack

### [Kotlin](https://kotlinlang.org/)
Kotlin is a modern, cross-platform, statically typed programming language. It is designed to be fully interoperable with Java, providing more safety, conciseness, and developer productivity for Android development.

### [Jetpack Compose](https://developer.android.com/jetpack/compose)
Android’s modern, declarative UI toolkit. It simplifies and accelerates UI development with less code, powerful tools, and intuitive Kotlin APIs.

### [Hilt (Dagger)](https://dagger.dev/hilt/)
A dependency injection library for Android that reduces the boilerplate of doing manual DI. It provides a standard way to use Dagger in your application.

### [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
The gold standard for networking on Android. Retrofit turns your HTTP API into a Java/Kotlin interface, while OkHttp handles the efficient underlying transport.

### [Room Database](https://developer.android.com/training/data-storage/room)
The Room persistence library provides an abstraction layer over SQLite to allow fluent database access while leveraging the full power of SQLite.

### [Navigation 3](https://developer.android.com/guide/navigation/navigation3)
The next evolution of Android Navigation, providing type-safe routing and improved support for adaptive, multi-pane layouts.

### [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
The library for scheduling deferrable, asynchronous tasks that must be run, such as your periodic background news refreshes.

### [Coil](https://coil-kt.github.io/coil/)
An image loading library for Android backed by Kotlin Coroutines. It is fast, lightweight, and modern.

### [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
A data storage solution that allows you to store key-value pairs or typed objects with protocol buffers. It is the modern replacement for SharedPreferences.

### [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles) & [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)
Tools used to optimize application startup and runtime performance by providing the runtime with hints about code execution paths.

### [GitHub Actions](https://github.com/features/actions)
Automated CI/CD workflows for building, testing, and releasing the application directly from the repository.

### [Fastlane](https://fastlane.tools/)
An open-source platform that simplifies Android deployment, automating builds and Play Store releases.

---

## 🚀 Setup Requirements
- Android device or Emulator (API 24+)
- Android Studio Ladybug | 2024.2.1 or newer
- JDK 17

## 🏁 Getting Started
1. **Clone the project**:
   ```bash
   git clone https://github.com/mkaomwakuni/Weaver-News-App.git
   ```
2. **Import into Android Studio**: Open the cloned folder.
3. **Configure API Keys**: Add your keys to `local.properties`:
   ```properties
   GNEWS_API_KEY="your_api_key"
   WORLDNEWS_API_KEY="your_api_key"
   ```
4. **Build & Run**: Click the **Run** button to deploy to your device.

---

## 🤝 Support
- Found this project useful ❤️? Support by clicking the ⭐️ button on the upper right of this page. ✌️
- Notice anything missing? **File an issue**.
- Feel free to **contribute** in any way—from typos in docs to code reviews, all are welcome.

<p align="center">
  Developed by <b>mkaomwakuni</b> ✌️
</p>
