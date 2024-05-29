package gradleproject1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App extends JFrame {
    // Komponen GUI
    private JTextField portField;
    private JTextField rootDirectoryField;
    private JTextField logFileField;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;

    // Konstruktor untuk kelas App yang mengatur GUI
    public App() {
        // Membuat komponen GUI
        portField = new JTextField("8080", 10);
        rootDirectoryField = new JTextField("D:\\localhost", 20);
        logFileField = new JTextField("C:\\Users\\ACER\\OneDrive\\Documents\\NetBeansProjects\\latihan mandiri java\\gradle\\gradleproject1\\app\\src\\main\\java\\gradleproject1\\log", 20);
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        logArea = new JTextArea(15, 50);

        // Mengatur layout untuk konfigurasi panel
        JPanel configPanel = new JPanel(new GridLayout(3, 2));
        configPanel.add(new JLabel("Port:"));
        configPanel.add(portField);
        configPanel.add(new JLabel("Root Directory:"));
        configPanel.add(rootDirectoryField);
        configPanel.add(new JLabel("Log File:"));
        configPanel.add(logFileField);

        // Mengatur layout untuk panel tombol
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Mengatur layout untuk panel utama
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(configPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Menambahkan panel utama ke frame
        this.add(mainPanel);

        // Mengatur action listener untuk tombol start
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Memulai server dengan konfigurasi yang diberikan
                SimpleWebServer.startServer(portField.getText(), rootDirectoryField.getText(), logFileField.getText(), logArea);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        // Mengatur action listener untuk tombol stop
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Menghentikan server
                SimpleWebServer.stopServer();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        // Mengatur properti frame
        this.setTitle("Simple Web Server");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    // Metode main untuk menjalankan aplikasi
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new App();
            }
        });
    }
}
