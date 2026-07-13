-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 13, 2026 at 04:44 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `cssd_rumahsakit`
--

-- --------------------------------------------------------

--
-- Table structure for table `alat`
--

CREATE TABLE `alat` (
  `id_alat` int(11) NOT NULL,
  `nama_alat` varchar(100) DEFAULT NULL,
  `id_jenis` int(11) DEFAULT NULL,
  `tipe` enum('satuan','set','linen') DEFAULT NULL,
  `stok` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `alat`
--

INSERT INTO `alat` (`id_alat`, `nama_alat`, `id_jenis`, `tipe`, `stok`) VALUES
(1, 'Gunting Jahit', 1, 'satuan', 6),
(2, 'Scalpel 3L', 5, 'satuan', 1),
(3, 'Pinset Anatomis', 2, 'satuan', 15),
(4, 'Lensa 0 Derajat R.Wolf', 3, 'satuan', 2),
(5, 'Elis Klem', 4, 'satuan', 4),
(8, 'gunting benang', 1, 'satuan', 5),
(11, 'klem mosquito', 1, 'satuan', 15),
(12, 'klem muskito', 4, 'satuan', 10);

-- --------------------------------------------------------

--
-- Table structure for table `detail_pengembalian`
--

CREATE TABLE `detail_pengembalian` (
  `id_detail` int(11) NOT NULL,
  `id_pengembalian` int(11) DEFAULT NULL,
  `id_alat` int(11) DEFAULT NULL,
  `jumlah` int(11) DEFAULT NULL,
  `kondisi` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `detail_pengembalian`
--

INSERT INTO `detail_pengembalian` (`id_detail`, `id_pengembalian`, `id_alat`, `jumlah`, `kondisi`) VALUES
(2, 1, 1, 1, 'kotor'),
(3, 2, 2, 1, 'kotor'),
(4, 2, 4, 1, 'kotor'),
(5, 3, 4, 1, 'kotor'),
(6, 4, 1, 1, 'bersih'),
(7, 5, 1, 1, 'kotor'),
(8, 6, 1, 1, 'kotor'),
(10, 7, 8, 1, 'kotor'),
(11, 8, 1, 2, 'bersih');

-- --------------------------------------------------------

--
-- Table structure for table `detail_permintaan`
--

CREATE TABLE `detail_permintaan` (
  `id_detail` int(11) NOT NULL,
  `id_permintaan` int(11) DEFAULT NULL,
  `id_alat` int(11) DEFAULT NULL,
  `jumlah` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `detail_permintaan`
--

INSERT INTO `detail_permintaan` (`id_detail`, `id_permintaan`, `id_alat`, `jumlah`) VALUES
(5, 5, 1, 1),
(6, 6, 1, 5),
(7, 7, 2, 1),
(8, 8, 2, 1),
(9, 8, 4, 1),
(10, 9, 1, 1),
(11, 10, 1, 1),
(12, 12, 8, 1),
(13, 13, 1, 2);

-- --------------------------------------------------------

--
-- Table structure for table `detail_set`
--

CREATE TABLE `detail_set` (
  `id_detail` int(11) NOT NULL,
  `id_set` int(11) DEFAULT NULL,
  `id_alat` int(11) DEFAULT NULL,
  `jumlah` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `detail_sterilisasi`
--

CREATE TABLE `detail_sterilisasi` (
  `id_detail` int(11) NOT NULL,
  `id_proses` int(11) DEFAULT NULL,
  `id_alat` int(11) DEFAULT NULL,
  `jumlah` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `detail_sterilisasi`
--

INSERT INTO `detail_sterilisasi` (`id_detail`, `id_proses`, `id_alat`, `jumlah`) VALUES
(2, 2, 1, 2),
(3, 3, 1, 2),
(4, 4, 2, 1),
(5, 4, 5, 2),
(6, 4, 8, 2),
(7, 5, 1, 2),
(8, 7, 1, 2);

-- --------------------------------------------------------

--
-- Table structure for table `dokter`
--

CREATE TABLE `dokter` (
  `id_dokter` int(11) NOT NULL,
  `nama_dokter` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `dokter`
--

INSERT INTO `dokter` (`id_dokter`, `nama_dokter`) VALUES
(1, 'Dr. Riyan Sp.PD'),
(2, 'Dr.Louis Sp.JP'),
(3, 'Dr. Raymond Sp.A'),
(4, 'Dr. Steven Sp.B'),
(5, 'Dr. Dani Sp.OG');

-- --------------------------------------------------------

--
-- Table structure for table `jenis_alat`
--

CREATE TABLE `jenis_alat` (
  `id_jenis` int(11) NOT NULL,
  `nama_jenis` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `jenis_alat`
--

INSERT INTO `jenis_alat` (`id_jenis`, `nama_jenis`) VALUES
(1, 'Gunting'),
(2, 'Pinset'),
(3, 'Lensa'),
(4, 'Klem'),
(5, 'Pisau');

-- --------------------------------------------------------

--
-- Table structure for table `mesin`
--

CREATE TABLE `mesin` (
  `id_mesin` int(11) NOT NULL,
  `nama_mesin` varchar(50) DEFAULT NULL,
  `nomor_mesin` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `mesin`
--

INSERT INTO `mesin` (`id_mesin`, `nama_mesin`, `nomor_mesin`) VALUES
(1, 'Autoclave', 'AC-001'),
(2, 'Plasma', 'PL-001');

-- --------------------------------------------------------

--
-- Table structure for table `pasien`
--

CREATE TABLE `pasien` (
  `id_pasien` int(11) NOT NULL,
  `nama_pasien` varchar(100) DEFAULT NULL,
  `no_rekam_medis` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pasien`
--

INSERT INTO `pasien` (`id_pasien`, `nama_pasien`, `no_rekam_medis`) VALUES
(1, 'Kevin Ray Mendoza', 'RM-001'),
(2, 'Samsul', 'RM-002'),
(3, 'Yanto', 'RM-003'),
(4, 'Firza', 'RM-004'),
(5, 'Djubaedah', 'RM-005');

-- --------------------------------------------------------

--
-- Table structure for table `pengembalian_alat`
--

CREATE TABLE `pengembalian_alat` (
  `id_pengembalian` int(11) NOT NULL,
  `id_permintaan` int(11) DEFAULT NULL,
  `id_pengembali` int(11) DEFAULT NULL,
  `tanggal_pengembalian` date DEFAULT NULL,
  `jenis_retur` enum('kotor','bersih') DEFAULT NULL,
  `keterangan` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pengembalian_alat`
--

INSERT INTO `pengembalian_alat` (`id_pengembalian`, `id_permintaan`, `id_pengembali`, `tanggal_pengembalian`, `jenis_retur`, `keterangan`) VALUES
(1, 5, 1, '2026-06-07', 'kotor', '-'),
(2, 8, 1, '2026-06-09', 'kotor', '-'),
(3, 8, 1, '2026-06-08', 'kotor', '-'),
(4, 5, 1, '2026-06-07', 'bersih', '-'),
(5, 10, 1, '2026-08-10', 'kotor', '-'),
(6, 9, 1, '2026-06-08', 'kotor', '-'),
(7, 12, NULL, '2026-07-12', NULL, ''),
(8, 13, NULL, '2026-07-12', NULL, '');

-- --------------------------------------------------------

--
-- Table structure for table `permintaan_alat`
--

CREATE TABLE `permintaan_alat` (
  `id_permintaan` int(11) NOT NULL,
  `id_ruangan` int(11) DEFAULT NULL,
  `id_peminjam` int(11) DEFAULT NULL,
  `id_pasien` int(11) DEFAULT NULL,
  `id_dokter` int(11) DEFAULT NULL,
  `tanggal_permintaan` date DEFAULT NULL,
  `tanggal_pemakaian` date DEFAULT NULL,
  `jenis_transaksi` enum('OT','ruangan') DEFAULT NULL,
  `nama_tindakan` varchar(100) DEFAULT NULL,
  `jenis_tindakan` varchar(100) DEFAULT NULL,
  `keterangan` text DEFAULT NULL,
  `status` enum('aktif','dikembalikan') DEFAULT 'aktif'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `permintaan_alat`
--

INSERT INTO `permintaan_alat` (`id_permintaan`, `id_ruangan`, `id_peminjam`, `id_pasien`, `id_dokter`, `tanggal_permintaan`, `tanggal_pemakaian`, `jenis_transaksi`, `nama_tindakan`, `jenis_tindakan`, `keterangan`, `status`) VALUES
(5, 1, 1, 1, 1, '2026-06-07', '2026-06-08', 'OT', 'LCA', NULL, '-', 'dikembalikan'),
(6, 1, 1, 1, 1, '2026-02-07', '2026-02-08', 'OT', 'LCA', NULL, '-', 'aktif'),
(7, 2, 2, 2, 2, '2026-01-01', '2026-01-02', 'ruangan', 'circumsisi', NULL, '-', 'aktif'),
(8, 1, 1, 1, 1, '2026-06-08', '2026-06-09', 'ruangan', 'HPP', NULL, '-', 'dikembalikan'),
(9, 1, 1, 1, 1, '2026-06-07', '2026-06-08', 'OT', 'LCA', NULL, '-', 'dikembalikan'),
(10, 1, 1, 1, 1, '2026-08-09', '2026-08-10', 'OT', 'LCA', NULL, '-', 'dikembalikan'),
(11, 1, 2, 5, 5, '2026-07-12', '2026-07-12', 'OT', 'Apendiks', NULL, '', 'aktif'),
(12, 1, 1, 4, 5, '2026-07-12', '2026-07-12', 'OT', 'Operasi Apendiks', NULL, '', 'aktif'),
(13, 1, 1, 5, 5, '2026-07-12', '2026-07-12', 'OT', 'Operasi Apendiktomi', NULL, '', 'aktif');

-- --------------------------------------------------------

--
-- Table structure for table `petugas_cssd`
--

CREATE TABLE `petugas_cssd` (
  `id_petugas` int(11) NOT NULL,
  `nama_petugas` varchar(100) DEFAULT NULL,
  `shift` enum('pagi','siang','oncall') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `petugas_cssd`
--

INSERT INTO `petugas_cssd` (`id_petugas`, `nama_petugas`, `shift`) VALUES
(1, 'Syarif', 'pagi'),
(2, 'Sultan', 'pagi'),
(3, 'Bagas', 'siang'),
(4, 'Cecep', 'siang'),
(5, 'Oki', 'oncall');

-- --------------------------------------------------------

--
-- Table structure for table `petugas_ruangan`
--

CREATE TABLE `petugas_ruangan` (
  `id_petugas` int(11) NOT NULL,
  `nama_petugas` varchar(100) DEFAULT NULL,
  `jabatan` varchar(50) DEFAULT NULL,
  `id_ruangan` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `petugas_ruangan`
--

INSERT INTO `petugas_ruangan` (`id_petugas`, `nama_petugas`, `jabatan`, `id_ruangan`) VALUES
(1, 'Adnan', 'Perawat', 5),
(2, 'Beckam Putra', 'Perawat', 2),
(3, 'Bagas Adi', 'Perawat', 3),
(4, 'Ali', 'Perawat', 1),
(5, 'Silvia', 'Perawat', 4);

-- --------------------------------------------------------

--
-- Table structure for table `proses_sterilisasi`
--

CREATE TABLE `proses_sterilisasi` (
  `id_proses` int(11) NOT NULL,
  `tanggal` date DEFAULT NULL,
  `nama_mesin` varchar(50) DEFAULT NULL,
  `nomor_mesin` varchar(20) DEFAULT NULL,
  `jumlah_siklus` int(11) DEFAULT NULL,
  `nomor_siklus` int(11) DEFAULT NULL,
  `id_petugas` int(11) DEFAULT NULL,
  `keterangan` text DEFAULT NULL,
  `id_mesin` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `proses_sterilisasi`
--

INSERT INTO `proses_sterilisasi` (`id_proses`, `tanggal`, `nama_mesin`, `nomor_mesin`, `jumlah_siklus`, `nomor_siklus`, `id_petugas`, `keterangan`, `id_mesin`) VALUES
(2, '2026-06-07', 'Autoclave', 'AC-001', 1, 1, 1, '-', 1),
(3, '2026-06-07', 'Plasma', 'PL-001', 1, 1, 2, '-', 2),
(4, '2026-06-08', 'Autoclave', 'AC-001', 1, 2, 5, '-', 1),
(5, '2026-06-06', 'Autoclave', 'AC-001', 1, 1, 1, '-', 1),
(6, '2026-07-12', NULL, NULL, 1, 1, 3, '', 1),
(7, '2026-07-12', NULL, NULL, 1, 1, 3, 'sterilisasi rutin', 1);

-- --------------------------------------------------------

--
-- Table structure for table `ruangan`
--

CREATE TABLE `ruangan` (
  `id_ruangan` int(11) NOT NULL,
  `nama_ruangan` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ruangan`
--

INSERT INTO `ruangan` (`id_ruangan`, `nama_ruangan`) VALUES
(1, 'OK'),
(2, 'IGD'),
(3, 'ICU'),
(4, 'VK'),
(5, 'CATHLAB');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id_user` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nama_lengkap` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id_user`, `username`, `password`, `nama_lengkap`) VALUES
(1, 'admin', 'admin', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id_user` int(11) NOT NULL,
  `nama` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','petugas') NOT NULL DEFAULT 'petugas',
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id_user`, `nama`, `username`, `password`, `role`, `created_at`) VALUES
(1, 'Administrator', 'admin', 'admin123', 'admin', '2026-05-14 13:00:49'),
(2, 'Petugas CSSD', 'petugas', 'petugas123', 'petugas', '2026-05-14 13:00:49');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `alat`
--
ALTER TABLE `alat`
  ADD PRIMARY KEY (`id_alat`),
  ADD KEY `fk_alat_jenis` (`id_jenis`);

--
-- Indexes for table `detail_pengembalian`
--
ALTER TABLE `detail_pengembalian`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `fk_detail_kembali` (`id_pengembalian`),
  ADD KEY `fk_detail_kembali_alat` (`id_alat`);

--
-- Indexes for table `detail_permintaan`
--
ALTER TABLE `detail_permintaan`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `fk_detail_perm` (`id_permintaan`),
  ADD KEY `fk_detail_alat` (`id_alat`);

--
-- Indexes for table `detail_set`
--
ALTER TABLE `detail_set`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `fk_set` (`id_set`),
  ADD KEY `fk_alat_set` (`id_alat`);

--
-- Indexes for table `detail_sterilisasi`
--
ALTER TABLE `detail_sterilisasi`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `id_proses` (`id_proses`),
  ADD KEY `id_alat` (`id_alat`);

--
-- Indexes for table `dokter`
--
ALTER TABLE `dokter`
  ADD PRIMARY KEY (`id_dokter`);

--
-- Indexes for table `jenis_alat`
--
ALTER TABLE `jenis_alat`
  ADD PRIMARY KEY (`id_jenis`);

--
-- Indexes for table `mesin`
--
ALTER TABLE `mesin`
  ADD PRIMARY KEY (`id_mesin`);

--
-- Indexes for table `pasien`
--
ALTER TABLE `pasien`
  ADD PRIMARY KEY (`id_pasien`);

--
-- Indexes for table `pengembalian_alat`
--
ALTER TABLE `pengembalian_alat`
  ADD PRIMARY KEY (`id_pengembalian`),
  ADD KEY `fk_kembali_perm` (`id_permintaan`),
  ADD KEY `fk_kembali_petugas` (`id_pengembali`);

--
-- Indexes for table `permintaan_alat`
--
ALTER TABLE `permintaan_alat`
  ADD PRIMARY KEY (`id_permintaan`),
  ADD KEY `fk_perm_ruangan` (`id_ruangan`),
  ADD KEY `fk_perm_petugas` (`id_peminjam`),
  ADD KEY `fk_perm_pasien` (`id_pasien`),
  ADD KEY `fk_perm_dokter` (`id_dokter`);

--
-- Indexes for table `petugas_cssd`
--
ALTER TABLE `petugas_cssd`
  ADD PRIMARY KEY (`id_petugas`);

--
-- Indexes for table `petugas_ruangan`
--
ALTER TABLE `petugas_ruangan`
  ADD PRIMARY KEY (`id_petugas`),
  ADD KEY `fk_petugas_ruangan` (`id_ruangan`);

--
-- Indexes for table `proses_sterilisasi`
--
ALTER TABLE `proses_sterilisasi`
  ADD PRIMARY KEY (`id_proses`),
  ADD KEY `fk_steril_petugas` (`id_petugas`),
  ADD KEY `fk_steril_mesin` (`id_mesin`);

--
-- Indexes for table `ruangan`
--
ALTER TABLE `ruangan`
  ADD PRIMARY KEY (`id_ruangan`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id_user`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `alat`
--
ALTER TABLE `alat`
  MODIFY `id_alat` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `detail_pengembalian`
--
ALTER TABLE `detail_pengembalian`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `detail_permintaan`
--
ALTER TABLE `detail_permintaan`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `detail_set`
--
ALTER TABLE `detail_set`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `detail_sterilisasi`
--
ALTER TABLE `detail_sterilisasi`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `dokter`
--
ALTER TABLE `dokter`
  MODIFY `id_dokter` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `jenis_alat`
--
ALTER TABLE `jenis_alat`
  MODIFY `id_jenis` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `mesin`
--
ALTER TABLE `mesin`
  MODIFY `id_mesin` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `pasien`
--
ALTER TABLE `pasien`
  MODIFY `id_pasien` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `pengembalian_alat`
--
ALTER TABLE `pengembalian_alat`
  MODIFY `id_pengembalian` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `permintaan_alat`
--
ALTER TABLE `permintaan_alat`
  MODIFY `id_permintaan` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `petugas_cssd`
--
ALTER TABLE `petugas_cssd`
  MODIFY `id_petugas` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `petugas_ruangan`
--
ALTER TABLE `petugas_ruangan`
  MODIFY `id_petugas` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `proses_sterilisasi`
--
ALTER TABLE `proses_sterilisasi`
  MODIFY `id_proses` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `ruangan`
--
ALTER TABLE `ruangan`
  MODIFY `id_ruangan` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `alat`
--
ALTER TABLE `alat`
  ADD CONSTRAINT `alat_ibfk_1` FOREIGN KEY (`id_jenis`) REFERENCES `jenis_alat` (`id_jenis`),
  ADD CONSTRAINT `fk_alat_jenis` FOREIGN KEY (`id_jenis`) REFERENCES `jenis_alat` (`id_jenis`);

--
-- Constraints for table `detail_pengembalian`
--
ALTER TABLE `detail_pengembalian`
  ADD CONSTRAINT `detail_pengembalian_ibfk_1` FOREIGN KEY (`id_pengembalian`) REFERENCES `pengembalian_alat` (`id_pengembalian`),
  ADD CONSTRAINT `detail_pengembalian_ibfk_2` FOREIGN KEY (`id_alat`) REFERENCES `alat` (`id_alat`),
  ADD CONSTRAINT `fk_detail_kembali` FOREIGN KEY (`id_pengembalian`) REFERENCES `pengembalian_alat` (`id_pengembalian`),
  ADD CONSTRAINT `fk_detail_kembali_alat` FOREIGN KEY (`id_alat`) REFERENCES `alat` (`id_alat`);

--
-- Constraints for table `detail_permintaan`
--
ALTER TABLE `detail_permintaan`
  ADD CONSTRAINT `detail_permintaan_ibfk_1` FOREIGN KEY (`id_permintaan`) REFERENCES `permintaan_alat` (`id_permintaan`),
  ADD CONSTRAINT `detail_permintaan_ibfk_2` FOREIGN KEY (`id_alat`) REFERENCES `alat` (`id_alat`),
  ADD CONSTRAINT `fk_detail_alat` FOREIGN KEY (`id_alat`) REFERENCES `alat` (`id_alat`),
  ADD CONSTRAINT `fk_detail_perm` FOREIGN KEY (`id_permintaan`) REFERENCES `permintaan_alat` (`id_permintaan`);

--
-- Constraints for table `detail_set`
--
ALTER TABLE `detail_set`
  ADD CONSTRAINT `detail_set_ibfk_1` FOREIGN KEY (`id_set`) REFERENCES `alat` (`id_alat`),
  ADD CONSTRAINT `detail_set_ibfk_2` FOREIGN KEY (`id_alat`) REFERENCES `alat` (`id_alat`),
  ADD CONSTRAINT `fk_alat_set` FOREIGN KEY (`id_alat`) REFERENCES `alat` (`id_alat`),
  ADD CONSTRAINT `fk_set` FOREIGN KEY (`id_set`) REFERENCES `alat` (`id_alat`);

--
-- Constraints for table `detail_sterilisasi`
--
ALTER TABLE `detail_sterilisasi`
  ADD CONSTRAINT `detail_sterilisasi_ibfk_1` FOREIGN KEY (`id_proses`) REFERENCES `proses_sterilisasi` (`id_proses`),
  ADD CONSTRAINT `detail_sterilisasi_ibfk_2` FOREIGN KEY (`id_alat`) REFERENCES `alat` (`id_alat`);

--
-- Constraints for table `pengembalian_alat`
--
ALTER TABLE `pengembalian_alat`
  ADD CONSTRAINT `fk_kembali_perm` FOREIGN KEY (`id_permintaan`) REFERENCES `permintaan_alat` (`id_permintaan`),
  ADD CONSTRAINT `fk_kembali_petugas` FOREIGN KEY (`id_pengembali`) REFERENCES `petugas_ruangan` (`id_petugas`),
  ADD CONSTRAINT `pengembalian_alat_ibfk_1` FOREIGN KEY (`id_permintaan`) REFERENCES `permintaan_alat` (`id_permintaan`),
  ADD CONSTRAINT `pengembalian_alat_ibfk_2` FOREIGN KEY (`id_pengembali`) REFERENCES `petugas_ruangan` (`id_petugas`);

--
-- Constraints for table `permintaan_alat`
--
ALTER TABLE `permintaan_alat`
  ADD CONSTRAINT `fk_perm_dokter` FOREIGN KEY (`id_dokter`) REFERENCES `dokter` (`id_dokter`),
  ADD CONSTRAINT `fk_perm_pasien` FOREIGN KEY (`id_pasien`) REFERENCES `pasien` (`id_pasien`),
  ADD CONSTRAINT `fk_perm_petugas` FOREIGN KEY (`id_peminjam`) REFERENCES `petugas_ruangan` (`id_petugas`),
  ADD CONSTRAINT `fk_perm_ruangan` FOREIGN KEY (`id_ruangan`) REFERENCES `ruangan` (`id_ruangan`),
  ADD CONSTRAINT `permintaan_alat_ibfk_1` FOREIGN KEY (`id_ruangan`) REFERENCES `ruangan` (`id_ruangan`),
  ADD CONSTRAINT `permintaan_alat_ibfk_2` FOREIGN KEY (`id_peminjam`) REFERENCES `petugas_ruangan` (`id_petugas`),
  ADD CONSTRAINT `permintaan_alat_ibfk_3` FOREIGN KEY (`id_pasien`) REFERENCES `pasien` (`id_pasien`),
  ADD CONSTRAINT `permintaan_alat_ibfk_4` FOREIGN KEY (`id_dokter`) REFERENCES `dokter` (`id_dokter`);

--
-- Constraints for table `petugas_ruangan`
--
ALTER TABLE `petugas_ruangan`
  ADD CONSTRAINT `fk_petugas_ruangan` FOREIGN KEY (`id_ruangan`) REFERENCES `ruangan` (`id_ruangan`),
  ADD CONSTRAINT `petugas_ruangan_ibfk_1` FOREIGN KEY (`id_ruangan`) REFERENCES `ruangan` (`id_ruangan`);

--
-- Constraints for table `proses_sterilisasi`
--
ALTER TABLE `proses_sterilisasi`
  ADD CONSTRAINT `fk_steril_mesin` FOREIGN KEY (`id_mesin`) REFERENCES `mesin` (`id_mesin`),
  ADD CONSTRAINT `fk_steril_petugas` FOREIGN KEY (`id_petugas`) REFERENCES `petugas_cssd` (`id_petugas`),
  ADD CONSTRAINT `proses_sterilisasi_ibfk_1` FOREIGN KEY (`id_petugas`) REFERENCES `petugas_cssd` (`id_petugas`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
