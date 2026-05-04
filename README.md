# An-Agent-Based-Smart-Tourism-System-for-Vietnam

`BeVietnam` is a smart tourism system for Vietnam which helps travelers discover meaningful cultural places, complete storyline-based exploration tasks, receive personalized recommendations, and generate travel memories through AI-assisted vlog/daily generation. This system supports for Android application and Website. A native iOS app can be added later when my team has access to Mac hardware or a CI/cloud to build workflow for iOS :)

We hope our system will become a helpful companion for both domestic and international travelers who may feel overwhelmed by the vast amount of information about destinations in Vietnam.

To provide the best possible experience for travelers who wish to explore and learn more about the country, our team developed this application with Vietnamese culture at its core. The system is designed to deliver cultural information in the most vivid and engaging ways possible through personalized feeds, daily tasks that encourage travelers to experience things firsthand, and more.

## Tech Stack

This project is built with a multi-platform architecture, including a native Android application, a responsive web application, a backend API, and an AI-powered core system.

| Layer | Technology | Usage |
|---|---|---|
| **Mobile App** | Kotlin | Main programming language for the native Android application. |
|  | Android SDK | Provides core Android features and platform APIs. |
|  | Jetpack Compose | Used to build modern native Android UI. |
|  | Retrofit / OkHttp | Handles API communication between the mobile app and backend services. |
|  | CameraX | Supports photo capture for check-ins and travel tasks. |
|  | Google Maps SDK | Provides map-based place discovery and location features. |
| **Web App / PWA** | React / Next.js | Builds the responsive web application for desktop and iPhone users. |
|  | TypeScript | Adds type safety and improves frontend maintainability. |
|  | Tailwind CSS | Used for fast and consistent UI styling. |
|  | PWA Support | Allows users to access core features through a browser-based app experience. |
| **Backend API** | FastAPI | Main backend framework for building RESTful APIs. |
|  | Python | Core programming language for backend services and AI integration. |
|  | PostgreSQL | Stores user data, places, tasks, captures, and travel history. |
|  | SQLAlchemy | Handles database models and ORM operations. |
|  | Alembic | Manages database migrations. |
|  | JWT Authentication | Secures user login and protected API routes. |
| **AI Core / Agent System** | Python | Main language for AI workflows and agent logic. |
|  | LangChain / LangGraph | Used to build AI agents, retrieval flows, and reasoning pipelines. |
|  | Vector Database | Stores and retrieves cultural knowledge for recommendation and task generation. |
|  | LLM API | Powers cultural explanation, personalized suggestions, and generated travel content. |
| **Infrastructure & DevOps** | Docker | Containerizes backend and AI services for consistent development and deployment. |
|  | GitHub Actions | Handles CI/CD workflows. |
|  | Cloud Storage | Stores user-uploaded photos and generated media assets. |


