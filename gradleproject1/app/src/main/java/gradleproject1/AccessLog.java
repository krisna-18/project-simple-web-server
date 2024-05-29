package gradleproject1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AccessLog {
    // Variabel yang menyimpan path direktori log
    private final Path logDirectory;

    // Konstruktor untuk kelas AccessLog yang menerima path direktori log sebagai argumen
    public AccessLog(Path logDirectory) {
        this.logDirectory = logDirectory;
    }

    // Metode untuk mencatat pesan log
    public void log(String message) throws IOException {
        // Mendapatkan timestamp saat ini dalam format "yyyy-MM-dd HH:mm:ss"
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // Membuat entri log dengan format "timestamp - message"
        String logEntry = String.format("%s - %s%n", timestamp, message);

        // Membuat nama file log berdasarkan tanggal saat ini dalam format "yyyy-MM-dd.log"
        String logFileName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
        // Menyusun path lengkap untuk file log
        Path logFilePath = logDirectory.resolve(logFileName);

        // Menulis entri log ke dalam file log, membuat file jika belum ada, dan menambahkan (append) jika sudah ada
        Files.write(logFilePath, logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
