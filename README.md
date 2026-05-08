# ChatLink - Social Messaging App

Kotlin + Jetpack Compose social messaging application.

## Tech Stack
- Kotlin 2.0.21
- Jetpack Compose BOM 2024.06
- Material 3
- Navigation Compose 2.8.3
- MVVM + Clean Architecture
- Hilt 2.52
- Coroutines + Flow / StateFlow

## Project Structure
- app/ - MainActivity, ChatLinkApp
- core/common/ - UiState, DateTimeUtils
- core/model/ - User, Conversation, Message, Contact
- core/navigation/ - Routes, AppNavHost
- core/ui/theme/ - Material3 Theme
- core/ui/components/ - Avatar, LoadingView, ErrorView, EmptyState
- domain/repository/ - Repository interfaces
- data/repository/ - FakeRepository
- data/di/ - Hilt Module
- feature/main/ - 4-tab bottom nav
- feature/chats/ - Conversation list
- feature/chatdetail/ - Chat detail
- feature/contacts/ - Contact list
- feature/discover/ - Discover page
- feature/profile/ - My profile page

## How to Run
1. Open D:\ChatApp in Android Studio
2. Wait for Gradle Sync
3. Connect phone via USB
4. Enable USB Debugging
5. Click Run
