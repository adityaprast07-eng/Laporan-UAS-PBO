package cssd;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

public class MainFrame extends JFrame {

    private final String userName;
    private final String userRole;
    private JPanel contentArea;
    private JLabel pageTitle;
    private final Map<String, JButton> navBtns = new LinkedHashMap<>();

    public MainFrame(String userName, String userRole) {
        this.userName = userName;
        this.userRole = userRole;
        setTitle("CSSD Rumah Sakit — " + userName);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        buildUI();
        navigate("dashboard");
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
    }

    // ── SIDEBAR ──────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(LoginFrame.NAVY);
        sb.setPreferredSize(new Dimension(205, 0));

        // Logo
        JPanel logo = new JPanel();
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBackground(new Color(0x163154));
        logo.setBorder(new EmptyBorder(16, 14, 12, 14));
        logo.setAlignmentX(LEFT_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        JLabel l1 = new JLabel("🏥  CSSD RS");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l1.setForeground(LoginFrame.WHITE);
        JLabel l2 = new JLabel("Manajemen Sterilisasi");
        l2.setFont(LoginFrame.PLAIN11);
        l2.setForeground(new Color(0x90B8D8));
        logo.add(l1); logo.add(l2);
        sb.add(logo);

        addSection(sb, "UTAMA");
        addNav(sb, "dashboard",          "📊  Dashboard");
        addSection(sb, "MASTER DATA");
        addNav(sb, "alat",               "🔧  Alat");
        addNav(sb, "jenis_alat",         "🏷  Jenis Alat");
        addNav(sb, "mesin",              "⚙  Mesin");
        addNav(sb, "ruangan",            "🏥  Ruangan");
        addNav(sb, "dokter",             "👨‍⚕  Dokter");
        addNav(sb, "pasien",             "🧑  Pasien");
        addNav(sb, "petugas_cssd",       "👷  Petugas CSSD");
        addNav(sb, "petugas_ruangan",    "👩‍⚕  Petugas Ruangan");
        addSection(sb, "TRANSAKSI");
        addNav(sb, "permintaan_alat",    "📋  Permintaan Alat");
        addNav(sb, "pengembalian_alat",  "↩  Pengembalian");
        addNav(sb, "proses_sterilisasi", "🧪  Sterilisasi");
        addSection(sb, "LAPORAN");
        addNav(sb, "performa",           "⏱  Performa Query");

        sb.add(Box.createVerticalGlue());

        // Footer user info
        JPanel foot = new JPanel();
        foot.setLayout(new BoxLayout(foot, BoxLayout.Y_AXIS));
        foot.setBackground(new Color(0x163154));
        foot.setBorder(new EmptyBorder(10, 14, 10, 14));
        foot.setAlignmentX(LEFT_ALIGNMENT);
        foot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel nameLbl = new JLabel(userName);
        nameLbl.setFont(LoginFrame.BOLD13);
        nameLbl.setForeground(new Color(0xC8DFF0));
        JLabel roleLbl = new JLabel(userRole.toUpperCase());
        roleLbl.setFont(LoginFrame.PLAIN11);
        roleLbl.setForeground(new Color(0x90B8D8));

        JButton btnOut = new JButton("⏏  Keluar");
        btnOut.setFont(LoginFrame.PLAIN11);
        btnOut.setForeground(new Color(0xFC8181));
        btnOut.setBackground(LoginFrame.NAVY);
        btnOut.setBorderPainted(false);
        btnOut.setFocusPainted(false);
        btnOut.setContentAreaFilled(false);
        btnOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOut.setAlignmentX(LEFT_ALIGNMENT);
        btnOut.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?",
                    "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                DBConnection.closeConnection();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        foot.add(nameLbl); foot.add(roleLbl);
        foot.add(Box.createVerticalStrut(8));
        foot.add(btnOut);
        sb.add(foot);
        return sb;
    }

    private void addSection(JPanel sb, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(0x90B8D8));
        lbl.setBorder(new EmptyBorder(10, 14, 4, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        sb.add(lbl);
    }

    private void addNav(JPanel sb, String key, String label) {
        JButton btn = new JButton(label);
        btn.setFont(LoginFrame.PLAIN13);
        btn.setForeground(new Color(0xC8DFF0));
        btn.setBackground(LoginFrame.NAVY);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(0, 14, 0, 0));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!key.equals(getActiveKey())) {
                    btn.setBackground(LoginFrame.BLUE);
                    btn.setForeground(LoginFrame.WHITE);
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!key.equals(getActiveKey())) {
                    btn.setBackground(LoginFrame.NAVY);
                    btn.setForeground(new Color(0xC8DFF0));
                }
            }
        });
        btn.addActionListener(e -> navigate(key));
        navBtns.put(key, btn);
        sb.add(btn);
    }

    private String activeKey = "";
    private String getActiveKey() { return activeKey; }

    // ── MAIN AREA ────────────────────────────────────────────────────
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(LoginFrame.BG);

        // Topbar
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(LoginFrame.WHITE);
        topbar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, LoginFrame.BORDER),
            new EmptyBorder(0, 20, 0, 20)));
        topbar.setPreferredSize(new Dimension(0, 48));

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pageTitle.setForeground(LoginFrame.NAVY);

        JLabel dateLbl = new JLabel(new java.util.Date().toString().substring(0, 10));
        dateLbl.setFont(LoginFrame.PLAIN11);
        dateLbl.setForeground(LoginFrame.MUTED);

        topbar.add(pageTitle, BorderLayout.WEST);
        topbar.add(dateLbl, BorderLayout.EAST);
        main.add(topbar, BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(LoginFrame.BG);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(contentArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // ── NAVIGATE ─────────────────────────────────────────────────────
    void navigate(String key) {
        activeKey = key;
        navBtns.forEach((k, btn) -> {
            btn.setBackground(LoginFrame.NAVY);
            btn.setForeground(new Color(0xC8DFF0));
        });
        JButton active = navBtns.get(key);
        if (active != null) {
            active.setBackground(LoginFrame.BLUE);
            active.setForeground(LoginFrame.WHITE);
        }

        Map<String, String> titles = Map.ofEntries(
            Map.entry("dashboard",          "Dashboard"),
            Map.entry("alat",               "Data Alat"),
            Map.entry("jenis_alat",         "Jenis Alat"),
            Map.entry("mesin",              "Mesin"),
            Map.entry("ruangan",            "Ruangan"),
            Map.entry("dokter",             "Dokter"),
            Map.entry("pasien",             "Pasien"),
            Map.entry("petugas_cssd",       "Petugas CSSD"),
            Map.entry("petugas_ruangan",    "Petugas Ruangan"),
            Map.entry("permintaan_alat",    "Permintaan Alat"),
            Map.entry("pengembalian_alat",  "Pengembalian Alat"),
            Map.entry("proses_sterilisasi", "Proses Sterilisasi"),
            Map.entry("performa",           "Laporan Performa Query")
        );
        pageTitle.setText(titles.getOrDefault(key, key));

        contentArea.removeAll();
        switch (key) {
            case "dashboard" -> contentArea.add(new DashboardPanel(this), BorderLayout.CENTER);
            case "performa"  -> contentArea.add(new PerformaPanel(), BorderLayout.CENTER);
            default          -> contentArea.add(new TablePanel(key, this), BorderLayout.CENTER);
        }
        contentArea.revalidate();
        contentArea.repaint();
    }
}
