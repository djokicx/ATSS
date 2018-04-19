import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

public class Screener {
    static final double PERCENTAGE_INCREASE_THRESHOLD = 10;
    static final double DOLLAR_VOLUME_THRESHOLD = 1000000;
    static final SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
    
    public static boolean isPositiveStrict(HistoricalQuote quote) {
        return isPositiveDay(quote) && hasDollarVolume(quote);
    }
    
    protected static boolean isPositiveDay(HistoricalQuote quote) {
        try {
            return quote.getClose().compareTo(quote.getOpen()) > 0;
        } catch (NullPointerException e) { 
            return false; 
        }
    }
    
    protected static boolean isThresholdPositive(HistoricalQuote quote) {
        try {
            return (((quote.getClose().doubleValue() - quote.getOpen().doubleValue()) / quote.getOpen().doubleValue()) * 100) > PERCENTAGE_INCREASE_THRESHOLD;
        } catch (NullPointerException e) { 
            return false; 
        }
   }
    
    protected static boolean hasDollarVolume(HistoricalQuote quote) {
        try {
            return (quote.getClose().doubleValue() * quote.getVolume()) > DOLLAR_VOLUME_THRESHOLD;
        } catch (NullPointerException e) { 
            return false; 
        }
    }
    
    protected static boolean positiveSequence(List<HistoricalQuote> quotes) {
        try {
            return quotes.stream().mapToDouble(quote -> (((quote.getClose().doubleValue() - quote.getOpen().doubleValue()) / quote.getOpen().doubleValue()) * 100)).sum() > PERCENTAGE_INCREASE_THRESHOLD ||
                    quotes.stream().allMatch(Screener::isThresholdPositive);
        } catch (NullPointerException e) { 
            return false; 
        }
    }
    
    public static List<Object> filterStockDump(Map<String, Stock> dump) {
        if(dump.containsKey(null)) dump.remove(null);
        List<Object> list = dump.keySet().parallelStream()
                .filter(key -> {
                    try {
                        return dump.get(key).getHistory()
                                .stream()
                                .allMatch(Screener::isPositiveStrict) &&
                                positiveSequence(dump.get(key).getHistory());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return false;
                }).collect(Collectors.toList());
    
    return list;
    }
    
    public static boolean isWinner(Stock stock) throws IOException {
        return isWinningSequence(stock.getHistory());
    }
    
    public static Map<String, List<HistoricalObservation>> getHistoricalWinnersAll(Map<String, Stock> stocks) throws IOException {
        Map<String, List<HistoricalObservation>> mappings = new HashMap<String, List<HistoricalObservation>>();
        for (String key : stocks.keySet()) {
            List<HistoricalObservation> historicalWinners = getHistoricalWinners(stocks.get(key));
            if(!historicalWinners.isEmpty()) {
                mappings.put(key, historicalWinners); 
            }
        }
        
        return mappings;
    }
    
    
    /* Helper Task */
    private static List<HistoricalObservation> getHistoricalWinners(Stock stock) throws IOException {
        List<HistoricalObservation> observations = new ArrayList<HistoricalObservation>();
        for (int i = stock.getHistory().size() - 1; i >= 2; i--) {
            List<HistoricalQuote> tempChunk = stock.getHistory().subList(i - 2, i + 1);
            if(isWinningSequence(tempChunk)) {
                HistoricalObservation observ = new HistoricalObservation();
                /* Get dates */
                observ.setFrom(format1.format(tempChunk.get(0).getDate().getTime()));
                observ.setTo(format1.format(tempChunk.get(tempChunk.size() - 1).getDate().getTime()));
                /* Calculate Dollar Value and Overall Percentage increase */
                observ.setTotalDollarValue(tempChunk.stream().mapToDouble(hs -> hs.getClose().doubleValue() * hs.getVolume().doubleValue()).sum());
                observ.setTotalPercentageIncrease(tempChunk.stream().mapToDouble(hs -> ((hs.getClose().doubleValue() - hs.getOpen().doubleValue()) / hs.getOpen().doubleValue()) * 100).sum());
                
                observations.add(observ);
            }
        }
        
        return observations;
    }
    
    public static boolean isWinningSequence(List<HistoricalQuote> quotes) {
        return quotes.stream().allMatch(Screener::isPositiveStrict) && positiveSequence(quotes);
    }
    
}
