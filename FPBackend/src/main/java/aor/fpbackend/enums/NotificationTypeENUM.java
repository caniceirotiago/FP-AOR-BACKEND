package aor.fpbackend.enums;

public enum NotificationTypeENUM {
    INDIVIDUAL_MESSAGE(0),
    GROUP_MESSAGE(1),
    PROJECT(3);



    private final int intValue;


    NotificationTypeENUM(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static NotificationTypeENUM fromInt(int intValue) {
        for (NotificationTypeENUM type : NotificationTypeENUM.values()) {
            if (type.getIntValue() == intValue) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid integer value for NotificationTypeENUM: " + intValue);
    }
}