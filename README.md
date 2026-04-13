# ApiaryManager

Aplikacja mobilna do zarządzania pasieką. Aktualny stan repozytorium to **funkcjonalna makieta (Functional Mockup)** — cały interfejs działa w oparciu o lokalne dane mockowane zapisywane w bazie Room. Docelowy backend w C# zostanie podłączony bez przebudowy warstwy prezentacji.

---

## Stos technologiczny

| Obszar | Technologia |
|--------|-------------|
| Język | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Architektura | MVVM + Clean Architecture |
| DI | Hilt 2.54 |
| Baza danych | Room 2.6 |
| Nawigacja | Navigation Compose 2.8 (type-safe routes) |
| Asynchroniczność | Kotlin Coroutines + Flow |
| Build | Gradle 8.9 + KSP + Version Catalog |
| Min SDK | 26 (Android 8.0) |

---

## Architektura

Projekt dzieli się na trzy warstwy Clean Architecture:

```
presentation/   — ekrany Compose, ViewModels, UiState, nawigacja
domain/         — modele domenowe, interfejsy repozytoriów, use case'y
data/           — Room entities, DAO, DTO, mappery, implementacje repozytoriów
```

Przepływ danych jest jednostronny:

```
Room ──(Entity)──► Repository ──(Domain Model)──► ViewModel ──(UiState)──► Screen
Screen ──(Event)──► ViewModel ──(suspend)──► UseCase ──► Repository ──► Room
```

Docelowo warstwa `data/` rozszerzy się o klienta API (Retrofit). Dzięki wzorcowi DTO warstwa domenowa i prezentacyjna nie wymagają żadnych zmian — wystarczy podłączyć nowe źródło danych do istniejących repozytoriów.

---

## Co robi makieta

### Ekran logowania i rejestracji

- Walidacja pól formularza (format e-mail, minimalna długość hasła, zgodność haseł)
- Przycisk "Zaloguj się" / "Zarejestruj się" symuluje żądanie sieciowe: przez 1 sekundę wyświetla spinner, następnie przekierowuje na Dashboard
- Dane wpisane w formularz nie są nigdzie zapisywane — to celowe, uwierzytelnianie przyjdzie z backendem
- "Zapomniałem hasła" wyświetla informację o niedostępności funkcji w trybie makiety

### Dashboard (widok główny)

- Pobiera i wyświetla wszystkie pasieki z lokalnej bazy Room wraz z liczbą aktywnych uli przy każdej
- Lista zadań do wykonania: filtruje zadania przeterminowane lub na dziś, sortuje po dacie i priorytecie
- Checkbox przy zadaniu natychmiast zapisuje zmianę statusu (`setTaskCompleted`) do bazy Room i odświeża listę reaktywnie przez Flow
- Cztery przyciski szybkich akcji (Nowy przegląd, Miodobranie, Dodaj zadanie, Mapa pasiek) wyświetlają komunikat "wkrótce dostępne"
- Kliknięcie karty pasieki przechodzi do listy uli tej pasieki

### Formularz przeglądu ula

- Obsługuje dwa tryby: tworzenie nowego przeglądu (`inspectionId == null`) oraz edycję istniejącego (ładuje dane z Room przez `InspectionRepository`)
- **Data przeglądu** — DatePickerDialog z Material 3, domyślnie dzisiejsza data
- **Stan rodziny** — trzy niezależne checkboxy: matka widoczna, czerw widoczny, mateczniki widoczne
- **Siła rodziny** — Slider z 5 pozycjami: Krytyczna / Słaba / Normalna / Silna / Bardzo Silna; aktywna pozycja wyróżniona kolorem i pogrubieniem
- **Zmiany w ulu** — Switch włącza/wyłącza sekcję liczników z animowanym rozwinięciem (AnimatedVisibility); liczniki z przyciskami [−] i [+] dla: nadstawki dodane, nadstawki usunięte, ramki z suszem, węzy; wartości nie schodzą poniżej zera
- **Pola tekstowe** — osobne pola na zaobserwowane problemy i dodatkowe notatki
- Przycisk "Zapisz przegląd" wywołuje `SaveInspectionUseCase`, który decyduje między `insert` a `update` na podstawie id, a po sukcesie wraca do poprzedniego ekranu

### Dane startowe (seed)

Przy pierwszej instalacji baza jest wypełniana danymi mockowanymi:

