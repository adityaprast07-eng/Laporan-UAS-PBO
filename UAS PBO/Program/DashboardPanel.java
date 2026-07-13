package cssd;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {

    private final MainFrame parent;

    public DashboardPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(LoginFrame.BG);
        setAlignmentX(LEFT_ALIGNMENT);
        add(new JLabel("Memuat dashboard...")); 
        loadData();
    }

    private void loadData() {
        SwingWorker<int[], Void> w = new SwingWorker<>() {
            int[] counts = new int[4];
            Object[][] permintaan = new Object[0][];
            Object[][] stokRendah = new Object[0][];

            @Override
            protected int[] doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    String[] tables = {"alat","permintaan_alat","pengembalian_alat","proses_sterilisasi"};
                    for (int i = 0; i < tables.length; i++) {
                        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + tables[i]);
                        if (rs.next()) counts[i] = rs.getInt(1);
                    }
                    ResultSet rs2 = conn.createStatement().executeQuery(
                        "SELECT p.id_permintaan, r.nama_ruangan, p.nama_tindakan, p.tanggal_permintaan " +
                        "FROM permintaan_alat p LEFT JOIN ruangan r ON p.id_ruangan=r.id_ruangan " +
                        "ORDER BY p.id_permintaan DESC LIMIT 5");
                    java.util.List<Object[]> list = new java.util.ArrayList<>();
                    while (rs2.next()) list.add(new Object[]{
                        rs2.getInt(1), rs2.getString(2), rs2.getString(3), rs2.getDate(4)});
                    permintaan = list.toArray(new Object[0][]);

                    ResultSet rs3 = conn.createStatement().executeQuery(
                        "SELECT nama_alat, tipe, stok FROM alat WHERE stok < 10 ORDER BY stok ASC LIMIT 8");
                    java.util.List<Object[]> list2 = new java.util.ArrayList<>();
                    while (rs3.next()) list2.add(new Object[]{rs3.getString(1), rs3.getString(2), rs3.getInt(3)});
                    stokRendah = list2.toArray(new Object[0][]);
                }
                return counts;
            }

            @Override
            protected void done() {
                try { get(); buildUI(counts, permintaan, stokRendah); }
                catch (Exception e) {
                    removeAll();
                    add(new JLabel("❌ Gagal memuat: " + e.getMessage()));
                    revalidate();
                }
            }
        };
        w.execute();
    }

    private void buildUI(int[] counts, Object[][] permintaan, Object[][] stokRendah) {
        removeAll();

        // Stat cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setBackground(LoginFrame.BG);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        cards.setAlignmentX(LEFT_ALIGNMENT);
        cards.add(statCard("Total Alat",    counts[0], new Color(0x63B3ED)));
        cards.add(statCard("Permintaan",    counts[1], new Color(0x48BB78)));
        cards.add(statCard("Pengembalian",  counts[2], new Color(0xED8936)));
        cards.add(statCard("Sterilisasi",   counts[3], new Color(0xFC8181)));
        add(cards);
        add(Box.createVerticalStrut(16));

        // Bottom grid
        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 0));
        grid.setBackground(LoginFrame.BG);
        grid.setAlignmentX(LEFT_ALIGNMENT);

        // Permintaan terbaru
        JPanel p1 = cardPanel("📋  Permintaan Terbaru",
            new String[]{"ID","Ruangan","Tindakan","Tanggal"}, permintaan);
        grid.add(p1);

        // Stok rendah
        JPanel p2 = cardPanel("⚠  Stok Alat Rendah (<10)",
            new String[]{"Nama Alat","Tipe","Stok"}, stokRendah);
        grid.add(p2);

        add(grid);
        revalidate(); repaint();
    }

    private JPanel statCard(String label, int value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(LoginFrame.WHITE);
        card.setBorder(BorderFactory.createLineBorder(LoginFrame.BORDER));

        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(0, 4));
        card.add(bar, BorderLayout.NORTH);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(LoginFrame.WHITE);
        inner.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(LoginFrame.MUTED);

        JLabel val = new JLabel(String.valueOf(value));
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(LoginFrame.NAVY);

        inner.add(lbl); inner.add(Box.createVerticalStrut(4)); inner.add(val);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel cardPanel(String title, String[] cols, Object[][] data) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(LoginFrame.WHITE);
        card.setBorder(BorderFactory.createLineBorder(LoginFrame.BORDER));

        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        head.setBackground(LoginFrame.WHITE);
        head.setBorder(new MatteBorder(0, 0, 1, 0, LoginFrame.BORDER));
        JLabel lbl = new JLabel(title);
        lbl.setFont(LoginFrame.BOLD13); lbl.setForeground(LoginFrame.NAVY);
        head.add(lbl);
        card.add(head, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model);
        tbl.setFont(LoginFrame.PLAIN13);
        tbl.setRowHeight(28);
        tbl.getTableHeader().setFont(LoginFrame.BOLD13);
        tbl.getTableHeader().setBackground(new Color(0xF7FAFC));
        tbl.setSelectionBackground(new Color(0xEBF8FF));
        tbl.setGridColor(new Color(0xEDF2F7));
        card.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return card;
    }
}
