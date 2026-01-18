package com.rabin.backend.enums;

public enum InterestCategory {
    MUSIC_CONCERTS("Music & Concerts"),
    ART_SHOWS("Art & Shows"),
    SPORTS("Sports"),
    TECHNOLOGY("Technology"),
    FOOD_DRINK("Food & Drink"),
    TRAVEL("Travel"),
    EDUCATION("Education"),
    OUTDOORS("Outdoors"),
    FITNESS("Fitness"),
    SPIRITUAL("Spiritual");

    private final String displayName;

    InterestCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static InterestCategory fromDisplayName(String displayName) {
        for (InterestCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown interest category: " + displayName);
    }
}
