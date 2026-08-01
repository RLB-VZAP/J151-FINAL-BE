package com.vzap.trytons.enums;

public enum TournamentStage {
    POOL,
    ROUND_OF_32,
    ROUND_OF_16,
    QUARTER_FINAL,
    SEMI_FINAL,
    THIRD_PLACE,
    FINAL;

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
