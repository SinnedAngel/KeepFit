package com.example.ui

import androidx.compose.ui.unit.dp

// Helper int to dp to prevent issues in standard compilation
fun Int.pngToDp(): androidx.compose.ui.unit.Dp {
    return this.dp
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

object KatedaL10n {
    fun get(key: String, lang: String): String {
        val entry = translations[key] ?: return key
        return entry[lang] ?: entry["EN"] ?: key
    }

    private val translations = mapOf(
        "app_subtitle" to mapOf("EN" to "A subsidiary of Kateda", "ID" to "Anak perusahaan Kateda"),
        "tab_dashboard" to mapOf("EN" to "Dashboard", "ID" to "Dasbor"),
        "tab_exercises" to mapOf("EN" to "Exercises", "ID" to "Latihan"),
        "tab_reminder_logs" to mapOf("EN" to "Reminder & Logs", "ID" to "Alarm & Catatan"),
        "greetings" to mapOf("EN" to "Greetings", "ID" to "Salam"),
        "level" to mapOf("EN" to "Level", "ID" to "Tingkatan"),
        "sub_agency" to mapOf("EN" to "Subsidiary of Kateda Central Energy", "ID" to "Afiliasi Energi Pusat Kateda"),
        "daily_steps" to mapOf("EN" to "Daily Steps Circulation", "ID" to "Sirkulasi Langkah Harian"),
        "goal" to mapOf("EN" to "Goal", "ID" to "Target"),
        "completed_percentage" to mapOf("EN" to "completed", "ID" to "selesai"),
        "calibrate" to mapOf("EN" to "Calibrate Walk Steps Log", "ID" to "Kalibrasi Langkah"),
        "valid_steps_err" to mapOf("EN" to "Please enter standard positive digits for steps", "ID" to "Harap masukkan langkah yang valid"),
        "bmi_alignment" to mapOf("EN" to "BMI & Weight Alignment", "ID" to "Penyelarasan Berat Badan & BMI"),
        "current_weight" to mapOf("EN" to "Current Weight", "ID" to "Berat Badan"),
        "current_height" to mapOf("EN" to "Current Height", "ID" to "Tinggi Badan"),
        "bmi_index" to mapOf("EN" to "Body Mass Index (BMI)", "ID" to "Indeks Massa Tubuh (BMI)"),
        "diagnostic" to mapOf("EN" to "Kateda Energy Diagnostic", "ID" to "Diagnosis Energi Kateda"),
        "valid_weight_err" to mapOf("EN" to "Please enter valid weight between 20kg and 300kg", "ID" to "Harap masukkan berat antara 20kg & 300kg"),
        "fitness_flows" to mapOf("EN" to "Kateda Subsidiary Fitness Flows", "ID" to "Sirkulasi Aliran Latihan Kateda"),
        "no_exercise_level" to mapOf("EN" to "No customized physical exercises found for this level.\nTry choosing a different martial art level in your profile settings.", "ID" to "Tidak ada latihan fisik yang disesuaikan untuk tingkat ini.\nCoba pilih tingkat seni bela diri yang berbeda di pengaturan profil."),
        "mins" to mapOf("EN" to "mins", "ID" to "menit"),
        "coordinated_movs" to mapOf("EN" to "Coordinated Movements:", "ID" to "Kombinasi Gerakan:"),
        "start_guided" to mapOf("EN" to "Start Guided Practice Timer", "ID" to "Mulai Sesi Latihan"),
        "quick_log" to mapOf("EN" to "Quick Log Practice Finish", "ID" to "Catat Selesai Cepat"),
        "dismiss" to mapOf("EN" to "DISMISS", "ID" to "BATAL"),
        "daily_reminder" to mapOf("EN" to "Daily Physical Reminder", "ID" to "Pengingat Latihan Harian"),
        "set_alerts" to mapOf("EN" to "Set alerts for martial breathing routines", "ID" to "Atur waktu pengingat latihan pernapasan harian"),
        "current_slot" to mapOf("EN" to "Current Alert Slot", "ID" to "Jadwal Alarm Aktif"),
        "reminders_silent" to mapOf("EN" to "Daily reminders are currently silent.", "ID" to "Jadwal pengingat harian saat ini kosong."),
        "exercise_log" to mapOf("EN" to "Exercise Circulation Log", "ID" to "Catatan Distribusi Latihan"),
        "logged" to mapOf("EN" to "logged", "ID" to "tercatat"),
        "no_routines_logged" to mapOf("EN" to "No completed martial routines logged yet.\nHead over to the Exercises tab and finish a session!", "ID" to "Kamu belum mencatat latihan apa pun.\nMulai latihan Anda dari tab Latihan!"),
        "steps_log" to mapOf("EN" to "Steps Distribution Log", "ID" to "Catatan Sirkulasi Langkah"),
        "no_history_logged" to mapOf("EN" to "No history logged yet.", "ID" to "Belum ada catatan."),
        "historic_bmi" to mapOf("EN" to "Historic Body Mass Index checks", "ID" to "Pemeriksaan Indeks Massa Tubuh"),
        "no_weight_log" to mapOf("EN" to "No weight log entries yet.", "ID" to "Belum ada catatan berat."),
        "stats" to mapOf("EN" to "Stats", "ID" to "Statistik"),
        "edit_profile_title" to mapOf("EN" to "Edit Keep Fit Profile", "ID" to "Ubah Profil Keep Fit"),
        "select_level" to mapOf("EN" to "Select Kateda Health Level:", "ID" to "Pilih Tingkatan Kesehatan Kateda:"),
        "voice_lang" to mapOf("EN" to "Vocal Command Language (TTS):", "ID" to "Bahasa Panduan Suara (TTS):"),
        "tech_guide_title" to mapOf("EN" to "Technique Video & Alignment Guide", "ID" to "Panduan Aliran & Gerakan"),
        "ref_photo" to mapOf("EN" to "Reference Photo/Video", "ID" to "Referensi Teknik/Gerakan"),
        "est_flow" to mapOf("EN" to "Estimated Flow", "ID" to "Estimasi Durasi"),
        "target_level" to mapOf("EN" to "Target Level", "ID" to "Tingkatan Target"),
        "sim_loop" to mapOf("EN" to "Simulated Loop:", "ID" to "Animasi Gerakan:"),
        "watch_tutorial" to mapOf("EN" to "Watch Tutorial", "ID" to "Tonton Tutorial"),
        "config_practice" to mapOf("EN" to "Configure Practice Flow", "ID" to "Konfigurasi Sesi Latihan"),
        "vocal_guide" to mapOf("EN" to "Vocal Guide", "ID" to "Panduan Suara"),
        "tts_commands" to mapOf("EN" to "TTS voice commands", "ID" to "Perintah suara"),
        "lung_wave" to mapOf("EN" to "Lung Wave", "ID" to "Gelombang Paru"),
        "breathing_wind" to mapOf("EN" to "Breathing audio wind", "ID" to "Efek napas udara"),
        "tot_duration" to mapOf("EN" to "Total Session Duration", "ID" to "Total Durasi Latihan"),
        "begin_guided" to mapOf("EN" to "BEGIN GUIDED PRACTICE", "ID" to "MULAI LATIHAN"),
        "cycle_of" to mapOf("EN" to "CYCLE", "ID" to "PUTARAN"),
        "cycle_of_connector" to mapOf("EN" to "OF", "ID" to "DARI"),
        "total_elapsed" to mapOf("EN" to "Total elapsed", "ID" to "Total waktu"),
        "flow_completed" to mapOf("EN" to "Kateda Flow Completed!", "ID" to "Latihan Kateda Selesai!"),
        "flow_completed_desc" to mapOf("EN" to "Your physical vessels are oxygenated and central energy has been harmonized.", "ID" to "Aliran darah Anda telah teroksigenasi, serta energi inti tubuh Anda telah selaras."),
        "time_spent" to mapOf("EN" to "TIME SPENT", "ID" to "DURASI"),
        "est_burn" to mapOf("EN" to "ESTIMATED BURN", "ID" to "ESTIMASI KALORI"),
        "cycles_done" to mapOf("EN" to "CYCLES DONE", "ID" to "PUTARAN SELESAI"),
        "save_practice" to mapOf("EN" to "SAVE PRACTICE TO DIARY", "ID" to "SIMPAN LATIHAN"),
        "close_no_log" to mapOf("EN" to "Close without logging", "ID" to "Tutup tanpa menyimpan"),
        "log_manual_steps" to mapOf("EN" to "Log Manual Steps", "ID" to "Catat Langkah Manual"),
        "add_custom_steps" to mapOf("EN" to "Add Custom Steps", "ID" to "Tambah Langkah Manual"),
        "enter_steps_hint" to mapOf("EN" to "Enter steps count", "ID" to "Masukkan jumlah langkah"),
        "quick_add" to mapOf("EN" to "Quick Add", "ID" to "Tambah Cepat")
    )
}
