package gradleproject1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextArea;

public class SimpleWebServer {
    private static ServerSocket serverSocket;

    // Metode untuk memulai server
    public static void startServer(String port, String rootDirectory, String logFile, JTextArea logArea) {
        int portNumber = Integer.parseInt(port);
        Path rootDirPath = Paths.get(rootDirectory);
        Path logFilePath = Paths.get(logFile);

        try {
            // Membuat ServerSocket pada port yang ditentukan
            serverSocket = new ServerSocket(portNumber);
            logArea.append("Server started on port " + portNumber + "\n");

            // Membuat dan memulai thread baru untuk menerima dan menangani permintaan klien
            new Thread(() -> {
                try {
                    while (true) {
                        Socket socket = serverSocket.accept();
                        handleRequest(socket, rootDirPath, logFilePath, logArea);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metode untuk menghentikan server
    public static void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metode untuk menangani permintaan klien
    private static void handleRequest(Socket socket, Path rootDirectory, Path logFile, JTextArea logArea) {
        PrintWriter writer = null;
        AccessLog accessLog = new AccessLog(logFile);
        String[] parts = null;

        try {
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            String line;
            StringBuilder request = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Membaca request dari klien
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                request.append(line).append("\r\n");
            }

            parts = request.toString().split(" ");
            if (parts.length < 3 || !parts[0].equalsIgnoreCase("GET")) {
                return;
            }

            String path = parts[1];
            String timeStamp = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
            String clientIp = socket.getInetAddress().getHostAddress();

            // Jika path adalah direktori, menampilkan daftar file dalam direktori
            if (path.endsWith("/")) {
                logArea.append(timeStamp + " - Directory accessed: " + path + "\n");

                File directory = new File(rootDirectory.toString() + path);
                File[] fileList = directory.listFiles();
                StringBuilder htmlResponse = new StringBuilder();
                htmlResponse.append("<html><head><style>")
                        .append("body { font-family: Arial, sans-serif; margin: 20px; }")
                        .append("h1 { color: #333; font-size: 24px; }")
                        .append("table { width: 100%; border-collapse: collapse; }")
                        .append("th, td { padding: 8px 12px; border: 1px solid #ddd; text-align: left; }")
                        .append("th { background-color: #f2f2f2; }")
                        .append("a { text-decoration: none; color: #1a73e8; }")
                        .append("a:hover { text-decoration: underline; }")
                        .append(".container { max-width: 800px; margin: 0 auto; }")
                        .append("</style></head><body>")
                        .append("<div class='container'>")
                        .append("<h1>Directory Listing</h1>")
                        .append("<table>")
                        .append("<tr><th>Name</th><th>Last Modified</th><th>Size</th></tr>");

                if (fileList != null) {
                    for (File file : fileList) {
                        String fileName = file.getName();
                        String filePath = path + fileName + (file.isDirectory() ? "/" : "");
                        String lastModified = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                .format(LocalDateTime.ofEpochSecond(file.lastModified() / 1000, 0, ZoneOffset.UTC));
                        String size = file.isDirectory() ? "-" : humanReadableByteCountSI(file.length());

                        htmlResponse.append("<tr>")
                                .append("<td><a href=\"" + filePath + "\">" + fileName + "</a></td>")
                                .append("<td>" + lastModified + "</td>")
                                .append("<td>" + size + "</td>")
                                .append("</tr>");
                    }
                }

                htmlResponse.append("</table></div></body></html>");

                writer.print("HTTP/1.1 200 OK\r\n");
                writer.print("Content-Type: text/html\r\n");
                writer.print("Content-Length: " + htmlResponse.length() + "\r\n");
                writer.print("\r\n");
                writer.print(htmlResponse.toString());
                writer.flush();

                // Log akses direktori
                accessLog.log(String.format("URL: %s, IP: %s", path, clientIp));
            } else {
                logArea.append(timeStamp + " - File accessed: " + path + "\n");

                // Menangani permintaan file
                Path filePath = rootDirectory.resolve(path.substring(1));
                if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                    writer.print("HTTP/1.1 404 Not Found\r\n\r\n");
                    writer.flush();

                    // Log error 404 Not Found
                    accessLog.log(String.format("URL: %s, IP: %s, Error: 404 Not Found", path, clientIp));
                    return;
                }

                // Baca file dan kirimkan ke output stream soket
                byte[] fileBytes = Files.readAllBytes(filePath);
                writer.print("HTTP/1.1 200 OK\r\n");
                writer.print("Content-Type: " + Files.probeContentType(filePath) + "\r\n");
                writer.print("Content-Length: " + fileBytes.length + "\r\n");
                writer.print("\r\n");
                writer.flush();
                socket.getOutputStream().write(fileBytes);
                socket.getOutputStream().flush();

                // Log akses file
                accessLog.log(String.format("URL: %s, IP: %s", path, clientIp));
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (writer != null) {
                writeInternalServerError(writer, e.getMessage());
            }

            // Log error lainnya
            try {
                if (parts != null && parts.length > 1) {
                    String clientIp = socket.getInetAddress().getHostAddress();
                    accessLog.log(String.format("URL: %s, IP: %s, Error: %s", parts[1], clientIp, e.getMessage()));
                }
            } catch (IOException logException) {
                logException.printStackTrace();
            }
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Metode untuk mengonversi ukuran file ke format yang mudah dibaca manusia
    private static String humanReadableByteCountSI(long bytes) {
        if (bytes < 1000) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1000));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1000, exp), pre);
    }

    // Metode untuk menulis respons kesalahan internal server
    private static void writeInternalServerError(PrintWriter writer, String message) {
        writer.print("HTTP/1.1 500 Internal Server Error\r\n");
        writer.print("Content-Type: text/plain\r\n");
        writer.print("\r\n");
        writer.print("Internal Server Error: " + message + "\r\n");
        writer.flush();
    }
}
