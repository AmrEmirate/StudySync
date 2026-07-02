# StudySyns Android App

StudySyns adalah aplikasi manajemen tugas berbasis Android dengan antarmuka modern yang terinspirasi dari gaya Google Play Store terbaru (Material 3). Aplikasi ini membantu pengguna dalam mengelola, menjadwalkan, dan melacak tugas sehari-hari.

## Fitur Utama
- **Autentikasi**: Mendukung pendaftaran dengan OTP Email (diverifikasi via Node.js backend) dan Google Sign-In.
- **Manajemen Tugas**: Lengkap dengan operasi CRUD (Create, Read, Update, Delete) tugas.
- **Tampilan Kalender**: Memvisualisasikan batas waktu tugas dengan menggunakan `kizitonwose-calendar`.
- **Integrasi API Sekolah**: Mendukung simulasi penarikan tugas langsung dari VPS atau server sekolah (*dummy API*).

## Teknologi yang Digunakan
- **Bahasa Pemrograman**: Kotlin
- **Arsitektur**: Retrofit untuk *network calls*, Coroutines
- **UI/UX**: Material Components (Bottom Sheets, Text Input Layout), ViewBinding
- **Backend (Terpisah)**: Node.js, Express, Prisma ORM, PostgreSQL

## Persiapan & Instalasi
1. Buka repositori ini menggunakan **Android Studio**.
2. Lakukan Sinkronisasi Gradle (Sync Project with Gradle Files).
3. Jika terdapat pesan error `Unsupported class file major version 70`, pastikan Anda mengatur **Gradle JDK** ke Java 17 atau Java 21 di `Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK`.
4. Jika backend Anda berada di server lain (bukan di emulator `localhost`), perbarui nilai `BASE_URL` di dalam file `NetworkClient.kt` dengan alamat VPS Anda (misalnya: `https://studysync-api.vimtk.cloud/`).
5. Jalankan aplikasi di emulator atau perangkat fisik Anda.

## Konfigurasi Tambahan
Untuk fitur pengiriman OTP dan Google Login, pastikan backend Anda sudah terkonfigurasi dengan file `.env` yang benar (berisi `SMTP_USER` dan `GOOGLE_CLIENT_ID`). Selain itu, di aplikasi Android ini, pastikan Anda juga menaruh *Client ID* yang valid di dalam `res/values/strings.xml`.
