package com.example.apiarymanager.data.local.seeder

import com.example.apiarymanager.data.dto.ApiaryDto
import com.example.apiarymanager.data.dto.HiveDto
import com.example.apiarymanager.data.dto.InspectionDto
import com.example.apiarymanager.data.dto.TaskDto
import com.example.apiarymanager.data.local.database.ApiaryManagerDatabase
import com.example.apiarymanager.data.mapper.toEntity

/**
 * Seeds the database with mock data on first install.
 * Called once from [DatabaseModule] via RoomDatabase.Callback.onCreate.
 *
 * All data goes through the DTO → Entity mapping chain to ensure
 * the full pipeline is exercised even with mock data.
 */
object DatabaseSeeder {

    suspend fun seed(db: ApiaryManagerDatabase) {
        seedApiaries(db)
        seedHives(db)
        seedInspections(db)
        seedTasks(db)
    }

    // ─── Apiaries ────────────────────────────────────────────────────────────

    private suspend fun seedApiaries(db: ApiaryManagerDatabase) {
        val apiaries = listOf(
            ApiaryDto(
                id        = 1,
                name      = "Pasieka Leśna",
                location  = "Bory Tucholskie, pow. Tuchola",
                latitude  = 53.5837,
                longitude = 17.8382,
                notes     = "Główna pasieka przy skraju lasu. Dobre pożytki lipowe.",
                createdAt = "2022-04-15"
            ),
            ApiaryDto(
                id        = 2,
                name      = "Pasieka Ogrodowa",
                location  = "Gdańsk-Oliwa",
                latitude  = 54.4068,
                longitude = 18.5601,
                notes     = "Pasieka miejska w ogrodzie botanicznym.",
                createdAt = "2023-03-20"
            )
        )
        db.apiaryDao().insertAll(apiaries.map { it.toEntity() })
    }

    // ─── Hives ───────────────────────────────────────────────────────────────

    private suspend fun seedHives(db: ApiaryManagerDatabase) {
        val hives = listOf(
            // Pasieka Leśna (apiaryId = 1)
            HiveDto(1, 1, 1, "Alfa",   2022, "ACTIVE", "Silna rodzina, dobra matka.",          "2022-04-20"),
            HiveDto(2, 1, 2, "Beta",   2023, "ACTIVE", "Wzorowa produkcja miodu.",              "2022-05-01"),
            HiveDto(3, 1, 3, "Gamma",  2021, "WEAK",   "Wymaga dokarmiania.",                  "2021-06-10"),
            HiveDto(4, 1, 4, "Delta",  null, "ACTIVE", "Nowy rój z podziału Alfa.",             "2024-06-01"),
            // Pasieka Ogrodowa (apiaryId = 2)
            HiveDto(5, 2, 1, "Omega",  2023, "ACTIVE", "Łagodna rodzina, polecana do miasta.", "2023-03-25"),
            HiveDto(6, 2, 2, "Sigma",  2022, "ACTIVE", "Dobra zbiórka nektaru.",               "2023-04-10"),
            HiveDto(7, 2, 3, "Theta",  2021, "WEAK",   "Matka stara, planowa wymiana.",        "2023-04-10"),
            HiveDto(8, 2, 4, "Lambda", null, "DEAD",   "Familia padła zimą 2023/2024.",        "2023-05-01")
        )
        db.hiveDao().insertAll(hives.map { it.toEntity() })
    }

    // ─── Inspections ─────────────────────────────────────────────────────────

    private suspend fun seedInspections(db: ApiaryManagerDatabase) {
        val inspections = listOf(
            // Ul Alfa
            InspectionDto(1,  1, "2024-04-10", true,  "EXCELLENT", 4.5f, 10, "Matka aktywna, piękny czerw."),
            InspectionDto(2,  1, "2024-05-15", true,  "GOOD",      8.2f, 12, "Gotowe do odbierania miodu."),
            // Ul Beta
            InspectionDto(3,  2, "2024-04-10", true,  "EXCELLENT", 5.0f, 11, "Wzorcowa rodzina."),
            InspectionDto(4,  2, "2024-05-15", true,  "EXCELLENT", 9.5f, 12, "Odebrano 4 kg miodu."),
            // Ul Gamma
            InspectionDto(5,  3, "2024-04-12", false, "POOR",      1.2f,  7, "Matki nie widać. Dokarmianie syropem."),
            InspectionDto(6,  3, "2024-05-16", true,  "FAIR",      2.5f,  8, "Matka odnaleziona po tygodniu."),
            // Ul Delta
            InspectionDto(7,  4, "2024-06-05", true,  "GOOD",      1.0f,  6, "Nowy rój dobrze przyjął ul."),
            // Ul Omega
            InspectionDto(8,  5, "2024-04-11", true,  "GOOD",      3.8f,  9, "Spokojne pszczoły."),
            InspectionDto(9,  5, "2024-05-20", true,  "EXCELLENT", 7.0f, 11, "Bardzo dobry sezon."),
            // Ul Sigma
            InspectionDto(10, 6, "2024-04-11", true,  "GOOD",      4.0f, 10, "Normalna aktywność."),
            // Ul Theta
            InspectionDto(11, 7, "2024-04-12", true,  "FAIR",      2.0f,  8, "Matka 3-letnia. Planowana wymiana."),
            // Ul Lambda — ostatni przegląd przed upadkiem
            InspectionDto(12, 8, "2023-10-20", true,  "POOR",      0.5f,  5, "Rodzina słaba wchodząca w zimę.")
        )
        db.inspectionDao().insertAll(inspections.map { it.toEntity() })
    }

    // ─── Tasks ───────────────────────────────────────────────────────────────

    private suspend fun seedTasks(db: ApiaryManagerDatabase) {
        val tasks = listOf(
            TaskDto(1, 1,    null, "Odbiór miodu — Pasieka Leśna",      "Zebrać plastry z uli Beta i Alfa.",   "2024-06-10", "HIGH",   false, "2024-05-20"),
            TaskDto(2, null, 3,    "Wymiana matki — ul Gamma",           "Zamówić unasienioną matkę z hodowli.","2024-06-01", "HIGH",   false, "2024-05-16"),
            TaskDto(3, null, 7,    "Wymiana matki — ul Theta",           "Stara matka, obniżona produkcja.",    "2024-06-15", "MEDIUM", false, "2024-05-20"),
            TaskDto(4, 2,    null, "Kontrola zimowli — Pasieka Ogrodowa","Sprawdzić zapasy przed zimą.",        "2024-10-01", "MEDIUM", false, "2024-05-20"),
            TaskDto(5, 1,    null, "Leczenie warrozy — Pasieka Leśna",   "Zastosować Apivar po zbiorach.",      "2024-08-01", "HIGH",   false, "2024-05-20"),
            TaskDto(6, null, 4,    "Obserwacja roju Delta",              "Sprawdzić przyjęcie się roju.",       "2024-06-12", "LOW",    true,  "2024-06-05"),
            TaskDto(7, 1,    null, "Malowanie uli",                      "Odświeżyć powłokę 3 uli w pasiece.", "2024-09-01", "LOW",    false, "2024-05-01")
        )
        db.taskDao().insertAll(tasks.map { it.toEntity() })
    }
}
