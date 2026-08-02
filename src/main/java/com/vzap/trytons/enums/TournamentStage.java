package com.vzap.trytons.enums;

public enum TournamentStage {
    POOL("Pool Stage"),
    ROUND_OF_32("Round of 32"),
    ROUND_OF_16("Round of 16"),
    QUARTER_FINAL("Quarter-Finals"),
    SEMI_FINAL("Semi-Finals"),
    THIRD_PLACE("Bronze Final"),
    FINAL("Final");

    private final String label;

    TournamentStage(String label) {
        this.label = label;
    }

    /**
     * How this stage is named to a manager.
     *
     * <p>The label lived only in the frontend before, which meant every other
     * consumer of the API had to reinvent it. It is the backend's answer now,
     * but note that Jackson serialises an enum by {@code name()}: a DTO that
     * wants the label must carry it as its own string field.
     */
    public String getLabel() {
        return label;
    }

    /**
     * The knockout stage played by a round containing {@code teamsRemaining}
     * managers. Bracket sizes are always powers of two, and never exceed 32:
     * a 100-manager league yields 25 pools of four, so the bracket is the
     * largest power of two not exceeding 50.
     */
    public static TournamentStage forTeamsRemaining(int teamsRemaining) {
        return switch (teamsRemaining) {
            case 32 -> ROUND_OF_32;
            case 16 -> ROUND_OF_16;
            case 8 -> QUARTER_FINAL;
            case 4 -> SEMI_FINAL;
            case 2 -> FINAL;
            default -> throw new IllegalArgumentException(
                    "Unsupported knockout round size: " + teamsRemaining);
        };
    }

    public boolean isKnockout() {
        return this != POOL;
    }
}
