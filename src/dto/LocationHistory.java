package dto;

public class LocationHistory {
    private int id;
    private double xCoordinate;
    private double yCoordinate;
    private String queryDate;

    public LocationHistory(int id, double xCoordinate, double yCoordinate, String queryDate) {
        this.id = id;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.queryDate = queryDate;
    }

    public int getId() {
        return id;
    }

    public double getXCoordinate() {
        return xCoordinate;
    }

    public double getYCoordinate() {
        return yCoordinate;
    }

    public String getQueryDate() {
        return queryDate;
    }
}
