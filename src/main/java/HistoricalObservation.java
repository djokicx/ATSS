
public class HistoricalObservation {
    private String from;
    private String to;
    private  double totalDollarValue;
    private double totalPercentageIncrease;
    
    HistoricalObservation() {
        
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getTotalDollarValue() {
        return totalDollarValue;
    }

    public double getTotalPercentageIncrease() {
        return totalPercentageIncrease;
    }

    protected void setFrom(String from) {
        this.from = from;
    }

    protected void setTo(String to) {
        this.to = to;
    }

    protected void setTotalDollarValue(double totalDollarValue) {
        this.totalDollarValue = totalDollarValue;
    }

    protected void setTotalPercentageIncrease(double totalPercentageIncrease) {
        this.totalPercentageIncrease = totalPercentageIncrease;
    }
}
