# <h1 align="center">Tugas Kecil 2 IF2211 Strategi Algoritma</h1>
# <h2 align="center">Semester II tahun 2024/2025</h2>
# <h2 align="center">Implementasi Divide And Conquer Pada Kompresi Gambar</h2>

<p align="center">
  <img src="test/tc/tc1_1.gif" alt="Quadtree Compression GIF" width="500"/>
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
│   ├── CompressionController.java.java
│   ├── CompressedImage.java.java
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
java -cp bin Main
```

Anda juga dapat menonton video di bawah ini untuk mempelajari cara menggunakan program:
[![Cara Menggunakan Program](https://img.youtube.com/vi/GmKJs3wif9k/0.jpg)](https://youtu.be/GmKJs3wif9k)

## Penulis
| **NIM**  | **Nama Anggota**               |
| -------- | ------------------------------ |
| 13523042 | Varel Tiara                    |
| 13523008 | Abdullah Farhan                |

## Referensi
Munir, Rinaldi. 2025. "Algoritma Divide and Conquer (Bagian 1)" (https://informatika.stei.itb.ac.id/~rinaldi.munir/Stmik/2024-2025/07-Algoritma-Divide-and-Conquer-(2025)-Bagian1.pdf, diakses 8 April 2025)  
Munir, Rinaldi. 2025. "Algoritma Divide and Conquer (Bagian 2)" (https://informatika.stei.itb.ac.id/~rinaldi.munir/Stmik/2024-2025/05-Algoritma-Greedy-(2025)-Bag2.pdf, diakses 8 April 2025)  
Munir, Rinaldi. 2025. "Algoritma Divide and Conquer (Bagian 3)" (https://informatika.stei.itb.ac.id/~rinaldi.munir/Stmik/2024-2025/09-Algoritma-Divide-and-Conquer-(2025)-Bagian3.pdf, diakses 8 April 2025)  
Munir, Rinaldi. 2025. "Algoritma Divide and Conquer (Bagian 4)" (https://informatika.stei.itb.ac.id/~rinaldi.munir/Stmik/2024-2025/10-Algoritma-Divide-and-Conquer-(2025)-Bagian4.pdf, diakses 8 April 2025)