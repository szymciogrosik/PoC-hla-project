package ieee1516e.statistic.service;
import java.io.*;

public class SaveToFileService {
    private final String fileName = "SimulationStatistics.txt";

    public void writeToNewFile(String messageToWrite) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.println(messageToWrite);
        writer.close();
    }

    public void writeToExistingFile(String messageToWrite) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            out.println(messageToWrite);
            out.close();
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }
}