- 2 pasieki: Pasieka Leśna (Bory Tucholskie) i Pasieka Ogrodowa (Gdańsk-Oliwa)
- 8 uli o zróżnicowanych statusach (ACTIVE, WEAK, DEAD)
- 12 przeglądów z realistycznymi obserwacjami i różnymi siłami rodzin
- 7 zadań z różnymi priorytetami i terminami (część przeterminowana — celowo, żeby Dashboard pokazywał niepustą listę)

Dane przepływają przez pełen pipeline DTO → Entity → Domain, symulując przyszłe pobieranie z API.

---

## Struktura pakietów

```
com.example.apiarymanager/
├── core/util/                  Resource.kt (sealed class Loading/Success/Error)
├── data/
│   ├── dto/                    ApiaryDto, HiveDto, InspectionDto, TaskDto
│   ├── local/
│   │   ├── dao/                ApiaryDao, HiveDao, InspectionDao, TaskDao
│   │   ├── database/           ApiaryManagerDatabase (Room, v2)
│   │   └── seeder/             DatabaseSeeder (dane mockowane)
│   ├── mapper/                 *Mapper.kt — DTO↔Entity↔Domain
│   ├── entity/                 *Entity.kt — Room @Entity z @ForeignKey
│   └── repository/             *RepositoryImpl.kt
├── di/
│   ├── CoroutinesModule.kt     @ApplicationScope CoroutineScope
│   ├── DatabaseModule.kt       Room + fallbackToDestructiveMigration (DEV)
│   └── RepositoryModule.kt     @Binds interface → impl
├── domain/
│   ├── model/                  Apiary, Hive, Inspection, Task + enumy
│   ├── repository/             interfejsy repozytoriów
│   └── usecase/                SaveInspectionUseCase
└── presentation/
    ├── navigation/             Screen.kt (type-safe routes), AppNavGraph.kt
    ├── theme/                  Color.kt, Type.kt, Theme.kt
    ├── login/                  LoginScreen, LoginViewModel
    ├── register/               RegisterScreen, RegisterViewModel
    ├── dashboard/              DashboardScreen, DashboardViewModel
    ├── hive/
    │   ├── list/               HiveListScreen (placeholder)
    │   └── detail/             HiveDetailScreen (placeholder)
    └── inspection/             InspectionFormScreen, InspectionFormViewModel
```

---

## Uruchomienie

Wymagania: Android Studio Ladybug (2024.2) lub nowszy, JDK 17.

```bash
# 1. Sklonuj repozytorium
git clone <url>

# 2. Otwórz projekt w Android Studio
# File → Open → folder ul-manager

# 3. Poczekaj na sync Gradle (pierwsze uruchomienie pobiera ~500 MB)

# 4. Uruchom na emulatorze API 26+ lub fizycznym urządzeniu
# Run → app
```

Przy pierwszym uruchomieniu Room wykrywa pustą bazę i wywołuje `DatabaseSeeder.seed()`.

---

## Stan implementacji

| Ekran / funkcja | Status |
|----------------|--------|
| Login + walidacja | gotowe |
| Register + walidacja | gotowe |
| Dashboard — pasieki + zadania + checkbox | gotowe |
| Formularz przeglądu (tworzenie i edycja) | gotowe |
| Lista uli | placeholder |
| Szczegóły ula | placeholder |
| Formularz zadania | placeholder |
| Mapa pasiek | placeholder |
| Profil / ustawienia | placeholder |
| Połączenie z API (C# backend) | planowane |

---

## Uwagi dotyczące makiety

**`fallbackToDestructiveMigration()`** — baza danych jest niszczona i odtwarzana przy zmianie schematu Room. Przed release zastąpić obiektami `Migration`.

**Uwierzytelnianie** — logowanie i rejestracja są symulowane. `LoginViewModel` i `RegisterViewModel` nie korzystają z żadnego repozytorium danych — po opóźnieniu 1 s przekierowują bezwarunkowo na Dashboard.

**DTO** — klasy w `data/dto/` odzwierciedlają kontrakt przyszłego REST API. Pola dat są String (ISO-8601). Po dodaniu Retrofit wystarczy dołożyć adnotacje `@SerializedName` / `@Json` bez zmiany struktury klas.

**Package name** — `com.example.apiarymanager` jest placeholderem. Przed publikacją zmienić na docelową domenę odwróconą.
