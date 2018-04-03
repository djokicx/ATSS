import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

public class Scraper {
//    private static final Logger logger = LogManager.getLogger(Scraper.class);
    
    Tickers tickers;
    
    public Scraper() throws IOException {
        this.tickers = Tickers.getInstance();
    }
    
    
    public Stock getPastThree(String ticker) throws IOException {
        Map<String, Calendar> dates = getDates();
        return YahooFinance.get(ticker, dates.get("from"), dates.get("to"), Interval.DAILY);
    }
    
    public Stock getHistoricDate(String ticker, Calendar to) throws IOException {
        Map<String, Calendar> dates = getDatesHistoric(to);
        return YahooFinance.get(ticker, dates.get("from"), dates.get("to"), Interval.DAILY);
    }
    
    public Stock getHistoricYears(String ticker, int fromYear, int toYear) throws IOException {
        Calendar from = new GregorianCalendar(fromYear - 1, 11, 30);
        Calendar to = new GregorianCalendar(toYear, 11, 31);
        
        return YahooFinance.get(ticker, from, to, Interval.DAILY);
    }
    
    public Map<String, Stock> getAll() throws IOException, InterruptedException, ExecutionException {
        Map<String, Calendar> dates = getDates();
        return runMulti(dates);
    }    
    
    public Map<String, Stock> getAllHistoric(int fromYear, int toYear) throws IOException, InterruptedException, ExecutionException {
        Map<String, Calendar> dates = new HashMap<String, Calendar>();
        dates.put("from", new GregorianCalendar(fromYear - 1, 11, 30));
        dates.put("to", new GregorianCalendar(toYear, 11, 31));
        
        return runMulti(dates);
    }
    
    /* MultiThreading */
    
    private Map<String, Stock> runMulti(Map<String, Calendar> dates) throws IOException, InterruptedException, ExecutionException {
        Map<String, Stock> mappings = new HashMap<String, Stock>();  
        
        Collection<Callable<PingResult>> tasks = new ArrayList<>();
        for(String ticker : Tickers.getInstance().getTickers()) {
            tasks.add(new Task(ticker, dates.get("from"), dates.get("to")));
        }
        int numThreads = Tickers.getInstance().getTickers().length > 3 ? Runtime.getRuntime().availableProcessors() : Tickers.getInstance().getTickers().length; //max 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Future<PingResult>> results = executor.invokeAll(tasks);
        for(Future<PingResult> result : results){
            PingResult pingResult = result.get();
            mappings.put(pingResult.ticker, pingResult.stock);
          }
        executor.shutdown(); //always reclaim resources
        return mappings;
    }
    
    private final class Task implements Callable<PingResult> {
        private final String ticker;
        private final Calendar from;
        private final Calendar to;
        
        Task(String ticker, Calendar from, Calendar to) {
            this.ticker = ticker;
            this.from = from;
            this.to = to;
        }

        @Override
        public PingResult call() throws Exception {
            PingResult result = new PingResult();
            try {
                Stock stock = YahooFinance.get(ticker, from, to, Interval.DAILY);
                result.ticker = stock.getSymbol();
                result.stock = stock;
            } catch (Exception e) {
//                logger.warn("failed to retrieve ticker: " + ticker);
            }
            return result;
        }
    }
    
    private static final class PingResult {
        String ticker;
        Stock stock;
      }
    
    /* Helper methods */
    private Map<String, Calendar> getDates() {
        return getDatesHistoric(null);
    }
    
    private Map<String, Calendar> getDatesHistoric(Calendar date) {
        Calendar to = date == null ? Calendar.getInstance() : date;
        
        if (isWeekend(to)) {
            to = workingDaysBack(to, 1);
        }
        else {
            if (date == null) {
                Calendar cutOff = Calendar.getInstance();
                cutOff.set(Calendar.HOUR_OF_DAY, 13);
                cutOff.set(Calendar.MINUTE, 30);
                Instant now = Instant.now();
                /* If market's open */
                if (now.isBefore(cutOff.toInstant())) {
                    to = workingDaysBack(to, 1);        
                }
            }
        }
        
        // if current day is weekend, get the first previous working day
        
        
        Calendar from = (Calendar) to.clone();
        from = workingDaysBack(from, 2);
        
        Map<String, Calendar> mappings = new HashMap<String, Calendar>();
        mappings.put("to", to);
        mappings.put("from", from);
        
        return mappings;
    }
    
    private Calendar workingDaysBack(final Calendar from, final int count) {
        for (int daysBack = 0; daysBack < count; ++daysBack) {
            do {
                from.add(Calendar.DAY_OF_YEAR, -1);
            } while(isWeekend(from));
        }
        return from;
    }

    private boolean isWeekend(Calendar cal) {
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
               cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }
    
    public static void write (String filename, List<String>x) throws IOException{
        BufferedWriter outputWriter = null;
        // for appending
//        outputWriter = new BufferedWriter(new FileWriter(filename, true));
        outputWriter = new BufferedWriter(new FileWriter(filename, true));
        for (String val: x) {
            outputWriter.write(val + ',');
            outputWriter.newLine();
        }
        outputWriter.flush();  
        outputWriter.close();  
      }
}
