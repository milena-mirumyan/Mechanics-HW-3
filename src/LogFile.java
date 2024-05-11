import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFile {
    private String fileName;
    private PrintWriter writer;

    public LogFile(String clashFileName) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        fileName = "timetable_log_" + clashFileName + "_" + timestamp + ".txt";
        writer = new PrintWriter(new FileWriter(fileName));
    }

    public void write(String message) {
        writer.println(message);
        writer.flush(); // Flush after each write operation
    }

    public void close() {
        writer.close(); // Close the writer when done
    }
}
