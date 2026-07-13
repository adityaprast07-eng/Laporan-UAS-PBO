
package cssd;

import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

// ══════════════════════════════════════════════════════════════════════
// CRUD DIALOG — Fixed dengan field alat & jumlah
// ══════════════════════════════════════════════════════════════════════
class CRUDDialog extends JDialog {

    private final String tableName;
    private final Map<String, Object> existingRow;
    private final Map<String, JComponent> fields = new LinkedHashMap<>();
    private boolean saved = false;

    // Untuk multi-alat (permintaan, pengembalian, sterilisasi)
    private java.util.List<int[]> daftarAlat = new ArrayList<>(); // [idAlat, jumlah]
    private DefaultTableModel alatModel;
    private JTextArea taInfoDipinjam; // FIX: info alat yang dipinjam pada permintaan terkait

    public CRUDDialog(JFrame parent, String tableName, Map<String, Object> row) {
        super(parent, (row == null ? "Tambah " : "Edit ") + getTitle(tableName), true);
        this.tableName   = tableName;
        this.existingRow = row;
        setSize(520, 620);
        setResizable(true);
        setLocationRelativeTo(parent);
        buildUI();
    }

    private static String getTitle(String t) {
        return Map.ofEntries(
            Map.entry("ruangan","Ruangan"),
            Map.entry("jenis_alat","Jenis Alat"),
            Map.entry("alat","Alat"),
            Map.entry("mesin","Mesin"),
            Map.entry("dokter","Dokter"),
            Map.entry("pasien","Pasien"),
            Map.entry("petugas_cssd","Petugas CSSD"),
            Map.entry("petugas_ruangan","Petugas Ruangan"),
            Map.entry("permintaan_alat","Permintaan Alat"),
            Map.entry("pengembalian_alat","Pengembalian Alat"),
            Map.entry("proses_sterilisasi","Proses Sterilisasi")
        ).getOrDefault(t, t);
    }

    private boolean needsAlat() {
        return tableName.equals("permintaan_alat") ||
               tableName.equals("pengembalian_alat") ||
               tableName.equals("proses_sterilisasi");
    }

    // FIX: "kondisi" hanya kolom di detail_pengembalian (per alat), bukan di tabel
    // pengembalian_alat itu sendiri. Field ini harus dikecualikan saat INSERT/UPDATE
    // ke tabel utama, walaupun tetap ada di form untuk dipakai di saveDetailAlat().
    private Set<String> nonColumnFields() {
        if (tableName.equals("pengembalian_alat")) return Set.of("kondisi");
        return Collections.emptySet();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LoginFrame.WHITE);
        setContentPane(root);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setBackground(LoginFrame.WHITE);
        buildForm(form);
        if (existingRow != null) {
            fillValues();
            loadExistingAlatIfNeeded(); // FIX: muat ulang daftar alat lama saat edit
        }

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        root.add(scroll, BorderLayout.CENTER);

        // Footer tombol
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        foot.setBackground(LoginFrame.WHITE);
        foot.setBorder(new MatteBorder(1, 0, 0, 0, LoginFrame.BORDER));

        JButton btnBatal = new JButton("Batal");
        btnBatal.setFont(LoginFrame.BOLD13);
        btnBatal.addActionListener(e -> dispose());

        JButton btnSimpan = new JButton("💾  Simpan");
        btnSimpan.setFont(LoginFrame.BOLD13);
        btnSimpan.setBackground(LoginFrame.NAVY);
        btnSimpan.setForeground(LoginFrame.WHITE);
        btnSimpan.setOpaque(true);
        btnSimpan.setBorderPainted(false);
        btnSimpan.setFocusPainted(false);
        btnSimpan.addActionListener(e -> saveData(btnSimpan));

