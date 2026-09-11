package com.hms.model;

public enum RoomType {
    GENERAL_WARD("General Ward"),
    PRIVATE("Private Room"),
    ICU("ICU"),
    PSYCHIATRIC_UNIT("Psychiatric Unit"),
    MATERNITY("Maternity"),
    PEDIATRIC_WARD("Pediatric Ward");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
