package com.example.ble_sos;

public final class EmergencySMS {

    private static String sos_message;

    private EmergencySMS() {}

    public static String getEmergencyMessage() {
        return sos_message;
    }

    public static void setEmergencyMessage(String message) {
        sos_message = message;
    }
}
