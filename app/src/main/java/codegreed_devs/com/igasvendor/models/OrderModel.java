package codegreed_devs.com.igasvendor.models;

public class OrderModel {

    private String orderId;
    private String gasBrand;
    private String gasSize;
    private String gasType;
    private String numberOfCylinders;
    private String orderStatus;

    public OrderModel(String orderId, String gasBrand, String gasSize, String gasType, String numberOfCylinders, String orderStatus) {
        this.orderId = orderId;
        this.gasBrand = gasBrand;
        this.gasSize = gasSize;
        this.gasType = gasType;
        this.numberOfCylinders = numberOfCylinders;
        this.orderStatus = orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getGasBrand() {
        return gasBrand;
    }

    public String getGasSize() {
        return gasSize;
    }

    public String getGasType() {
        return gasType;
    }

    public String getNumberOfCylinders() {
        return numberOfCylinders;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
