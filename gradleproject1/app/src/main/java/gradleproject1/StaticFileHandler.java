package gradleproject1;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler {
    // Variabel yang menyimpan path file statis yang akan dilayani
    private final Path filePath;

    // Konstruktor untuk kelas StaticFileHandler yang menerima path file sebagai argumen
    public StaticFileHandler(Path filePath) {
        this.filePath = filePath;
    }

    // Metode untuk menangani permintaan file statis dan menulis konten file ke output stream
    public void handle(OutputStream outputStream) throws IOException {
        // Menyalin konten file ke output stream
        Files.copy(filePath, outputStream);
    }
}
