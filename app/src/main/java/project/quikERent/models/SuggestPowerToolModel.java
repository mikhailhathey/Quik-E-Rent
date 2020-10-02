package project.quikERent.models;


public class SuggestPowerToolModel {

    private Long id;
    private String brand;
    private String powerToolName;
    private Integer year;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPowerToolName() {
        return powerToolName;
    }

    public void setPowerToolName(String powerToolName) {
        this.powerToolName = powerToolName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public SuggestPowerToolModel(String brand, String powerToolName, Integer year) {
        this.brand = brand;
        this.powerToolName = powerToolName;
        this.year = year;
    }

    public SuggestPowerToolModel(Long id, String brand, String powerToolName, Integer year) {
        this.id = id;
        this.brand = brand;
        this.powerToolName = powerToolName;
        this.year = year;
    }

    @Override
    public String toString() {
        return brand + " \"" + powerToolName + "\" " + year;
    }
}