        foot.add(btnBatal);
        foot.add(btnSimpan);
        root.add(foot, BorderLayout.SOUTH);
    }

    // ── BUILD FORM PER TABEL ──────────────────────────────────────────
    private void buildForm(JPanel form) {
        switch (tableName) {
            case "ruangan"    -> addField(form, "nama_ruangan",  "Nama Ruangan *",  "text");
            case "jenis_alat" -> addField(form, "nama_jenis",    "Nama Jenis *",    "text");
            case "dokter"     -> addField(form, "nama_dokter",   "Nama Dokter *",   "text");
            case "alat"       -> {
                addField(form, "nama_alat", "Nama Alat *",  "text");
                addField(form, "id_jenis",  "Jenis Alat",   "select:jenis_alat:id_jenis:nama_jenis");
                addField(form, "tipe",      "Tipe",         "select:satuan,set,linen");
                addField(form, "stok",      "Stok",         "number");
            }
            case "mesin" -> {
                addField(form, "nama_mesin",  "Nama Mesin *", "text");
                addField(form, "nomor_mesin", "Nomor Mesin",  "text");
            }
            case "pasien" -> {
                addField(form, "nama_pasien",    "Nama Pasien *",   "text");
                addField(form, "no_rekam_medis", "No. Rekam Medis", "text");
            }
            case "petugas_cssd" -> {
                addField(form, "nama_petugas", "Nama Petugas *", "text");
                addField(form, "shift",        "Shift",          "select:pagi,siang,oncall");
            }
            case "petugas_ruangan" -> {
                addField(form, "nama_petugas", "Nama Petugas *", "text");
                addField(form, "jabatan",      "Jabatan",        "text");
                addField(form, "id_ruangan",   "Ruangan",        "select:ruangan:id_ruangan:nama_ruangan");
            }
            case "permintaan_alat" -> {
                addField(form, "tanggal_permintaan", "Tgl Permintaan *", "date");
                addField(form, "tanggal_pemakaian",  "Tgl Pemakaian",    "date");
                addField(form, "id_ruangan",         "Ruangan *",        "select:ruangan:id_ruangan:nama_ruangan");
                addField(form, "jenis_transaksi",    "Jenis Transaksi",  "select:OT,ruangan");
                addField(form, "id_peminjam",        "Peminjam",         "select:petugas_ruangan:id_petugas:nama_petugas");
                addField(form, "id_pasien",          "Pasien",           "select:pasien:id_pasien:nama_pasien");
                addField(form, "id_dokter",          "Dokter",           "select:dokter:id_dokter:nama_dokter");
                addField(form, "nama_tindakan",      "Nama Tindakan",    "text");
                addField(form, "keterangan",         "Keterangan",       "textarea");
                addAlatSection(form, "Alat yang Dipinjam");
            }
            case "pengembalian_alat" -> {
                addField(form, "id_permintaan",         "Permintaan *",       "select:permintaan_alat:id_permintaan:nama_tindakan");
                addField(form, "tanggal_pengembalian",  "Tgl Pengembalian *", "date");
                addField(form, "kondisi",               "Kondisi",            "select:kotor,bersih,rusak");
                addField(form, "keterangan",            "Keterangan",         "textarea");
                addAlatSection(form, "Alat yang Dikembalikan");
            }
            case "proses_sterilisasi" -> {
                addField(form, "tanggal",       "Tanggal *",    "date");
                addField(form, "id_mesin",      "Mesin",        "select:mesin:id_mesin:nama_mesin");
                addField(form, "nomor_siklus",  "No. Siklus",   "number");
                addField(form, "jumlah_siklus", "Jml Siklus",   "number");
                addField(form, "id_petugas",    "Petugas CSSD", "select:petugas_cssd:id_petugas:nama_petugas");
                addField(form, "keterangan",    "Keterangan",   "textarea");
                addAlatSection(form, "Alat yang Disterilisasi");
            }
        }
    }

    // ── SECTION TAMBAH ALAT ──────────────────────────────────────────
    private void addAlatSection(JPanel form, String title) {
        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(LoginFrame.BORDER);
        form.add(Box.createVerticalStrut(10));
        form.add(sep);
        form.add(Box.createVerticalStrut(8));

        // Label judul
        JLabel lblTitle = new JLabel("📦 " + title);
        lblTitle.setFont(LoginFrame.BOLD13);
        lblTitle.setForeground(LoginFrame.NAVY);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblTitle);
        form.add(Box.createVerticalStrut(8));

        // FIX: khusus pengembalian, tampilkan alat yang dulu dipinjam pada permintaan terkait
        if (tableName.equals("pengembalian_alat")) {
            addInfoAlatDipinjam(form);
        }

        // Tabel alat yang dipilih
        alatModel = new DefaultTableModel(new String[]{"ID Alat", "Nama Alat", "Jumlah"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblAlat = new JTable(alatModel);
        tblAlat.setFont(LoginFrame.PLAIN13);
        tblAlat.setRowHeight(26);
        tblAlat.getTableHeader().setFont(LoginFrame.BOLD13);
        tblAlat.setGridColor(new Color(0xEDF2F7));

        JScrollPane scrollAlat = new JScrollPane(tblAlat);
        scrollAlat.setAlignmentX(LEFT_ALIGNMENT);
        scrollAlat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollAlat.setPreferredSize(new Dimension(450, 100));
        form.add(scrollAlat);
        form.add(Box.createVerticalStrut(8));

        // Panel input alat
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        inputPanel.setBackground(LoginFrame.WHITE);
        inputPanel.setAlignmentX(LEFT_ALIGNMENT);
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Dropdown pilih alat
        JComboBox<String[]> cbAlat = loadCombo("alat", "id_alat", "nama_alat");
        cbAlat.setPreferredSize(new Dimension(200, 30));
        cbAlat.setFont(LoginFrame.PLAIN13);

        // Input jumlah
        JSpinner spJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spJumlah.setPreferredSize(new Dimension(70, 30));
        spJumlah.setFont(LoginFrame.PLAIN13);

        // Tombol tambah alat
        JButton btnTambahAlat = new JButton("+ Tambah");
        btnTambahAlat.setFont(LoginFrame.BOLD13);
        btnTambahAlat.setBackground(new Color(0x48BB78));
        btnTambahAlat.setForeground(LoginFrame.WHITE);
        btnTambahAlat.setOpaque(true);
        btnTambahAlat.setBorderPainted(false);
        btnTambahAlat.setFocusPainted(false);

        // Tombol hapus alat dari tabel
        JButton btnHapusAlat = new JButton("✕ Hapus");
        btnHapusAlat.setFont(LoginFrame.BOLD13);
        btnHapusAlat.setBackground(new Color(0xFC8181));
        btnHapusAlat.setForeground(LoginFrame.WHITE);
        btnHapusAlat.setOpaque(true);
        btnHapusAlat.setBorderPainted(false);
        btnHapusAlat.setFocusPainted(false);

        btnTambahAlat.addActionListener(e -> {
            String[] selected = (String[]) cbAlat.getSelectedItem();
            if (selected == null || selected[0].isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pilih alat terlebih dahulu!");
                return;
            }
            int idAlat  = Integer.parseInt(selected[0]);
            String nama = selected[1];
            int jumlah  = (int) spJumlah.getValue();

            // Cek duplikat
            for (int[] a : daftarAlat) {
                if (a[0] == idAlat) {
                    JOptionPane.showMessageDialog(this, "Alat sudah ditambahkan!");
                    return;
                }
            }

            daftarAlat.add(new int[]{idAlat, jumlah});
            alatModel.addRow(new Object[]{idAlat, nama, jumlah});
        });

        btnHapusAlat.addActionListener(e -> {
            int row = tblAlat.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih alat yang ingin dihapus dari daftar!");
                return;
            }
            daftarAlat.remove(row);
            alatModel.removeRow(row);
        });

        inputPanel.add(new JLabel("Alat:"));
        inputPanel.add(cbAlat);
        inputPanel.add(new JLabel("Jumlah:"));
        inputPanel.add(spJumlah);
        inputPanel.add(btnTambahAlat);
        inputPanel.add(btnHapusAlat);
        form.add(inputPanel);
        form.add(Box.createVerticalStrut(10));
    }

    // FIX: tampilkan daftar alat yang dipinjam pada permintaan terkait, dan
    // otomatis update tiap kali dropdown "Permintaan" diganti.
    private void addInfoAlatDipinjam(JPanel form) {
        JLabel lbl = new JLabel("ℹ Alat yang dipinjam pada permintaan ini:");
        lbl.setFont(LoginFrame.PLAIN11);
        lbl.setForeground(LoginFrame.MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lbl);
        form.add(Box.createVerticalStrut(4));

        taInfoDipinjam = new JTextArea(2, 20);
        taInfoDipinjam.setEditable(false);
        taInfoDipinjam.setLineWrap(true);
        taInfoDipinjam.setWrapStyleWord(true);
        taInfoDipinjam.setFont(LoginFrame.PLAIN11);
        taInfoDipinjam.setBackground(new Color(0xF7FAFC));
        taInfoDipinjam.setText("Pilih permintaan di atas untuk melihat daftar alat yang dipinjam.");
        JScrollPane sp = new JScrollPane(taInfoDipinjam);
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        form.add(sp);
        form.add(Box.createVerticalStrut(10));

        JComponent permComp = fields.get("id_permintaan");
        if (permComp instanceof JComboBox<?> cbPermintaan) {
            cbPermintaan.addActionListener(e -> updateInfoAlatDipinjam(cbPermintaan));
            // Jika sedang edit dan field sudah terisi, langsung tampilkan begitu dialog dibuka
            SwingUtilities.invokeLater(() -> updateInfoAlatDipinjam(cbPermintaan));
        }
    }

    private void updateInfoAlatDipinjam(JComboBox<?> cbPermintaan) {
        if (taInfoDipinjam == null) return;
        Object selected = cbPermintaan.getSelectedItem();
        if (!(selected instanceof String[] item) || item[0].isEmpty()) {
            taInfoDipinjam.setText("Pilih permintaan di atas untuk melihat daftar alat yang dipinjam.");
            return;
        }

        int idPermintaan;
        try {
            idPermintaan = Integer.parseInt(item[0]);
        } catch (NumberFormatException ex) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sql = "SELECT a.nama_alat, dp.jumlah FROM detail_permintaan dp " +
                     "JOIN alat a ON dp.id_alat = a.id_alat WHERE dp.id_permintaan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPermintaan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(rs.getString(1)).append(" (x").append(rs.getInt(2)).append(")");
                }
            }
        } catch (SQLException ex) {
            taInfoDipinjam.setText("Gagal memuat data alat: " + ex.getMessage());
            return;
        }
        taInfoDipinjam.setText(sb.length() > 0 ? sb.toString() : "Tidak ada data alat untuk permintaan ini.");
    }

    // ── ADD FIELD ─────────────────────────────────────────────────────
    private void addField(JPanel form, String key, String label, String type) {
        JPanel fg = new JPanel();
        fg.setLayout(new BoxLayout(fg, BoxLayout.Y_AXIS));
        fg.setBackground(LoginFrame.WHITE);
        fg.setAlignmentX(LEFT_ALIGNMENT);
        fg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));

        JLabel lbl = new JLabel(label);
        lbl.setFont(LoginFrame.BOLD13);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        fg.add(lbl);
        fg.add(Box.createVerticalStrut(4));

        if (type.equals("textarea")) {
            JTextArea ta = new JTextArea(3, 20);
            ta.setFont(LoginFrame.PLAIN13);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(ta);
            sp.setAlignmentX(LEFT_ALIGNMENT);
            sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            fg.add(sp);
            fields.put(key, ta);
        } else if (type.equals("number")) {
            JSpinner sp = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
            sp.setFont(LoginFrame.PLAIN13);
            sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            sp.setAlignmentX(LEFT_ALIGNMENT);
            fg.add(sp);
            fields.put(key, sp);
        } else if (type.startsWith("select:")) {
            String rest = type.substring(7);
            JComboBox<String[]> cb;
            if (rest.contains(",")) {
                String[] opts = rest.split(",");
                String[][] items = new String[opts.length + 1][2];
                items[0] = new String[]{"", "-- Pilih --"};
                for (int i = 0; i < opts.length; i++) items[i+1] = new String[]{opts[i], opts[i]};
                cb = makeCombo(items);
            } else {
                String[] parts = rest.split(":");
                cb = loadCombo(parts[0], parts[1], parts[2]);
            }
            cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            cb.setAlignmentX(LEFT_ALIGNMENT);
            fg.add(cb);
            fields.put(key, cb);
        } else if (type.equals("date")) {
            JTextField tf = new JTextField(java.time.LocalDate.now().toString());
            tf.setFont(LoginFrame.PLAIN13);
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            tf.setAlignmentX(LEFT_ALIGNMENT);
            JLabel hint = new JLabel("Format: YYYY-MM-DD");
            hint.setFont(LoginFrame.PLAIN11);
            hint.setForeground(LoginFrame.MUTED);
            fg.add(tf);
            fg.add(hint);
            fields.put(key, tf);
        } else {
            JTextField tf = new JTextField();
            tf.setFont(LoginFrame.PLAIN13);
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            tf.setAlignmentX(LEFT_ALIGNMENT);
            fg.add(tf);
            fields.put(key, tf);
        }

        form.add(fg);
        form.add(Box.createVerticalStrut(10));
    }

    @SuppressWarnings("unchecked")
    private JComboBox<String[]> makeCombo(String[][] items) {
        JComboBox<String[]> cb = new JComboBox<>(items);
        cb.setFont(LoginFrame.PLAIN13);
        cb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                String[] arr = (String[]) v;
                return super.getListCellRendererComponent(l, arr != null ? arr[1] : "", i, sel, foc);
            }
        });
        return cb;
    }

    private JComboBox<String[]> loadCombo(String table, String pkCol, String labelCol) {
        java.util.List<String[]> items = new ArrayList<>();
        items.add(new String[]{"", "-- Pilih --"});
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT " + pkCol + ", " + labelCol + " FROM " + table + " ORDER BY " + labelCol)) {
            while (rs.next()) items.add(new String[]{rs.getString(1), rs.getString(2)});
        } catch (Exception ignored) {}
        return makeCombo(items.toArray(new String[0][]));
    }

    // ── FILL VALUES SAAT EDIT ─────────────────────────────────────────
    private void fillValues() {
        for (Map.Entry<String, JComponent> e : fields.entrySet()) {
            Object val = existingRow.get(e.getKey());
            if (val == null) continue;
            String sv = val.toString();
            JComponent comp = e.getValue();
            if (comp instanceof JTextField tf) tf.setText(sv);
            else if (comp instanceof JTextArea ta) ta.setText(sv);
            else if (comp instanceof JSpinner sp) {
                try { sp.setValue(Integer.parseInt(sv)); } catch (Exception ignored) {}
            } else if (comp instanceof JComboBox<?> cb) {
                for (int i = 0; i < cb.getItemCount(); i++) {
                    String[] item = (String[]) cb.getItemAt(i);
                    if (item[0].equals(sv)) { cb.setSelectedIndex(i); break; }
                }
            }
        }
    }

    // FIX: nama tabel detail per jenis transaksi
    private String getDetailTable(String t) {
        return switch (t) {
            case "permintaan_alat"    -> "detail_permintaan";
            case "pengembalian_alat"  -> "detail_pengembalian";
            case "proses_sterilisasi" -> "detail_sterilisasi";
            default -> null;
        };
    }

    // FIX: muat ulang daftar alat lama dari tabel detail saat dialog dibuka untuk edit,
    // supaya tidak terlihat kosong dan tidak ketimpa/hilang begitu saja saat disimpan ulang.
    private void loadExistingAlatIfNeeded() {
        if (!needsAlat() || alatModel == null) return;
        String detailTable = getDetailTable(tableName);
        String parentCol   = getPK(tableName);
        Object parentVal   = existingRow.get(parentCol);
        if (detailTable == null || parentVal == null) return;

        String sql = "SELECT da.id_alat, a.nama_alat, da.jumlah FROM " + detailTable +
                     " da JOIN alat a ON da.id_alat = a.id_alat WHERE da." + parentCol + " = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, parentVal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idAlat   = rs.getInt("id_alat");
                    String nama  = rs.getString("nama_alat");
                    int jumlah   = rs.getInt("jumlah");
                    daftarAlat.add(new int[]{idAlat, jumlah});
                    alatModel.addRow(new Object[]{idAlat, nama, jumlah});
                }
            }
        } catch (SQLException ex) {
            // Tabel detail mungkin belum ada / struktur beda — biarkan dialog tetap terbuka
        }
    }

    // ── SAVE DATA ─────────────────────────────────────────────────────
    private void saveData(JButton btnSimpan) {
        // Validasi alat untuk transaksi
        if (needsAlat() && daftarAlat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Minimal 1 alat harus ditambahkan!");
            return;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        for (Map.Entry<String, JComponent> e : fields.entrySet()) {
            JComponent comp = e.getValue();
            Object val;
            if (comp instanceof JTextField tf) val = tf.getText().trim();
            else if (comp instanceof JTextArea ta) val = ta.getText().trim();
            else if (comp instanceof JSpinner sp) val = sp.getValue();
            else if (comp instanceof JComboBox<?> cb) {
                String[] item = (String[]) cb.getSelectedItem();
                val = (item != null && !item[0].isEmpty()) ? item[0] : null;
            } else val = null;
            data.put(e.getKey(), val);
        }

        // FIX: pisahkan field yang memang bukan kolom tabel utama (mis. "kondisi"
        // untuk pengembalian_alat) supaya tidak ikut di-INSERT/UPDATE ke tabel utama
        Set<String> skipCols = nonColumnFields();
        Map<String, Object> mainTableData = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            if (!skipCols.contains(e.getKey())) mainTableData.put(e.getKey(), e.getValue());
        }

        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        String pk = getPK(tableName);
                        int parentId;

                        if (existingRow == null) {
                            // INSERT
                            String cols  = String.join(",", mainTableData.keySet());
                            String marks = String.join(",", Collections.nCopies(mainTableData.size(), "?"));
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "INSERT INTO " + tableName + " (" + cols + ") VALUES (" + marks + ")",
                                    Statement.RETURN_GENERATED_KEYS)) {
                                int i = 1;
                                for (Object v : mainTableData.values()) ps.setObject(i++, v);
                                ps.executeUpdate();
                                try (ResultSet gk = ps.getGeneratedKeys()) {
                                    parentId = gk.next() ? gk.getInt(1) : 0;
                                }
                            }
                        } else {
                            // UPDATE
                            String set = String.join(",", mainTableData.keySet().stream().map(k -> k + "=?").toList());
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "UPDATE " + tableName + " SET " + set + " WHERE " + pk + "=?")) {
                                int i = 1;
                                for (Object v : mainTableData.values()) ps.setObject(i++, v);
                                ps.setObject(i, existingRow.get(pk));
                                ps.executeUpdate();
                            }
                            // FIX: id untuk baris yang diedit diambil dari existingRow, bukan dibiarkan 0
                            parentId = Integer.parseInt(String.valueOf(existingRow.get(pk)));
                        }

                        // FIX: detail alat sekarang disimpan baik untuk INSERT maupun UPDATE
                        if (needsAlat() && parentId > 0) {
                            if (existingRow != null) {
                                revertAndDeleteDetail(conn, parentId); // balikkan stok lama & hapus baris lama
                            }
                            saveDetailAlat(conn, parentId);
                        }

                        conn.commit();
                    } catch (SQLException ex) {
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    saved = true;
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CRUDDialog.this,
                        "❌ Gagal menyimpan: " + ex.getMessage());
                } finally {
                    btnSimpan.setEnabled(true);
                    btnSimpan.setText("💾  Simpan");
                }
            }
        };
        w.execute();
    }

    // FIX: dipanggil sebelum insert ulang saat mode edit — membalikkan efek stok
    // dari baris detail lama lalu menghapusnya, supaya stok tidak dobel/salah.
    private void revertAndDeleteDetail(Connection conn, int parentId) throws SQLException {
        String detailTable = getDetailTable(tableName);
        String pk = getPK(tableName);
        if (detailTable == null) return;

        if (tableName.equals("permintaan_alat")) {
            // Dulu stok dikurangi saat dipinjam -> kembalikan dulu
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_alat, jumlah FROM " + detailTable + " WHERE " + pk + " = ?")) {
                ps.setInt(1, parentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try (PreparedStatement psStok = conn.prepareStatement(
                                "UPDATE alat SET stok = stok + ? WHERE id_alat = ?")) {
                            psStok.setInt(1, rs.getInt("jumlah"));
                            psStok.setInt(2, rs.getInt("id_alat"));
                            psStok.executeUpdate();
                        }
                    }
                }
            }
        } else if (tableName.equals("pengembalian_alat")) {
            // Dulu stok ditambah saat dikembalikan (kecuali kondisi rusak) -> kurangi lagi
            String sql = "SELECT id_alat, jumlah, kondisi FROM " + detailTable + " WHERE " + pk + " = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, parentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String kondisi = null;
                        try { kondisi = rs.getString("kondisi"); } catch (SQLException ignored) {}
                        if (kondisi == null || !kondisi.equals("rusak")) {
                            try (PreparedStatement psStok = conn.prepareStatement(
                                    "UPDATE alat SET stok = stok - ? WHERE id_alat = ?")) {
                                psStok.setInt(1, rs.getInt("jumlah"));
                                psStok.setInt(2, rs.getInt("id_alat"));
                                psStok.executeUpdate();
                            }
                        }
                    }
                }
            }
        }
        // proses_sterilisasi tidak mengubah stok, jadi tidak perlu dibalikkan

        try (PreparedStatement del = conn.prepareStatement(
                "DELETE FROM " + detailTable + " WHERE " + pk + " = ?")) {
            del.setInt(1, parentId);
            del.executeUpdate();
        }
    }

    private void saveDetailAlat(Connection conn, int parentId) throws SQLException {
        switch (tableName) {
            case "permintaan_alat" -> {
                for (int[] alat : daftarAlat) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO detail_permintaan (id_permintaan, id_alat, jumlah) VALUES (?,?,?)")) {
                        ps.setInt(1, parentId); ps.setInt(2, alat[0]); ps.setInt(3, alat[1]);
                        ps.executeUpdate();
                    }
                    // Kurangi stok
                    try (PreparedStatement psStok = conn.prepareStatement(
                            "UPDATE alat SET stok = stok - ? WHERE id_alat = ?")) {
                        psStok.setInt(1, alat[1]); psStok.setInt(2, alat[0]);
                        psStok.executeUpdate();
                    }
                }
            }
            case "pengembalian_alat" -> {
                // Ambil kondisi dari form
                String kondisi = "kotor";
                JComponent cbKondisi = fields.get("kondisi");
                if (cbKondisi instanceof JComboBox<?> cb) {
                    String[] item = (String[]) cb.getSelectedItem();
                    if (item != null && !item[0].isEmpty()) kondisi = item[0];
                }
                final String finalKondisi = kondisi;
                for (int[] alat : daftarAlat) {
                    // Coba insert dengan kolom kondisi
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO detail_pengembalian (id_pengembalian, id_alat, jumlah, kondisi) VALUES (?,?,?,?)")) {
                        ps.setInt(1, parentId); ps.setInt(2, alat[0]);
                        ps.setInt(3, alat[1]); ps.setString(4, finalKondisi);
                        ps.executeUpdate();
                    } catch (SQLException ex) {
                        // Fallback tanpa kolom kondisi
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO detail_pengembalian (id_pengembalian, id_alat, jumlah) VALUES (?,?,?)")) {
                            ps.setInt(1, parentId); ps.setInt(2, alat[0]); ps.setInt(3, alat[1]);
                            ps.executeUpdate();
                        }
                    }
                    // Tambah stok jika tidak rusak
                    if (!finalKondisi.equals("rusak")) {
                        try (PreparedStatement psStok = conn.prepareStatement(
                                "UPDATE alat SET stok = stok + ? WHERE id_alat = ?")) {
                            psStok.setInt(1, alat[1]); psStok.setInt(2, alat[0]);
                            psStok.executeUpdate();
                        }
                    }
                }
            }
            case "proses_sterilisasi" -> {
                for (int[] alat : daftarAlat) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO detail_sterilisasi (id_proses, id_alat, jumlah) VALUES (?,?,?)")) {
                        ps.setInt(1, parentId); ps.setInt(2, alat[0]); ps.setInt(3, alat[1]);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    private String getPK(String t) {
        return switch (t) {
            case "ruangan"            -> "id_ruangan";
            case "jenis_alat"         -> "id_jenis";
            case "alat"               -> "id_alat";
            case "mesin"              -> "id_mesin";
            case "dokter"             -> "id_dokter";
            case "pasien"             -> "id_pasien";
            case "petugas_cssd",
                 "petugas_ruangan"    -> "id_petugas";
            case "permintaan_alat"    -> "id_permintaan";
            case "pengembalian_alat"  -> "id_pengembalian";
            case "proses_sterilisasi" -> "id_proses";
            default                   -> "id";
        };
    }

    public boolean isSaved() { return saved; }
}

