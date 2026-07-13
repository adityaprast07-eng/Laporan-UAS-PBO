package cssd;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class TablePanel extends JPanel {

    private final String tableName;
    private final MainFrame parent;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private java.util.List<Map<String, Object>> allRows = new ArrayList<>();
    private java.util.List<Integer> filteredIndices = new ArrayList<>();

    private static final Map<String, String[]> COLUMNS = Map.ofEntries(
        Map.entry("ruangan",            new String[]{"id_ruangan","nama_ruangan"}),
        Map.entry("jenis_alat",         new String[]{"id_jenis","nama_jenis"}),
        Map.entry("alat",               new String[]{"id_alat","nama_alat","nama_jenis","tipe","stok"}),
        Map.entry("mesin",              new String[]{"id_mesin","nama_mesin","nomor_mesin"}),
        Map.entry("dokter",             new String[]{"id_dokter","nama_dokter"}),
        Map.entry("pasien",             new String[]{"id_pasien","nama_pasien","no_rekam_medis"}),
        Map.entry("petugas_cssd",       new String[]{"id_petugas","nama_petugas","shift"}),
        Map.entry("petugas_ruangan",    new String[]{"id_petugas","nama_petugas","jabatan","nama_ruangan"}),
        // FIX: tambah kolom "daftar_alat" supaya list Permintaan Alat menampilkan alat yang dipinjam
        Map.entry("permintaan_alat",    new String[]{"id_permintaan","tanggal_permintaan","nama_ruangan","nama_tindakan","jenis_transaksi","nama_dokter","daftar_alat"}),
        // FIX: "kondisi" sekarang diambil dari subquery detail_pengembalian (lihat buildQuery),
        // dan tambah "daftar_alat" untuk menampilkan alat yang dikembalikan
        Map.entry("pengembalian_alat",  new String[]{"id_pengembalian","tanggal_pengembalian","id_permintaan","kondisi","daftar_alat","keterangan"}),
        Map.entry("proses_sterilisasi", new String[]{"id_proses","tanggal","nama_mesin","nomor_siklus","jumlah_siklus","nama_petugas"})
    );

    private static final Map<String, String> LABEL = Map.ofEntries(
        Map.entry("id_ruangan","ID"), Map.entry("nama_ruangan","Nama Ruangan"),
        Map.entry("id_jenis","ID"), Map.entry("nama_jenis","Nama Jenis"),
        Map.entry("id_alat","ID"), Map.entry("nama_alat","Nama Alat"),
        Map.entry("tipe","Tipe"), Map.entry("stok","Stok"),
        Map.entry("id_mesin","ID"), Map.entry("nama_mesin","Nama Mesin"), Map.entry("nomor_mesin","No. Mesin"),
        Map.entry("id_dokter","ID"), Map.entry("nama_dokter","Nama Dokter"),
        Map.entry("id_pasien","ID"), Map.entry("nama_pasien","Nama Pasien"), Map.entry("no_rekam_medis","No. RM"),
        Map.entry("id_petugas","ID"), Map.entry("nama_petugas","Nama Petugas"),
        Map.entry("shift","Shift"), Map.entry("jabatan","Jabatan"),
        Map.entry("id_permintaan","#"), Map.entry("tanggal_permintaan","Tanggal"),
        Map.entry("nama_tindakan","Tindakan"), Map.entry("jenis_transaksi","Jenis"),
        Map.entry("id_pengembalian","#"), Map.entry("tanggal_pengembalian","Tanggal"),
        Map.entry("kondisi","Kondisi"), Map.entry("keterangan","Keterangan"),
        Map.entry("id_proses","#"), Map.entry("tanggal","Tanggal"),
        Map.entry("nomor_siklus","No. Siklus"), Map.entry("jumlah_siklus","Jml Siklus"),
        Map.entry("daftar_alat","Alat") // FIX: label kolom baru
    );

    public TablePanel(String tableName, MainFrame parent) {
        this.tableName = tableName;
        this.parent    = parent;
        setLayout(new BorderLayout());
        setBackground(LoginFrame.BG);
        buildUI();
        loadData();
    }

    private void buildUI() {
        String[] cols = COLUMNS.getOrDefault(tableName, new String[]{});
        String[] headers = Arrays.stream(cols).map(c -> LABEL.getOrDefault(c, c)).toArray(String[]::new);

        model = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(LoginFrame.PLAIN13);
        table.setRowHeight(30);
        table.getTableHeader().setFont(LoginFrame.BOLD13);
        table.getTableHeader().setBackground(new Color(0xF7FAFC));
        table.getTableHeader().setForeground(new Color(0x4A5568));
        table.setSelectionBackground(new Color(0xEBF8FF));
        table.setGridColor(new Color(0xEDF2F7));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(LoginFrame.WHITE);
        card.setBorder(BorderFactory.createLineBorder(LoginFrame.BORDER));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(LoginFrame.WHITE);
        head.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, LoginFrame.BORDER),
            new EmptyBorder(8, 14, 8, 14)));

        JLabel titleLbl = new JLabel(getTitle());
        titleLbl.setFont(LoginFrame.BOLD13);
        titleLbl.setForeground(LoginFrame.NAVY);

        searchField = new JTextField(14);
        searchField.setFont(LoginFrame.PLAIN13);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LoginFrame.BORDER),
            new EmptyBorder(2, 6, 2, 6)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterRows(searchField.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterRows(searchField.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JButton btnTambah = makeBtn("+ Tambah", LoginFrame.NAVY, LoginFrame.WHITE);
        btnTambah.addActionListener(e -> openForm(null));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(LoginFrame.WHITE);
        actions.add(new JLabel("🔍"));
        actions.add(searchField);
        actions.add(btnTambah);

        head.add(titleLbl, BorderLayout.WEST);
        head.add(actions, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer actions
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        foot.setBackground(LoginFrame.WHITE);
        foot.setBorder(new MatteBorder(1, 0, 0, 0, LoginFrame.BORDER));

        JButton btnEdit  = makeBtn("✏  Edit",  new Color(0xEBF8FF), new Color(0x2B6CB0));
        JButton btnHapus = makeBtn("🗑  Hapus", new Color(0xFFF5F5), new Color(0xC53030));

        btnEdit.setBorder(BorderFactory.createLineBorder(new Color(0xBEE3F8)));
        btnHapus.setBorder(BorderFactory.createLineBorder(new Color(0xFEB2B2)));

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih baris yang ingin diedit."); return; }
            openForm(allRows.get(getActualIndex(row)));
        });
        btnHapus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus."); return; }
            confirmDelete(allRows.get(getActualIndex(row)));
        });

        foot.add(btnEdit); foot.add(btnHapus);
        card.add(foot, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(LoginFrame.BOLD13);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String getTitle() {
        Map<String, String> m = Map.ofEntries(
            Map.entry("ruangan","Ruangan"), Map.entry("jenis_alat","Jenis Alat"),
            Map.entry("alat","Data Alat"), Map.entry("mesin","Mesin"),
            Map.entry("dokter","Dokter"), Map.entry("pasien","Pasien"),
            Map.entry("petugas_cssd","Petugas CSSD"), Map.entry("petugas_ruangan","Petugas Ruangan"),
            Map.entry("permintaan_alat","Permintaan Alat"),
            Map.entry("pengembalian_alat","Pengembalian Alat"),
            Map.entry("proses_sterilisasi","Proses Sterilisasi")
        );
        return m.getOrDefault(tableName, tableName);
    }

    public void loadData() {
        model.setRowCount(0);
        SwingWorker<java.util.List<Map<String, Object>>, Void> w = new SwingWorker<>() {
            @Override
            protected java.util.List<Map<String, Object>> doInBackground() throws Exception {
                return fetchRows();
            }
            @Override
            protected void done() {
                try { allRows = get(); populateTable(allRows); }
                catch (Exception e) { JOptionPane.showMessageDialog(TablePanel.this, "❌ Gagal memuat: " + e.getMessage()); }
            }
        };
        w.execute();
    }

    private java.util.List<Map<String, Object>> fetchRows() throws Exception {
        java.util.List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(buildQuery())) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                rows.add(row);
            }
        }
        return rows;
    }

    private String buildQuery() {
        return switch (tableName) {
            case "alat"            -> "SELECT a.*, j.nama_jenis FROM alat a LEFT JOIN jenis_alat j ON a.id_jenis=j.id_jenis ORDER BY a.id_alat";
            case "petugas_ruangan" -> "SELECT pr.*, r.nama_ruangan FROM petugas_ruangan pr LEFT JOIN ruangan r ON pr.id_ruangan=r.id_ruangan ORDER BY pr.id_petugas";

            // FIX: tambah subquery GROUP_CONCAT untuk menampilkan ringkasan alat yang dipinjam
            case "permintaan_alat" -> "SELECT p.*, r.nama_ruangan, d.nama_dokter, " +
                "(SELECT GROUP_CONCAT(CONCAT(a.nama_alat,' (x',dp.jumlah,')') SEPARATOR ', ') " +
                " FROM detail_permintaan dp JOIN alat a ON dp.id_alat = a.id_alat " +
                " WHERE dp.id_permintaan = p.id_permintaan) AS daftar_alat " +
                "FROM permintaan_alat p " +
                "LEFT JOIN ruangan r ON p.id_ruangan=r.id_ruangan " +
                "LEFT JOIN dokter d ON p.id_dokter=d.id_dokter " +
                "ORDER BY p.id_permintaan DESC";

            // FIX: "kondisi" & "daftar_alat" sekarang diambil dari detail_pengembalian,
            // karena kolom kondisi memang tidak ada di tabel pengembalian_alat
            case "pengembalian_alat" -> "SELECT pa.*, " +
                "(SELECT GROUP_CONCAT(DISTINCT dp.kondisi SEPARATOR ', ') " +
                " FROM detail_pengembalian dp WHERE dp.id_pengembalian = pa.id_pengembalian) AS kondisi, " +
                "(SELECT GROUP_CONCAT(CONCAT(a.nama_alat,' (x',dp.jumlah,')') SEPARATOR ', ') " +
                " FROM detail_pengembalian dp JOIN alat a ON dp.id_alat = a.id_alat " +
                " WHERE dp.id_pengembalian = pa.id_pengembalian) AS daftar_alat " +
                "FROM pengembalian_alat pa " +
                "ORDER BY pa.id_pengembalian DESC";

            case "proses_sterilisasi" -> "SELECT ps.*, pc.nama_petugas FROM proses_sterilisasi ps LEFT JOIN petugas_cssd pc ON ps.id_petugas=pc.id_petugas ORDER BY ps.id_proses DESC";
            default -> "SELECT * FROM " + tableName + " ORDER BY 1";
        };
    }

    private void populateTable(java.util.List<Map<String, Object>> rows) {
        String[] cols = COLUMNS.getOrDefault(tableName, new String[]{});
        model.setRowCount(0);
        for (Map<String, Object> row : rows) {
            Object[] r = new Object[cols.length];
            for (int i = 0; i < cols.length; i++) r[i] = row.getOrDefault(cols[i], "");
            model.addRow(r);
        }
    }

    private void filterRows(String q) {
        String[] cols = COLUMNS.getOrDefault(tableName, new String[]{});
        model.setRowCount(0);
        filteredIndices.clear();
        String lower = q.toLowerCase();
        for (int i = 0; i < allRows.size(); i++) {
            Map<String, Object> row = allRows.get(i);
            boolean match = q.isEmpty() || row.values().stream()
                .anyMatch(v -> v != null && v.toString().toLowerCase().contains(lower));
            if (match) {
                filteredIndices.add(i);
                Object[] r = new Object[cols.length];
                for (int j = 0; j < cols.length; j++) r[j] = row.getOrDefault(cols[j], "");
                model.addRow(r);
            }
        }
    }

    private int getActualIndex(int tableRow) {
        if (filteredIndices.isEmpty() || searchField.getText().isEmpty()) return tableRow;
        return filteredIndices.get(tableRow);
    }

    private void openForm(Map<String, Object> row) {
        CRUDDialog dialog = new CRUDDialog((JFrame) SwingUtilities.getWindowAncestor(this), tableName, row);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void confirmDelete(Map<String, Object> row) {
        int r = JOptionPane.showConfirmDialog(this,
            "Hapus data ini? Tindakan tidak dapat dibatalkan.",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;

        String pk = getPK();
        Object id = row.get(pk);
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        if (tableName.equals("permintaan_alat")) {
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM detail_permintaan WHERE id_permintaan=?")) {
                                ps.setObject(1, id); ps.executeUpdate();
                            }
                        } else if (tableName.equals("proses_sterilisasi")) {
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM detail_sterilisasi WHERE id_proses=?")) {
                                ps.setObject(1, id); ps.executeUpdate();
                            }
                        } else if (tableName.equals("pengembalian_alat")) {
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM detail_pengembalian WHERE id_pengembalian=?")) {
                                ps.setObject(1, id); ps.executeUpdate();
                            }
                        }
                        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tableName + " WHERE " + pk + "=?")) {
                            ps.setObject(1, id); ps.executeUpdate();
                        }
                        conn.commit();
                    } catch (SQLException e) { conn.rollback(); throw e; }
                    finally { conn.setAutoCommit(true); }
                }
                return null;
            }
            @Override
            protected void done() {
                try { get(); JOptionPane.showMessageDialog(TablePanel.this, "✅ Data berhasil dihapus."); loadData(); }
                catch (Exception e) { JOptionPane.showMessageDialog(TablePanel.this, "❌ Gagal: " + e.getMessage()); }
            }
        };
        w.execute();
    }

    private String getPK() {
        return switch (tableName) {
            case "ruangan"            -> "id_ruangan";
            case "jenis_alat"         -> "id_jenis";
            case "alat"               -> "id_alat";
            case "mesin"              -> "id_mesin";
            case "dokter"             -> "id_dokter";
            case "pasien"             -> "id_pasien";
            case "petugas_cssd","petugas_ruangan" -> "id_petugas";
            case "permintaan_alat"    -> "id_permintaan";
            case "pengembalian_alat"  -> "id_pengembalian";
            case "proses_sterilisasi" -> "id_proses";
            default                   -> "id";
        };
    }
}