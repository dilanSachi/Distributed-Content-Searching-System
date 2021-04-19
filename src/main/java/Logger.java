import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    FileWriter logFileWriter;

    public Logger(String logFilename) {
        try {
            logFileWriter = new FileWriter("./logs/" + logFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logMsg(String log) {
        try {
            logFileWriter.write(log + "\n");
            logFileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
