# UL Manager

Aplikacja mobilna do zarządzania pasieką. Aplikacja korzysta z backendu C# hostowanego na Azure, uwierzytelnienia Microsoft Entra ID (MSAL) i lokalnego cache Room jako offline-first. Warstwa prezentacji (Jetpack Compose) komunikuje się wyłącznie z repozytoriami domenowymi — zmiana źródła danych nie wymaga modyfikacji ViewModeli ani ekranów.

---

## Stos technologiczny

| Obszar | Technologia |
|--------|-------------|
| Język | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Architektura | MVVM + Clean Architecture |
| DI | Hilt 2.54 |
| Baza danych | Room 2.6 (schemat v10, migracje 4→10) |
| Nawigacja | Navigation Compose 2.8 (type-safe routes) |
| Asynchroniczność | Kotlin Coroutines + Flow |
| Sieć | Retrofit 2.11 + OkHttp 4.12 + Kotlinx Serialization |
| Uwierzytelnianie | MSAL 5.x (Microsoft Entra ID) |
| Mapy | OSMDroid 6.1 (OpenStreetMap) |
| Wykresy | Vico 2.0 (Compose M3) |
| Zdjęcia | CameraX 1.3 + Coil 2.7 |
| Generowanie QR | ZXing Core 3.5 |
| Bezpieczeństwo | AndroidX Biometric + EncryptedSharedPreferences |
| Build | Gradle 8.9 + KSP + Version Catalog |
| Min SDK | 26 (Android 8.0) |

---

## Architektura

Projekt dzieli się na trzy warstwy Clean Architecture:

```
core/               — uwierzytelnianie (MSAL), sieć (AuthInterceptor, TokenProvider),
                      bezpieczeństwo (PIN, biometria), narzędzia (Resource.kt)
data/
  ├── remote/        — interfejsy Retrofit (api/) + ApiSource z obsługą ETag (source/)
  ├── dto/           — obiekty transferu danych (DTO) z @Serializable
  ├── local/         — Room entities, DAO, baza danych z migracjami
  ├── mapper/        — DTO ↔ Entity ↔ Domain mappery
  └── repository/    — implementacje repozytoriów (offline-first: API → Room → Flow)
domain/
  ├── model/         — modele domenowe (Apiary, Hive, Inspection, Task, …)
  ├── repository/    — interfejsy repozytoriów
  └── usecase/       — SaveInspectionUseCase
presentation/        — ekrany Compose, ViewModels, UiState, nawigacja
di/                  — moduły Hilt: Database, Network, Repository, Coroutines
```

Przepływ danych (offline-first):

```
[Write]  Screen → ViewModel → Repository → ApiSource → API
                                         ↘ Room (cache)

[Read]   API → ApiSource → Room → Flow → ViewModel → Screen
         (refresh w tle)    ↑
                     lokalne dane natychmiast
```

Repozytoria przy starcie odświeżają dane z API (`refresh()`), a ViewModele obserwują Flow z Room — dzięki temu UI reaguje natychmiast na zmiany lokalne i zdalne.

---

## Uwierzytelnianie i bezpieczeństwo

### Microsoft Entra ID (MSAL)

- **Logowanie** — `LoginViewModel` wywołuje `MsalAuthManager.signIn()` (interactive sign-in przez przeglądarkę systemową)
- **Rejestracja** — `RegisterViewModel` wykonuje ten sam flow MSAL, po czym `POST /api/auth/register` tworzy profil w backendzie (idempotentne — 201/200)
- **Resetowanie hasła** — `ForgotPasswordViewModel` wywołuje `POST /api/auth/forgot-password`, otwiera link SSPR w Custom Tab
- **Token** — `AuthInterceptor` automatycznie dołącza `Authorization: Bearer` do każdego żądania; przy 401 odświeża token i ponawia request

### Lokalny PIN i biometria

Po pomyślnym zalogowaniu MSAL użytkownik przechodzi przez flow onboardingu:
1. **Onboarding Carousel** — prezentacja funkcji aplikacji (4 slajdy)
2. **Ustawienie PIN** — 4-cyfrowy PIN zabezpieczający lokalne odblokowanie
3. Przy kolejnych uruchomieniach — **PIN Unlock** lub **biometria** (odcisk palca)

