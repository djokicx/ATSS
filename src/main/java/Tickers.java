import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class Tickers{
    
    private static Tickers instance = null;
    // find the number of lines in loadTicker and dynamically set it
    private String[] tickers;
    
    private Tickers() throws IOException {
        loadTickers();
    }
    
    private void loadTickers() throws IOException {
        String csvFile = "./OTC.csv";
        
        tickers = new String[countLines(csvFile) + 1];
        BufferedReader br = null;
        String line = "";
        String csvSplit = ",";
        
        try {
            br = new BufferedReader(new FileReader(csvFile));
            int i = 0;
            while ((line = br.readLine()) != null) {
                // comma separation
                String[] ticker = line.split(csvSplit);
                tickers[i++] = ticker[0];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static Tickers getInstance() throws IOException {
        if(instance == null) {
            instance = new Tickers();
        }
        return instance;
    }
    
    private static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }
    
    protected String[] getTickers() {
        return this.tickers;
    }
    
    
}