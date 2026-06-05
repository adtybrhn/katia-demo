# 🚀 Katia-Demo: End-to-End Hybrid Automation Testing

**Automated Web, Mobile, and API Testing Framework using Katalon Studio & Katia-Report**

Repositori ini adalah proyek demonstrasi untuk eksekusi pengujian perangkat lunak *End-to-End* (E2E) lintas platform. Proyek ini mengimplementasikan arsitektur **Modular Master-Slave**, yang memungkinkan Katalon Studio untuk menjalankan pengujian UI Web (Chrome), UI Mobile (Android/Appium), dan REST API secara berurutan, lalu merangkum seluruh hasilnya ke dalam **satu Master PDF Report** menggunakan utilitas `Katia-Report`.

## ✨ Fitur Utama

* **Modular Master-Slave Architecture:** Pengujian dipisah ke dalam *Test Suites* spesifik (Mobile, Web, API, Report) yang diorkestrasi melalui *Test Suite Collection* agar terhindar dari konflik antar *driver* (Web vs Mobile).
* **Native Groovy Implementation:** Menggunakan *Class Groovy* murni di dalam direktori `Include/scripts` tanpa bergantung pada fitur *Custom Keyword* Katalon, membuat pemanggilan *method* lebih cepat dan berorientasi objek (OOP).
* **Smart Anti-Duplicate Logging:** Menggunakan file `result.json` sebagai penyimpan data (*state management*) antarsesi, dilengkapi dengan logika *idempotent* untuk mencegah *ghost data* saat terjadi *retry* otomatis.
* **Native Screenshot Bypass:** Mekanisme penangkapan layar (*screenshot*) tingkat lanjut yang langsung mengakses *Native Selenium/Appium Driver* untuk menghindari *StepFailedException* jika terjadi *freeze* pada UI.
* **Unified Master PDF & Auto-Teardown:** Seluruh bukti pengujian dari berbagai platform dijahit secara otomatis menjadi 1 dokumen PDF profesional. Setelah PDF berhasil dicetak, sistem akan melakukan *Auto-Teardown* (pembersihan otomatis) pada *file temporary* agar lingkungan (*environment*) selalu bersih untuk eksekusi berikutnya.

## 📂 Struktur Repositori & Arsitektur

Proyek ini menggunakan struktur kelas *utility* dan 4 pilar *Test Suite* utama yang berjalan secara **Sequential**:

```text
katia-demo/
│
├── Include/
│   └── scripts/
│       └── groovy/
│           └── KatiaReporter.groovy      # Class utility utama (Default Package) untuk manipulasi JSON & PDF
│
├── katia_report/                 
│   ├── katia-report.exe          # Executable Node.js rendering PDF (jsPDF)
│   ├── result.json               # Temporary memory saat Test Suite berjalan (terhapus otomatis di akhir)
│   └── screenshots/              # Folder penyimpanan sementara gambar (terhapus otomatis di akhir)
│
└── Test Suites/
    └── TSC_Master_E2E (Collection)
        ├── 1. TS_MOBILE          # (Android) Mengeksekusi Mobile App Testing (ex: Swag Labs)
        ├── 2. TS_WEB             # (Chrome) Mengeksekusi Web Testing
        ├── 3. TS_API             # (Web Service) Mengeksekusi REST API Testing
        └── 4. TS_REPORT          # (Web Service) Merender PDF & eksekusi Auto-Teardown (Pembersihan memori)

```

## 🛠️ Prasyarat (Prerequisites)

Sebelum menjalankan proyek ini di mesin lokal Anda, pastikan Anda telah menginstal:

1. **Katalon Studio** (Direkomendasikan versi 8.x atau terbaru).
2. **Appium & Android SDK** (Untuk mengeksekusi `TS_MOBILE`).
3. **Katia-Report Executable:** Pastikan file `katia-report.exe` sudah berada di dalam folder `katia_report/` di *root* proyek ini.

## 🚀 Cara Menjalankan Eksekusi (How to Run)

1. Buka proyek ini menggunakan Katalon Studio.
2. Navigasikan ke panel **Test Suites** -> Buka **`TSC_Master_E2E`** (Test Suite Collection).
3. Pastikan **Execution Mode** disetel ke **Sequential**.
4. Pastikan profil *Environment* pada kolom **Run With** sudah sesuai (Android untuk Mobile, Chrome untuk Web, Web Service untuk API & Report).
5. Klik tombol **Execute**.
6. Katalon akan menjalankan orkestrasi lintas platform secara otomatis.
7. Setelah eksekusi selesai, buka folder `katia_report/`. Anda akan menemukan file laporan utuh dengan format `[ID]_[Nama]_[Timestamp].pdf`, dan folder tersebut akan langsung bersih dari *file* gambar/JSON *temporary* berkat fitur *Auto-Teardown*.

## 📜 Contoh Output Laporan

*Katia-Report* akan menghasilkan PDF dengan struktur:

1. **Halaman Cover:** Detail proyek, tanggal eksekusi, dan daftar *Test Case*.
2. **Ringkasan Eksekusi:** *Dashboard* jumlah total tes, status *PASSED*, dan *FAILED*.
3. **Detail Eksekusi (Tabel):** Daftar komprehensif seluruh status ID E2E.
4. **Lampiran Step & Bukti Data:** Rincian setiap langkah (*step-by-step*), *payload* (untuk API), dan kompresi gambar tangkapan layar (untuk Web/Mobile).

---

*Arsitektur E2E Testing Automation - Dirancang untuk menyederhanakan pelaporan SIT/UAT lintas platform secara profesional.*
