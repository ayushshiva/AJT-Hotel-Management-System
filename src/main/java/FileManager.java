import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class FileManager {

    public static void save(String data) {
        try {
            FileWriter fw = new FileWriter("receipt.txt", true);
            fw.write(LocalDateTime.now() + " | " + data + "\n");
            fw.close();
        } catch (IOException e) {
            System.out.println("File Error");
        }
    }

    public static String readAll() throws IOException {
        Path receiptFile = Path.of("receipt.txt");
        return Files.exists(receiptFile) ? Files.readString(receiptFile) : "";
    }
}