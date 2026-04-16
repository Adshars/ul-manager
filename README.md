# ApiaryManager

Aplikacja mobilna do zarządzania pasieką. Aktualny stan repozytorium to **funkcjonalna makieta (Functional Mockup)** — cały interfejs działa w oparciu o lokalne dane zapisywane w bazie Room. Docelowy backend w C# zostanie podłączony bez przebudowy warstwy prezentacji.

---

## Stos technologiczny

| Obszar | Technologia |
|--------|-------------|
| Język | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Architektura | MVVM + Clean Architecture |
| DI | Hilt 2.54 |
| Baza danych | Room 2.6 (schemat v4) |
| Nawigacja | Navigation Compose 2.8 (type-safe routes) |
| Asynchroniczność | Kotlin Coroutines + Flow |
| Generowanie QR | ZXing Core |
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

## Funkcjonalności makiety

### Uwierzytelnianie

- **Logowanie** — walidacja formatu e-mail i minimalnej długości hasła; symulacja żądania sieciowego (spinner 1 s) i przejście do Dashboard
- **Rejestracja** — walidacja pól (e-mail, hasło, potwierdzenie hasła, nazwa użytkownika); symulowane tworzenie konta
- **Resetowanie hasła** — osobny ekran z polem e-mail i symulacją wysyłki linku resetującego

### Nawigacja

- Boczna szuflada (`ModalNavigationDrawer`) z pozycjami: Dashboard, Pasieki, Zadania, Statystyki, Ustawienia
- Aktywna pozycja menu wyróżniona dynamicznie na podstawie bieżącej trasy
- Przycisk hamburgera w TopAppBar na ekranach listy

### Dashboard

- Lista pasiek z liczbą aktywnych uli przy każdej; kliknięcie przechodzi do listy uli pasieki
- Lista zadań na dziś i zaległych, sortowana po dacie; checkbox natychmiast zapisuje zmianę statusu do Room
- **Szybkie akcje:**
  - *Nowy przegląd* i *Miodobranie* — otwierają dwuetapowy `ModalBottomSheet`: wybór pasieki → wybór ula, po czym przechodzą do odpowiedniego formularza
  - *Dodaj zadanie* — przechodzi bezpośrednio do formularza zadania

### Pasieki (`ApiaryListScreen`)

- Lista pasiek z lokalizacją i liczbą aktywnych uli
- Dodawanie, edycja i usuwanie pasiek z dialogiem potwierdzenia usunięcia
- Kliknięcie pasieki otwiera listę jej uli

### Ule

- **Lista uli** (`HiveListScreen`) — karty z nazwą, numerem i statusem; przejście do szczegółów ula lub formularza dodawania
- **Formularz ula** (`HiveFormScreen`) — pola: nazwa, numer, rok matki, typ ramy, liczba nadstawek, pochodzenie matki, status, notatki; przy tworzeniu nowego ula automatycznie generowany jest unikalny UUID przypisany jako kod QR
- **Szczegóły ula** (`HiveDetailScreen`) — widok zakładkowy z sekcjami: Szczegóły, Przeglądy, Miodobrania, Leczenia, Dokarmiania, Zadania; FAB dodaje nowy rekord w aktywnej zakładce
- **Kod QR ula** (`HiveQrScreen`) — wyświetla wygenerowany kod QR; opcja wysłania kodu pocztą e-mail jako załącznik PNG; regeneracja kodu (z dialogiem potwierdzenia — stary kod przestaje obowiązywać)

### Przegląd ula (`InspectionFormScreen`)

- Tryby tworzenia i edycji
- DatePickerDialog (Material 3) z domyślną datą dzisiejszą
- Trzy checkboxy: matka widoczna, czerw widoczny, mateczniki widoczne
- Slider siły rodziny (5 stopni: Krytyczna → Bardzo Silna)
- Sekcja zarządzania ramkami (switch + AnimatedVisibility): nadstawki dodane/usunięte, ramki z suszem, węzy
- Dodawanie zdjęć z aparatu lub galerii; podgląd miniatur z możliwością usunięcia
- Pola tekstowe na problemy i notatki

### Miodobranie, Leczenia, Dokarmiania

Osobne formularze (`HarvestFormScreen`, `TreatmentFormScreen`, `FeedingFormScreen`) powiązane z ulem; dane zapisywane do Room.

### Zadania (`TaskListScreen`)

- **Widok listy** — zadania pogrupowane w sekcje: Zaległe, Dzisiaj, Nadchodzące, Ukończone
- **Widok kalendarza** — niestandardowy kalendarz miesięczny z nawigacją, polskimi nagłówkami dni (pn–nd), kolorowymi kropkami pod datami, które mają zaplanowane zadania, oraz wyróżnieniem wybranego dnia
- Filtr: Wszystkie / Aktywne / Ukończone
- Checkbox przy każdym zadaniu natychmiast zapisuje zmianę statusu

### Formularz zadania (`TaskFormScreen`)

Tworzenie i edycja zadań z opcjonalnym powiązaniem z pasieką lub ulem, datą wykonania i priorytetem.

### Dane startowe (seed)

Przy pierwszej instalacji (i przy destruktywnej migracji) baza jest wypełniana danymi mockowanymi:

