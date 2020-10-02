package project.quikERent.models;

import java.util.Date;

public class ConfirmationPowerToolModel {

    private Long powerToolId;
    private String userId;
    private ConfirmationType type;
    private Date datetime;

    public ConfirmationPowerToolModel() {
    }

    public ConfirmationPowerToolModel(Long powerToolId, String userId, ConfirmationType type, Date datetime) {
        this.powerToolId = powerToolId;
        this.userId = userId;
        this.type = type;
        this.datetime = datetime;
    }

    public void setPowerToolId(Long powerToolId) {
        this.powerToolId = powerToolId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(ConfirmationType type) {
        this.type = type;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public Long getPowerToolId() {
        return powerToolId;
    }

    public String getUserId() {
        return userId;
    }

    public ConfirmationType getType() {
        return type;
    }

    public Date getDatetime() {
        return datetime;
    }
}
