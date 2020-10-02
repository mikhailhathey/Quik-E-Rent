package project.quikERent.models;

import java.util.Date;

public class RentedPowerToolModel {

    private Long powerToolId;
    private String userId;
    private Date rentedDate;

    public RentedPowerToolModel() {
    }

    public RentedPowerToolModel(Long powerToolId, String userId, Date rentedDate) {
        this.powerToolId = powerToolId;
        this.userId = userId;
        this.rentedDate = rentedDate;
    }

    public Long getPowerToolId() {
        return powerToolId;
    }

    public void setPowerToolId(Long powerToolId) {
        this.powerToolId = powerToolId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getRentedDate() {
        return rentedDate;
    }

    public void setRentedDate(Date rentedDate) {
        this.rentedDate = rentedDate;
    }
}