// ══════════════════════════════════════════════════════════════════════
// PERFORMA PANEL
// ══════════════════════════════════════════════════════════════════════
class PerformaPanel extends JPanel {

    public PerformaPanel() {
        setLayout(new BorderLayout());
        setBackground(LoginFrame.BG);
        buildUI();
    }

    private void buildUI() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(LoginFrame.WHITE);
        card.setBorder(BorderFactory.createLineBorder(LoginFrame.BORDER));

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(LoginFrame.WHITE);
        head.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, LoginFrame.BORDER),
            new EmptyBorder(10, 14, 10, 14)));

        JLabel title = new JLabel("⏱  Laporan Performa Query");
        title.setFont(LoginFrame.BOLD13);
        title.setForeground(LoginFrame.NAVY);

        JButton btnRun = new JButton("▶  Jalankan Tes");
        btnRun.setFont(LoginFrame.BOLD13);
        btnRun.setBackground(LoginFrame.NAVY);
        btnRun.setForeground(LoginFrame.WHITE);
        btnRun.setOpaque(true);
        btnRun.setBorderPainted(false);
        btnRun.setFocusPainted(false);
        btnRun.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        head.add(title, BorderLayout.WEST);
        head.add(btnRun, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        String[] cols = {"Query", "Waktu (ms)", "Jumlah Baris"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(LoginFrame.PLAIN13);
        table.setRowHeight(30);
        table.getTableHeader().setFont(LoginFrame.BOLD13);
        table.setGridColor(new Color(0xEDF2F7));
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel status = new JLabel(" ", SwingConstants.CENTER);
        status.setFont(LoginFrame.PLAIN11);
        status.setForeground(LoginFrame.MUTED);
        status.setBorder(new EmptyBorder(8, 0, 8, 0));
        card.add(status, BorderLayout.SOUTH);

        String[][] QUERIES = {
            {"SELECT semua alat (JOIN jenis)",         "SELECT a.*, j.nama_jenis FROM alat a LEFT JOIN jenis_alat j ON a.id_jenis=j.id_jenis"},
            {"SELECT semua permintaan (JOIN 3 tabel)", "SELECT p.*, r.nama_ruangan, d.nama_dokter FROM permintaan_alat p LEFT JOIN ruangan r ON p.id_ruangan=r.id_ruangan LEFT JOIN dokter d ON p.id_dokter=d.id_dokter"},
            {"SELECT semua pengembalian (JOIN)",       "SELECT k.*, p.nama_tindakan FROM pengembalian_alat k LEFT JOIN permintaan_alat p ON k.id_permintaan=p.id_permintaan"},
            {"SELECT proses sterilisasi (JOIN)",       "SELECT ps.*, m.nama_mesin, pc.nama_petugas FROM proses_sterilisasi ps LEFT JOIN mesin m ON ps.id_mesin=m.id_mesin LEFT JOIN petugas_cssd pc ON ps.id_petugas=pc.id_petugas"},
            {"COUNT permintaan per ruangan (GROUP BY)","SELECT r.nama_ruangan, COUNT(p.id_permintaan) AS total FROM permintaan_alat p LEFT JOIN ruangan r ON p.id_ruangan=r.id_ruangan GROUP BY r.nama_ruangan"},
            {"SELECT alat stok rendah (<10)",          "SELECT * FROM alat WHERE stok < 10 ORDER BY stok ASC"},
        };

        btnRun.addActionListener(e -> {
            btnRun.setEnabled(false);
            btnRun.setText("Mengukur...");
            model.setRowCount(0);
            status.setText("Menjalankan query...");

            SwingWorker<Void, Object[]> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection()) {
                        for (String[] q : QUERIES) {
                            long start = System.currentTimeMillis();
                            int rowCount = 0;
                            try (PreparedStatement ps = conn.prepareStatement(q[1]);
                                 ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) rowCount++;
                            }
                            publish(new Object[]{q[0], System.currentTimeMillis() - start, rowCount});
                        }
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<Object[]> chunks) {
                    for (Object[] row : chunks) model.addRow(row);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        status.setText("✅ Selesai — " + QUERIES.length + " query diuji.");
                    } catch (Exception ex) {
                        status.setText("❌ Error: " + ex.getMessage());
                    } finally {
                        btnRun.setEnabled(true);
                        btnRun.setText("▶  Jalankan Tes");
                    }
                }
            };
            w.execute();
        });

        add(card, BorderLayout.CENTER);
    }
}