PIN i preferencje przechowywane w `EncryptedSharedPreferences`. Biometria oparta na `BiometricHelper` (AndroidX Biometric).

---

## Funkcjonalności

### Nawigacja

- Boczna szuflada (`ModalNavigationDrawer`) z pozycjami: Pulpit, Pasieki, Zadania, Mapa pasiek, Statystyki, Ustawienia
- Dodatkowe pozycje w drawers: Sterowanie głosowe (planowane), Analiza AI
- Aktywna pozycja menu wyróżniona dynamicznie na podstawie bieżącej trasy
- Przycisk hamburgera w TopAppBar na ekranach listy

### Dashboard

- Lista pasiek z liczbą aktywnych uli przy każdej; kliknięcie przechodzi do listy uli pasieki
- Lista zadań na dziś i zaległych, sortowana po dacie; checkbox natychmiast zapisuje zmianę statusu
- **Szybkie akcje:**
  - *Nowy przegląd* i *Miodobranie* — otwierają dwuetapowy `ModalBottomSheet`: wybór pasieki → wybór ula (lub skan QR), po czym przechodzą do odpowiedniego formularza
  - *Dodaj zadanie* — przechodzi bezpośrednio do formularza zadania
  - *Analiza AI* — picker pasieki/ula, potem ekran analizy zdjęć
  - *Mapa* — przechodzi do widoku mapy ze wszystkimi ulami
  - *Sterowanie głosowe* — zaplanowane

### Pasieki (`ApiaryListScreen`)

- Lista pasiek z lokalizacją i liczbą aktywnych uli
- Dodawanie, edycja i usuwanie pasiek z dialogiem potwierdzenia usunięcia
- Kliknięcie pasieki otwiera listę jej uli

### Ule

- **Lista uli** (`HiveListScreen`) — karty z nazwą, numerem i statusem; przejście do szczegółów ula lub formularza dodawania
- **Formularz ula** (`HiveFormScreen`) — pola: nazwa, numer, rok matki (dropdown 10-letni zakres), typ ramy, liczba nadstawek, pochodzenie matki, notatki; przy tworzeniu nowego ula automatycznie generowany jest unikalny UUID przypisany jako kod QR
- **Szczegóły ula** (`HiveDetailScreen`) — widok zakładkowy z sekcjami: Szczegóły, Przeglądy, Miodobrania, Leczenia, Dokarmiania, Zadania; FAB dodaje nowy rekord w aktywnej zakładce
- **Kod QR ula** (`HiveQrScreen`) — wyświetla wygenerowany kod QR; opcja wysłania kodu pocztą e-mail jako załącznik PNG; regeneracja kodu (z dialogiem potwierdzenia — stary kod przestaje obowiązywać)
- **Lokalizacja ula** (`HiveLocationScreen`) — mapa OpenStreetMap z GPS; kliknięcie na mapie ustawia marker lokalizacji ula; przycisk „Moja lokalizacja" z obsługą uprawnień

### Przegląd ula (`InspectionFormScreen`)

- Tryby tworzenia i edycji
- DatePickerDialog (Material 3) z domyślną datą dzisiejszą
- Trzy checkboxy: matka widoczna, czerw widoczny, mateczniki widoczne
- Slider siły rodziny (5 stopni: Krytyczna → Bardzo Silna)
- Sekcja zarządzania ramkami (switch + AnimatedVisibility): nadstawki dodane/usunięte, ramki z suszem, węzy
- Dodawanie zdjęć z aparatu (CameraX) lub galerii; podgląd miniatur z możliwością usunięcia
- Opcja wymiana matki z wyborem roku urodzenia
- Pola tekstowe na problemy i notatki

### Miodobranie, Leczenia, Dokarmiania

Osobne formularze (`HarvestFormScreen`, `TreatmentFormScreen`, `FeedingFormScreen`) powiązane z ulem; dane zapisywane przez API i cache'owane w Room.

### Zadania (`TaskListScreen`)

