# <h1 align="center">Tugas Kecil 2 IF2211 Strategi Algoritma</h1>
# <h2 align="center">Semester II tahun 2024/2025</h2>
# <h2 align="center">Implementasi Divide And Conquer Pada Kompresi Gambar</h2>

<p align="center">
  <img src="test/sol/tc1_1.gif" alt="Quadtree Compression GIF" width="500"/>
</p>

## Daftar Isi
- [Deskripsi](#deskripsi)
- [Struktur Program](#struktur-program)
- [Persyaratan & Instalasi](#persyaratan--instalasi)
- [Kompilasi](#kompilasi)
- [Cara Penggunaan](#cara-penggunaan)
- [Penulis](#penulis)
- [Referensi](#referensi)

## Deskripsi
Repositori ini berisi aplikasi berbasis Java yang dikembangkan sebagai bagian dari mata kuliah IF2211 Strategi Algoritma. Proyek ini berfokus pada kompresi gambar menggunakan metode Quadtree, sebuah pendekatan divide-and-conquer yang secara rekursif membagi gambar menjadi blok-blok yang lebih kecil berdasarkan kesamaan warna.

Untuk menentukan homogenitas blok, program mengimplementasikan lima metrik kesalahan:
- Variance (Varians)
- Mean Absolute Deviation (MAD)
- Max Pixel Difference (Perbedaan Piksel Maksimum)
- Entropy (Entropi)
- Structural Similarity Index (SSIM)

Selain mengompresi gambar, program ini juga mendukung pembuatan **visualisasi GIF** yang mengilustrasikan proses kompresi secara langsung. Hal ini memungkinkan pengguna untuk lebih memahami bagaimana struktur quadtree beradaptasi dengan berbagai area gambar. Program ini ditulis sepenuhnya dalam bahasa Java, dengan penekanan pada efisiensi dan kejelasan dalam desain algoritma.

## Struktur Program
```
├── README.md
├── bin
│   ├── ErrorMetrics.class
│   ├── InputParser.class
│   ├── OutputHandler.class
│   ├── Main.class
│   ├── Quadtree.class
│   ├── QuadtreeNode.class
│   ├── MADErrorMetric.class
│   ├── SSIMErrorMetric.class
│   ├── MaxPixelDifferenceErrorMetric.class
│   ├── EntropyErrorMetric.class
│   ├── Pixel.class
│   ├── RGBMatrix.class
│   ├── GifSequenceWriter.class
│   ├── CompressionController.class
│   ├── CompressedImage.class
│   └── VarianceErrorMetric.class
├── doc
│   └── Tucil2_13523042_13523008.pdf
├── src
│   ├── ErrorMetrics.java
│   ├── InputParser.java
│   ├── OutputHandler.java
│   ├── Main.java
│   ├── Quadtree.java
│   ├── QuadtreeNode.java
│   ├── MADErrorMetric.java
│   ├── SSIMErrorMetric.java
│   ├── MaxPixelDifferenceErrorMetric.java
│   ├── EntropyErrorMetric.java
│   ├── Pixel.java
│   ├── RGBMatrix.java
│   ├── GifSequenceWriter.java
│   ├── CompressionController.java
│   ├── CompressedImage.java
│   └── VarianceErrorMetric.java
└── test
    ├── sol
    └── tc
```
- **bin** : berisi file .class Java yang dapat dieksekusi yang dikompilasi dari kode sumber di folder src.
- **src** : berisi file kode sumber program utama (.java).
- **doc** : berisi laporan tugas dan dokumentasi program.
- **test** : berisi hasil pengujian yang disertakan dalam laporan.

## Persyaratan & Instalasi
1. Instal Java Development Kit (JDK) 17 atau lebih baru
2. Kloning repositori
    ```bash
    git clone https://github.com/ivan-wirawan/Tucil2_13523008_13523042.git
    ```
3. Pindah ke direktori proyek
    ```bash
    cd Tucil2_13523008_13523042
    ```

## Kompilasi

```bash
javac -d bin src/*.java
```

## Cara Penggunaan
```bash
cd bin
java Main
```

## Penjelasan Asumsi
1. Dalam implementasi kelas SSIMErrorMetric, terdapat beberapa asumsi yang disamakan untuk memastikan perhitungan SSIM berjalan dengan konsisten. Pertama, perhitungan SSIM dilakukan secara terpisah untuk setiap saluran warna (R, G, dan B), kemudian dikombinasikan dengan bobot tetap (W_R, W_G, W_B) untuk mendapatkan nilai akhir. Kedua, rata-rata warna dalam blok gambar dihitung dengan menjumlahkan semua nilai warna pada piksel dalam area yang ditentukan, lalu dibagi dengan jumlah piksel yang dihitung, dengan asumsi bahwa blok tersebut tidak kosong. Jika jumlah piksel dalam blok adalah nol, maka rata-rata warna akan dikembalikan sebagai hitam (0, 0, 0). Ketiga, dalam perhitungan variansi dan standar deviasi, asumsi bahwa jumlah piksel dalam blok minimal satu dijaga dengan pengecekan count == 0, yang akan mengembalikan SSIM maksimal (1.0) jika tidak ada piksel yang valid untuk dihitung. Keempat, dalam formula SSIM, konstanta C1 dan C2 ditetapkan berdasarkan nilai maksimum piksel 255 untuk mencegah pembagian oleh nol serta menjaga stabilitas numerik dalam perhitungan. Kelima, fungsi getChannelValue mengasumsikan bahwa indeks kanal selalu valid (0 untuk merah, 1 untuk hijau, dan 2 untuk biru), sehingga nilai kanal diperoleh tanpa pengecekan tambahan di luar batas yang sudah ditentukan. Dengan asumsi-asumsi ini, implementasi tetap sederhana dan efisien dalam menghitung SSIM untuk blok gambar tertentu.

2. Pada target compression percentage menggunakan metode SSIM karena SSIM yang paling stabil threshold (0 - 1)

## Penulis
| **NIM**  | **Nama Anggota**               |
| -------- | ------------------------------ |
| 13523008 | Varel Tiara                    |
| 13523042 | Abdullah Farhan                |

## Referensi
Munir, Rinaldi. 2025. “Algoritma Divide and Conquer (Bagian 1)” (https://informatika.stei.itb.ac.id/~rinaldi.munir/Stmik/2024-2025/07-Algoritma-Divide-and-Conquer-(2025)-Bagian1.pdf) 
Munir, Rinaldi. 2025. “Algoritma Divide and Conquer (Bagian 2)” (https://informatika.stei.itb.ac.id/~rinaldi.munir/Stmik/2024-2025/08-Algoritma-Divide-and-Conquer-(2025)-Bagian2.pdf) 
Munir, Rinaldi. 2025. “Algoritma Divide and Conquer (Bagian 3)” (https://informatika.stei.itb.ac.id/~rinaldi.munir/Stmik/2024-2025/09-Algoritma-Divide-and-Conquer-(2025)-Bagian3.pdf) 
Munir, Rinaldi. 2025. “Algoritma Divide and Conquer (Bagian 4)” (https://informatika.stei.itb.ac.id/~rinaldi.munir/Stmik/2024-2025/10-Algoritma-Divide-and-Conquer-(2025)-Bagian4.pdf)