- 2 pasieki: Pasieka Leśna (Bory Tucholskie) i Pasieka Ogrodowa (Gdańsk-Oliwa)
- 8 uli o zróżnicowanych statusach (ACTIVE, WEAK, DEAD)
- 12 przeglądów z realistycznymi obserwacjami
- Zadania z różnymi priorytetami i terminami (część przeterminowana — celowo, żeby widoki pokazywały niepuste listy)

---

## Struktura pakietów

```
com.example.apiarymanager/
├── core/
│   ├── security/               PinManager (biometria / PIN)
│   └── util/                   Resource.kt
├── data/
│   ├── dto/                    ApiaryDto, HiveDto, InspectionDto, TaskDto, ...
│   ├── local/
│   │   ├── dao/                ApiaryDao, HiveDao, InspectionDao, TaskDao, ...
│   │   ├── database/           ApiaryManagerDatabase (Room, v4)
│   │   └── seeder/             DatabaseSeeder
│   ├── mapper/                 *Mapper.kt — DTO↔Entity↔Domain
│   ├── entity/                 *Entity.kt — Room @Entity z @ForeignKey
│   └── repository/             *RepositoryImpl.kt
├── di/
│   ├── CoroutinesModule.kt     @ApplicationScope CoroutineScope
│   ├── DatabaseModule.kt       Room + fallbackToDestructiveMigration (DEV)
│   └── RepositoryModule.kt     @Binds interface → impl
├── domain/
│   ├── model/                  Apiary, Hive, Inspection, Task, HoneyHarvest, ...
│   ├── repository/             interfejsy repozytoriów
│   └── usecase/                SaveInspectionUseCase
└── presentation/
    ├── navigation/             Screen.kt (type-safe routes), AppNavGraph.kt, AppDrawer.kt
    ├── theme/                  Color.kt, Type.kt, Theme.kt
    ├── auth/
    │   └── forgotpassword/     ForgotPasswordScreen, ForgotPasswordViewModel
    ├── login/                  LoginScreen, LoginViewModel
    ├── register/               RegisterScreen, RegisterViewModel
    ├── dashboard/              DashboardScreen, DashboardViewModel, HivePickerBottomSheet
    ├── apiary/                 ApiaryListScreen, ApiaryFormScreen + ViewModels
    ├── hive/
    │   ├── list/               HiveListScreen, HiveListViewModel
    │   ├── form/               HiveFormScreen, HiveFormViewModel
    │   ├── detail/             HiveDetailScreen, HiveDetailViewModel
    │   └── qr/                 HiveQrScreen, HiveQrViewModel
    ├── inspection/             InspectionFormScreen, InspectionFormViewModel
    ├── harvest/                HarvestFormScreen, HarvestFormViewModel
    ├── treatment/              TreatmentFormScreen, TreatmentFormViewModel
    ├── feeding/                FeedingFormScreen, FeedingFormViewModel
    ├── task/                   TaskListScreen, TaskFormScreen + ViewModels
    ├── statistics/             StatisticsScreen (placeholder)
    └── settings/               SettingsScreen (placeholder)
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
| Resetowanie hasła (makieta) | gotowe |
| Dashboard — pasieki, zadania, szybkie akcje | gotowe |
| Lista pasiek z CRUD | gotowe |
| Formularz pasieki | gotowe |
| Lista uli | gotowe |
| Formularz ula z auto-generacją QR | gotowe |
| Szczegóły ula (6 zakładek) | gotowe |
| Kod QR ula — podgląd, email, regeneracja | gotowe |
| Formularz przeglądu (tworzenie i edycja) | gotowe |
| Formularz miodobrania | gotowe |
| Formularz leczenia | gotowe |
| Formularz dokarmiania | gotowe |
| Lista zadań (lista + kalendarz) | gotowe |
| Formularz zadania | gotowe |
| Zdjęcia (aparat + galeria) | gotowe |
| Nawigacja boczna (drawer) | gotowe |
| Statystyki | placeholder |
| Ustawienia | placeholder |
| Połączenie z API (C# backend) | planowane |

---

## Uwagi dotyczące makiety

**`fallbackToDestructiveMigration()`** — baza danych jest niszczona i odtwarzana przy zmianie schematu Room. Przed release zastąpić obiektami `Migration`.

**Uwierzytelnianie** — logowanie i rejestracja są symulowane. Po opóźnieniu 1 s przekierowują bezwarunkowo na Dashboard. Dane nie są nigdzie zapisywane — uwierzytelnianie przyjdzie z backendem.

**Kod QR** — każdy ul ma unikalny UUID jako treść kodu QR, generowany przy tworzeniu ula i zapisywany w Room. Kod można zregenerować (nowy UUID, stary przestaje obowiązywać). Wysyłka e-mail korzysta z `Intent.ACTION_SEND` — otwiera systemowy arkusz udostępniania.

**DTO** — klasy w `data/dto/` odzwierciedlają kontrakt przyszłego REST API. Pola dat są String (ISO-8601). Po dodaniu Retrofit wystarczy dołożyć adnotacje `@SerializedName` / `@Json` bez zmiany struktury klas.

**Package name** — `com.example.apiarymanager` jest placeholderem. Przed publikacją zmienić na docelową domenę odwróconą.
