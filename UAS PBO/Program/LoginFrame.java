package cssd;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;
    private int attempt = 0;

    // Warna tema
    static final Color NAVY    = new Color(0x1A3A5C);
    static final Color BLUE    = new Color(0x2C5282);
    static final Color LBLUE   = new Color(0x63B3ED);
    static final Color WHITE   = Color.WHITE;
    static final Color BG      = new Color(0xF0F4F8);
    static final Color BORDER  = new Color(0xE2E8F0);
    static final Color MUTED   = new Color(0x718096);
    static final Color GREEN   = new Color(0x38A169);
    static final Color RED     = new Color(0xE53E3E);
    static final Font  BOLD13  = new Font("Segoe UI", Font.BOLD, 13);
    static final Font  PLAIN13 = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font  PLAIN11 = new Font("Segoe UI", Font.PLAIN, 11);

    public LoginFrame() {
        setTitle("Login — CSSD Rumah Sakit");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 430);
        setResizable(false);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG);
        setContentPane(root);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(WHITE);
        box.setBorder(new EmptyBorder(32, 32, 32, 32));

        // Header
        JLabel iconLbl = new JLabel("🏥  CSSD RUMAH SAKIT");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        iconLbl.setForeground(NAVY);
        iconLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel("Sistem Manajemen Sterilisasi");
        subLbl.setFont(PLAIN11);
        subLbl.setForeground(MUTED);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(BORDER);

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(PLAIN11);
        lblError.setForeground(RED);
        lblError.setAlignmentX(LEFT_ALIGNMENT);
        lblError.setOpaque(true);
        lblError.setBackground(new Color(0xFFF5F5));
        lblError.setBorder(new EmptyBorder(6, 10, 6, 10));
        lblError.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        lblError.setVisible(false);

        // Form fields
        JLabel lUser = new JLabel("Username");
        lUser.setFont(BOLD13); lUser.setAlignmentX(LEFT_ALIGNMENT);

        txtUsername = new JTextField();
        txtUsername.setFont(PLAIN13);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtUsername.setAlignmentX(LEFT_ALIGNMENT);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(4, 8, 4, 8)));

        JLabel lPass = new JLabel("Password");
        lPass.setFont(BOLD13); lPass.setAlignmentX(LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(PLAIN13);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPassword.setAlignmentX(LEFT_ALIGNMENT);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(4, 8, 4, 8)));
        txtPassword.addActionListener(e -> doLogin());

        btnLogin = new JButton("Masuk");
        btnLogin.setFont(BOLD13);
        btnLogin.setBackground(NAVY);
        btnLogin.setForeground(WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());

        JLabel hint = new JLabel("Default: admin / admin123  |  petugas / petugas123");
        hint.setFont(PLAIN11); hint.setForeground(MUTED); hint.setAlignmentX(LEFT_ALIGNMENT);

        box.add(iconLbl);
        box.add(Box.createVerticalStrut(4));
        box.add(subLbl);
        box.add(Box.createVerticalStrut(12));
        box.add(sep);
        box.add(Box.createVerticalStrut(10));
        box.add(lblError);
        box.add(Box.createVerticalStrut(6));
        box.add(lUser);
        box.add(Box.createVerticalStrut(4));
        box.add(txtUsername);
        box.add(Box.createVerticalStrut(12));
        box.add(lPass);
        box.add(Box.createVerticalStrut(4));
        box.add(txtPassword);
        box.add(Box.createVerticalStrut(20));
        box.add(btnLogin);
        box.add(Box.createVerticalStrut(14));
        box.add(hint);

        root.add(box);
        txtUsername.requestFocusInWindow();
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password wajib diisi.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Memeriksa...");

        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT nama, role FROM users WHERE username = ? AND password = ?");
                    ps.setString(1, username);
                    ps.setString(2, password);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) return new String[]{"ok", rs.getString("nama"), rs.getString("role")};
                    return new String[]{"fail"};
                } catch (SQLException e) {
                    return new String[]{"error", e.getMessage()};
                }
            }

            @Override
            protected void done() {
                try {
                    String[] result = get();
                    if ("ok".equals(result[0])) {
                        dispose();
                        new MainFrame(result[1], result[2]).setVisible(true);
                    } else if ("error".equals(result[0])) {
                        showError("Error koneksi: " + result[1]);
                    } else {
                        attempt++;
                        int sisa = 3 - attempt;
                        if (sisa <= 0) {
                            showError("Terlalu banyak percobaan. Ditutup...");
                            Timer t = new Timer(2000, ev -> System.exit(0));
                            t.setRepeats(false); t.start();
                        } else {
                            showError("Username/password salah. Sisa: " + sisa + "x");
                        }
                        txtPassword.setText("");
                    }
                } catch (Exception e) {
                    showError("Kesalahan: " + e.getMessage());
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Masuk");
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        lblError.setText("⚠  " + msg);
        lblError.setVisible(true);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