- **Widok listy** — zadania pogrupowane w sekcje: Zaległe, Dzisiaj, Nadchodzące, Ukończone
- **Widok kalendarza** — niestandardowy kalendarz miesięczny z nawigacją, polskimi nagłówkami dni (pn–nd), kolorowymi kropkami pod datami z zaplanowanymi zadaniami, wyróżnieniem wybranego dnia
- Filtr: Wszystkie / Aktywne / Ukończone
- Checkbox przy każdym zadaniu natychmiast zapisuje zmianę statusu

### Formularz zadania (`TaskFormScreen`)

Tworzenie i edycja zadań z opcjonalnym powiązaniem z pasieką lub ulem, datą wykonania i priorytetem.

### Mapa pasiek (`HivesMapScreen`)

- Mapa OpenStreetMap ze wszystkimi ulami posiadającymi zapisaną lokalizację
- Widok ogólny wszystkich pasiek dostępny z nawigacji i szybkich akcji

### Statystyki (`StatisticsScreen`)

- Filtrowanie po pasiece i roku
- Karty podsumowujące: łączny miód (kg), łączne dokarmianie (kg), liczba uli
- Wykres słupkowy miesięcznych miodobrań (Vico)
- Wykres słupkowy miesięcznych dokarmień
- Rozkład typów miodu (pasek postępu z etykietami)
- Polskie nazwy miesięcy na osiach wykresów

### Analiza AI (`AiAnalysisScreen`)

- Wybór zdjęcia ula do analizy
- Upload oczekujących zdjęć do API
- Typy analizy: wyszukiwanie matki (FindQueen) / detekcja warrozy (DetectVarroa)
- Pole notatki opisującej kontekst
- Prezentacja wyników z backendu (AI Vision)

### Ustawienia (`SettingsScreen`)

- **Bezpieczeństwo:** przełącznik biometrii (odcisk palca), zmiana PIN (weryfikacja starego → nowy → potwierdzenie)
- **Wygląd:** przełącznik ciemnego motywu
- **Konto:** wylogowanie (MSAL sign-out + reset onboardingu)
- Wyświetlanie wersji aplikacji

### Skaner QR

Wbudowany w picker uli na dashboardzie — skanowanie kodu QR natychmiast identyfikuje ul i przechodzi do wybranej akcji (przegląd/miodobranie). Oparty na CameraX + ZXing.

---

## Warstwa sieciowa

### Interfejsy Retrofit (`data/remote/api/`)

| Interface | Endpointy |
|-----------|-----------|
| `AuthApi` | register, login, forgot-password, current user |
| `ApiaryApi` | CRUD pasiek, lista uli, statystyki |
| `HiveApi` | CRUD uli, lookup po QR, regeneracja QR |
| `InspectionApi` | CRUD przeglądów |
| `TaskApi` | CRUD zadań, toggle ukończenia |
| `HivePhotoApi` | upload multipart, usuwanie, analiza AI |
| `FeedingApi` | CRUD dokarmień |
| `TreatmentApi` | CRUD leczenia |
| `HoneyHarvestApi` | CRUD miodobrań |
| `DashboardApi` | agregat dashboard |
| `StatsApi` | statystyki roczne/miesięczne |

### ApiSource (`data/remote/source/`)

Cienki wrapper na każdy interfejs Retrofit z obsługą:
- **ETag** — cache w `ConcurrentHashMap`, automatyczny `If-Match` przy PUT/DELETE
- **Mapowanie błędów** — `HttpException`/`IOException` → `Result<T>` z `ApiException`
- **Paginacja** — `PagedResponse<T>` dla list

### AuthInterceptor (`core/network/`)

Automatycznie dołącza `Authorization: Bearer <token>` do żądań. Przy 401 — jednorazowe odświeżenie tokena i retry.

---

## Struktura pakietów

