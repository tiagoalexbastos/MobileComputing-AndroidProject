package ua.cm.tiagoalexbastos.imgreader.ImageUtils;

import java.io.Serializable;

public class Image implements Serializable {
    private String name;
    private String small, medium, large;
    private String timestamp;

    public Image() {
    }

    @SuppressWarnings("unused")
    public Image(String name, String small, String medium, String large, String timestamp) {
        this.name = name;
        this.small = small;
        this.medium = medium;
        this.large = large;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public String getSmall() {
        return small;
    }

    @SuppressWarnings("unused")
    public void setSmall(String small) {
        this.small = small;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unused")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
