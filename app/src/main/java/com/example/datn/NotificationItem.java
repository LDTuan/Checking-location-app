package com.example.datn;

public class NotificationItem {
    private String message;
    private String timestamp;
    private String coordinates;

    public NotificationItem(String message, String timestamp, String coordinates) {
        this.message = message;
        this.timestamp = timestamp;
        this.coordinates = coordinates;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCoordinates() {
        return coordinates;
    }
}