```
com.example.apiarymanager/
├── core/
│   ├── auth/                  MsalAuthManager
│   ├── network/               AuthInterceptor, TokenProvider, MsalTokenProvider, ApiException
│   ├── security/              PinManager, BiometricHelper
│   └── util/                  Resource.kt
├── data/
│   ├── dto/                   ApiaryDto, HiveDto, InspectionDto, TaskDto, AuthDto, DashboardDto, …
│   ├── local/
│   │   ├── dao/               ApiaryDao, HiveDao, InspectionDao, TaskDao, …
│   │   ├── database/          ApiaryManagerDatabase (Room, v10, migracje 4→10)
│   │   └── entity/            *Entity.kt — Room @Entity z @ForeignKey
│   ├── mapper/                *Mapper.kt — DTO ↔ Entity ↔ Domain
│   ├── remote/
│   │   ├── api/               Retrofit interfaces (11 interfejsów)
│   │   └── source/            *Source.kt — ETag cache + error handling
│   └── repository/            *RepositoryImpl.kt (offline-first: API → Room → Flow)
├── di/
│   ├── CoroutinesModule.kt    @ApplicationScope CoroutineScope
│   ├── DatabaseModule.kt      Room + migracje (bez fallbackToDestructiveMigration)
│   ├── NetworkModule.kt       OkHttp, Retrofit, Kotlinx Serialization, API interfaces
│   └── RepositoryModule.kt    @Binds interface → impl
├── domain/
│   ├── model/                 Apiary, Hive, Inspection, Task, HivePhoto, AiAnalysis, …
│   ├── repository/            interfejsy repozytoriów (9 interfejsów)
│   └── usecase/               SaveInspectionUseCase
└── presentation/
    ├── navigation/            Screen.kt (type-safe routes), AppNavGraph.kt, AppDrawer.kt
    ├── theme/                 Color.kt, Type.kt, Theme.kt
    ├── components/            PinKeypadComponents, QueenYearDropdown
    ├── onboarding/            OnboardingCarouselScreen, PinScreen, PinViewModel
    ├── login/                 LoginScreen, LoginViewModel
    ├── register/              RegisterScreen, RegisterViewModel
    ├── auth/forgotpassword/   ForgotPasswordScreen, ForgotPasswordViewModel
    ├── pin/                   PinUnlockScreen, ChangePinScreen + ViewModels
    ├── dashboard/             DashboardScreen, DashboardViewModel, HivePickerBottomSheet, QrScannerView
    ├── apiary/                ApiaryListScreen, ApiaryFormScreen + ViewModels
    ├── hive/
    │   ├── list/              HiveListScreen, HiveListViewModel
    │   ├── form/              HiveFormScreen, HiveFormViewModel
    │   ├── detail/            HiveDetailScreen, HiveDetailViewModel
    │   ├── qr/                HiveQrScreen, HiveQrViewModel
    │   └── location/          HiveLocationScreen, HiveLocationViewModel
    ├── camera/                CameraScreen (CameraX)
    ├── inspection/            InspectionFormScreen, InspectionFormViewModel
    ├── harvest/               HarvestFormScreen, HarvestFormViewModel
    ├── treatment/             TreatmentFormScreen, TreatmentFormViewModel
    ├── feeding/               FeedingFormScreen, FeedingFormViewModel
    ├── task/                  TaskListScreen, TaskFormScreen + ViewModels
    ├── map/                   HivesMapScreen, HivesMapViewModel
    ├── aianalysis/            AiAnalysisScreen, AiAnalysisViewModel
    ├── statistics/            StatisticsScreen, StatisticsViewModel
    └── settings/              SettingsScreen, SettingsViewModel
```

---

## Uruchomienie

Wymagania: Android Studio Ladybug (2024.2) lub nowszy, JDK 17.

```bash
# 1. Sklonuj repozytorium
git clone <url>

# 2. Skonfiguruj local.properties
#    apiBaseUrl=https://am-dev-api.azurewebsites.net/api

# 3. Otwórz projekt w Android Studio
# File → Open → folder ul-manager

# 4. Poczekaj na sync Gradle (pierwsze uruchomienie pobiera ~500 MB)

# 5. Uruchom na emulatorze API 26+ lub fizycznym urządzeniu
# Run → app
```

Konfiguracja MSAL znajduje się w `app/src/main/res/raw/msal_config.json` (client_id, redirect_uri, authority).

---

## Stan implementacji

