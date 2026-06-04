# 🚀 Katia-Demo: End-to-End Hybrid Automation Testing

**Automated Web, Mobile, and API Testing Framework using Katalon Studio & Katia-Report**

Repositori ini adalah proyek demonstrasi untuk eksekusi pengujian perangkat lunak *End-to-End* (E2E) lintas platform. Proyek ini mengimplementasikan arsitektur **Modular Master-Slave**, yang memungkinkan Katalon Studio untuk menjalankan pengujian UI Web (Chrome), UI Mobile (Android/Appium), dan REST API secara berurutan, lalu merangkum seluruh hasilnya ke dalam **satu Master PDF Report** menggunakan utilitas `Katia-Report`.

## ✨ Fitur Utama

* **Modular Master-Slave Architecture:** Pengujian dipisah ke dalam *Test Suites* spesifik (Setup, Web, Mobile, API, Report) yang diorkestrasi melalui *Test Suite Collection* agar terhindar dari konflik antar *driver* (Web vs Mobile).
* **Smart Anti-Duplicate Logging:** Menggunakan file `result.json` sebagai penyimpan data (*state management*) antarsesi, dilengkapi dengan logika *idempotent* untuk mencegah *ghost data* saat terjadi *retry* otomatis.
* **Native Screenshot Bypass:** Mekanisme penangkapan layar (*screenshot*) tingkat lanjut yang langsung mengakses *Native Selenium/Appium Driver* untuk menghindari *StepFailedException* bawaan Katalon jika layar *freeze*.
* **Unified Master PDF:** Seluruh bukti pengujian (status *Pass/Fail*, *payload* API, dan tangkapan layar UI) dari berbagai platform dijahit secara otomatis menjadi 1 dokumen PDF yang sangat profesional dan mudah dibaca oleh *stakeholders*.

## 📂 Struktur Repositori & Arsitektur

Proyek ini menggunakan 5 pilar *Test Suite* utama yang berjalan secara **Sequential**:

```text
katia-demo/
│
├── Keywords/
│   └── KatiaReporter.groovy      # Mesin utama (Custom Keyword) untuk manipulasi JSON & pemanggilan PDF
│
├── katia_report/                 
│   ├── katia-report.exe          # Executable Node.js rendering PDF (jsPDF)
│   ├── result.json               # Temporary memory saat Test Suite berjalan
│   └── screenshots/              # Folder penyimpanan sementara gambar terkompresi
│
└── Test Suites/
    └── TSC_Master_E2E (Collection)
        ├── 1. TS_SETUP           # (Web Service) Menghapus sisa cache, JSON, dan gambar eksekusi sebelumnya
        ├── 2. TS_MOBILE          # (Android) Mengeksekusi Mobile App Testing (ex: Swag Labs)
        ├── 3. TS_WEB             # (Chrome) Mengeksekusi Web Testing
        ├── 4. TS_API             # (Web Service) Mengeksekusi REST API Testing
        └── 5. TS_REPORT          # (Web Service) Memicu Katia-Report untuk merender result.json menjadi PDF

```

## 🛠️ Prasyarat (Prerequisites)

Sebelum menjalankan proyek ini di mesin lokal Anda, pastikan Anda telah menginstal:

1. **Katalon Studio** (Direkomendasikan versi 8.x atau terbaru).
2. **Appium & Android SDK** (Untuk mengeksekusi `TS_MOBILE`).
3. **Katia-Report Executable:** Pastikan file `katia-report.exe` sudah berada di dalam folder `katia_report/` di root proyek ini. *(Anda bisa mem-buildnya dari source `index.js` menggunakan `pkg`)*.

## 🚀 Cara Menjalankan Eksekusi (How to Run)

1. Buka proyek ini menggunakan Katalon Studio.
2. Navigasikan ke panel **Test Suites** -> Buka **`TSC_Master_E2E`** (Test Suite Collection).
3. Pastikan **Execution Mode** disetel ke **Sequential**.
4. Pastikan profil *Environment* di tabel sudah sesuai (Chrome untuk Web, Android untuk Mobile, Web Service untuk Setup & Report).
5. Klik tombol **Execute**.
6. Katalon akan menjalankan orkestrasi lintas platform secara otomatis.
7. Setelah eksekusi selesai, buka folder `katia_report/` di komputer Anda. Anda akan menemukan file laporan dengan format `[ID]_[Nama]_[Timestamp].pdf`.

## 📜 Contoh Output Laporan

*Katia-Report* akan menghasilkan PDF dengan struktur:

1. **Halaman Cover:** Detail proyek, tanggal eksekusi, dan daftar *Test Case*.
2. **Ringkasan Eksekusi:** *Dashboard* jumlah total tes, status *PASSED*, dan *FAILED*.
3. **Detail Eksekusi (Tabel):** Daftar komprehensif seluruh status ID.
4. **Lampiran Step & Bukti Data:** Rincian setiap langkah (*step-by-step*), *payload* (untuk API), dan kompresi gambar tangkapan layar (untuk Web/Mobile).

---

*Dibuat untuk menyederhanakan pelaporan SIT/UAT pada pengujian Hybrid berskala besar.*
