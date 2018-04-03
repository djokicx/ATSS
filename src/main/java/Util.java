import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class Util {
    
    public static void write(String fileName, int numAnalyzedStock, long testTime, int numFilteredStock, String filtered) throws IOException {
        
        // for appending
//        outputWriter = new BufferedWriter(new FileWriter(filename, true));
        Date date = Date.from(Instant.now());
        SimpleDateFormat formatter = new SimpleDateFormat("MMM-d-");
        String formattedDate = formatter.format(date);
        
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(formattedDate + fileName));
        outputWriter.write("test time elapsed = " + ((testTime/1000.0)/60.0) + "s");
        outputWriter.newLine();
        outputWriter.write("# of analyzed stocks = " + numAnalyzedStock);
        outputWriter.newLine();
        outputWriter.write("# of passing tickers = " + numFilteredStock);
        outputWriter.newLine();
        outputWriter.newLine();
        outputWriter.write("Constraints:");
        outputWriter.write("percentage threshold per day = " + Screener.PERCENTAGE_INCREASE_THRESHOLD);
        outputWriter.newLine();
        outputWriter.write("dollar volume per day = " + Screener.DOLLAR_VOLUME_THRESHOLD);
        outputWriter.newLine();
        outputWriter.newLine();
        outputWriter.write(filtered);
        
        
        outputWriter.flush();  
        outputWriter.close();  
    }
}