| Ekran / funkcja | Status |
|----------------|--------|
| Logowanie MSAL (Microsoft Entra ID) | ✅ gotowe |
| Rejestracja MSAL + backend | ✅ gotowe |
| Resetowanie hasła (SSPR + Custom Tab) | ✅ gotowe |
| Onboarding carousel | ✅ gotowe |
| PIN setup + unlock + zmiana | ✅ gotowe |
| Biometria (odcisk palca) | ✅ gotowe |
| Dashboard — pasieki, zadania, szybkie akcje | ✅ gotowe |
| Lista pasiek z CRUD | ✅ gotowe |
| Formularz pasieki | ✅ gotowe |
| Lista uli | ✅ gotowe |
| Formularz ula z auto-generacją QR | ✅ gotowe |
| Szczegóły ula (6 zakładek) | ✅ gotowe |
| Kod QR ula — podgląd, email, regeneracja | ✅ gotowe |
| Lokalizacja ula na mapie (GPS + OpenStreetMap) | ✅ gotowe |
| Formularz przeglądu (tworzenie i edycja) | ✅ gotowe |
| Formularz miodobrania | ✅ gotowe |
| Formularz leczenia | ✅ gotowe |
| Formularz dokarmiania | ✅ gotowe |
| Lista zadań (lista + kalendarz) | ✅ gotowe |
| Formularz zadania | ✅ gotowe |
| Zdjęcia (aparat CameraX + galeria) | ✅ gotowe |
| Mapa pasiek (OpenStreetMap) | ✅ gotowe |
| Statystyki (wykresy Vico, filtry) | ✅ gotowe |
| Analiza AI zdjęć (FindQueen / DetectVarroa) | ✅ gotowe |
| Ustawienia (biometria, PIN, motyw, wylogowanie) | ✅ gotowe |
| Nawigacja boczna (drawer) | ✅ gotowe |
| Skaner QR (CameraX + ZXing) | ✅ gotowe |
| Połączenie z API (C# backend) | ✅ gotowe |
| Ciemny motyw | ✅ gotowe |
| Sterowanie głosowe | ⏳ planowane |

---

## Backend (C# / Azure)

Aplikacja komunikuje się z REST API hostowanym na Azure App Service:

- **Base URL (dev):** `https://am-dev-api.azurewebsites.net/api`
- **Dokumentacja API:** OpenAPI + Scalar UI
- **Uwierzytelnianie:** Microsoft Entra ID (Azure AD), scope `api://…/User.ReadWrite`
- **ETag:** wymagany nagłówek `If-Match` na każdym `PUT`/`DELETE` (brak → 400)
- **Zdjęcia:** upload multipart, odpowiedź z SAS URL (2h TTL) — nie persistować
- **AI:** endpoint `/analyze` z typem `FindQueen` / `DetectVarroa`

Pełna specyfikacja endpointów w `07-kotlin-integration-guide.md`.

---

## Uwagi techniczne

**Migracje Room** — baza danych jest na schemacie v10 z pełnymi migracjami od v4. `fallbackToDestructiveMigration()` został usunięty — dane lokalne (cache serwerowy) są trwałe.

**Token MSAL** — cache'owany przez bibliotekę MSAL. `AuthInterceptor` pobiera token cicho (`acquireTokenSilent`); przy wygaśnięciu automatyczny retry z `forceRefresh`.

**Offline-first** — repozytoria zawsze emitują dane z Room (Flow). Przy starcie i zapisie synchronizują z API. Brak sieci → ostatni snapshot z Room.

**Kod QR** — każdy ul ma unikalny UUID jako treść kodu QR, generowany przy tworzeniu ula. Kod można zregenerować (nowy UUID, stary przestaje obowiązywać). Wysyłka e-mail korzysta z `Intent.ACTION_SEND`.

**DTO** — klasy w `data/dto/` z `@Serializable` (Kotlinx Serialization). Enumy serializowane jako uppercase string (`ACTIVE`, `WEAK`). Daty jako `String` ISO-8601.

**Mapy** — OSMDroid (OpenStreetMap), nie wymaga klucza API. Lokalizacja GPS z runtime permission handling.

**Package name** — `com.example.apiarymanager` jest placeholderem. Przed publikacją zmienić na docelową domenę odwróconą.
