<img width="9424" height="5548" alt="Echo1" src="https://github.com/user-attachments/assets/02f7745f-546d-48f1-be44-2fb77259de0f" />

com.ai.Echo/
├── data/                         # DATA LAYER (Implementation)
│   ├── remote/                   # API / Firebase Services
│   │   ├── ApiClient.kt          # OkHttp setup for GPT
│   │   └── FirebaseService.kt    # Firebase Auth/Database calls
│   ├── repository/               # Repository Implementations
│   │   ├── ChatRepositoryImpl.kt
│   │   └── AuthRepositoryImpl.kt
│   └── model/                    # DTOs (Data Transfer Objects)
│       └── ChatResponseDto.kt    # JSON parsing classes
│
├── domain/                       # DOMAIN LAYER (Business Logic - Pure Kotlin)
│   ├── model/                    # UI-Friendly Data Models
│   │   └── Message.kt
│   ├── repository/               # Interfaces (Abstractions)
│   │   ├── ChatRepository.kt
│   │   └── AuthRepository.kt
│   └── usecase/                  # Single Responsibility Actions
│       ├── GetAiResponseUseCase.kt
│       └── SendEmailUseCase.kt   # Logic for JavaMail
│
├── ui/                           # UI LAYER (Presentation)
│   ├── theme/                    # Color.kt, Type.kt, Theme.kt
│   ├── components/               # Reusable UI (Custom Buttons, Loaders)
│   │   └── EchoLoadingState.kt   # Uses your ContainedLoadingIndicator
│   ├── navigation/               # Compose Navigation
│   │   └── NavGraph.kt           # Screen routes
│   └── screens/                  # Feature-based screens
│       ├── chat/
│       │   ├── ChatScreen.kt
│       │   └── ChatViewModel.kt
│       └── login/
│           ├── LoginScreen.kt
│           └── LoginViewModel.kt
│
├── util/                         # Helpers & Extensions
│   ├── Constants.kt              # API Keys, Firebase paths
│   └── Extensions.kt             # View/Context helpers
│
└── EchoApp.kt                    # Application Class
