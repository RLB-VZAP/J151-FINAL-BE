package com.vzap.trytons.service.catalog.feed;

import java.util.Map;
import java.util.Optional;

/**
 * Curated lookup from the external feed's own {@code clubId}/{@code positionId}
 * UUIDs to our catalog's club/position <em>names</em>.
 * <p>
 * Why names and not our UUIDs: our {@code club}/{@code position} primary keys are
 * generated with random {@code UUID()} in the seed, so they differ on every reseed
 * and per environment. Mapping to a stable name and resolving that name to the live
 * row at import time keeps this table valid across reseeds. The feed exposes no
 * {@code /clubs} or {@code /positions} endpoint, so these UUIDs cannot be resolved
 * at runtime - they were identified once from the players each UUID groups
 * (all real United Rugby Championship squads, 2024/25).
 * <p>
 * Feed granularity vs ours:
 * <ul>
 *   <li>16 feed clubs = the 16 URC teams (our seed also has Cheetahs and Pumas,
 *       which simply receive no feed players).</li>
 *   <li>11 feed position buckets fold into our 9 positions: the feed splits the
 *       back row (flankers + number 8s) and has one 2-player miscategorised bucket;
 *       all three collapse to "Loose Forward".</li>
 * </ul>
 * Any feed UUID absent from these maps is reported as unmapped and its players are
 * skipped rather than guessed.
 */
public final class ExternalCatalogMapping {

    private ExternalCatalogMapping() {
    }

    /** External feed clubId -> our club.clubName. Sample players shown for auditing. */
    private static final Map<String, String> CLUB_NAME_BY_FEED_ID = Map.ofEntries(
            Map.entry("ae895410-6b76-5426-8c28-ebf6b7511199", "Lions"),            // Henco van Wyk, Quan Horn, Ntlabakanye
            Map.entry("5691bf5e-d903-5913-92d5-0dcae25902fa", "Bulls"),            // Ruan Nortje, Embrose Papier, Grobbelaar
            Map.entry("2f870857-44a1-520c-a00c-145c0877c717", "Sharks"),          // Esterhuizen, Buthelezi, Tshituka
            Map.entry("48fc16a1-43f3-5a77-98c8-c335cbc45605", "Stormers"),        // Feinberg-Mngomezulu, Evan Roos, Willemse
            Map.entry("c2750f3e-02a9-5f81-aab7-994ed568a5bc", "Leinster"),        // Jimmy O'Brien, Joe McCarthy, Sam Prendergast
            Map.entry("be49ee20-4d46-5f45-8d01-50adaee4f46f", "Munster"),         // Tom Farrell, Gavin Coombes, Shane Daly
            Map.entry("d511e017-3a61-5ed5-b6dd-2e012762857e", "Ulster"),          // Tom Stewart, Nick Timoney, Nathan Doak
            Map.entry("b01ef4e2-1d70-575d-b497-c5e4efce888a", "Connacht"),        // Cian Prendergast, Cathal Forde, Sean Jansen
            Map.entry("9ab0f7fa-980a-53fa-a560-c217dc694ae9", "Cardiff"),         // Cam Winnett, Ben Thomas, Callum Sheedy
            Map.entry("c8fd6cc4-d90d-5171-bfc8-892935e2cb92", "Dragons"),         // Aaron Wainwright, Rio Dyer, Aneurin Owen
            Map.entry("e5a81818-a6d6-5fa7-95ea-e3aef64b4ab5", "Ospreys"),         // Jack Walsh, Keiran Williams, Dan Edwards
            Map.entry("90f7e37f-160a-59df-977e-5c8098174faf", "Scarlets"),        // Tom Rogers, Blair Murray, Taine Plumtree
            Map.entry("e0d51d30-b10d-54da-8790-91e0a5a52373", "Glasgow Warriors"),// McDowall, Tuipulotu, George Horne
            Map.entry("9339207b-9502-5dfa-83a1-8882b974d25a", "Edinburgh"),       // Darcy Graham, Wes Goosen, Ewan Ashman
            Map.entry("5ebc8df8-46c6-579f-8220-9c4c1a1c8d42", "Benetton"),        // Menoncello, Louis Lynagh, Paolo Odogwu
            Map.entry("8ca9f924-9455-5470-87b1-ec6cca75c18f", "Zebre Parma")      // Bertaccini, Simone Gesi, Montemauri
    );

    /** External feed positionId -> our position.positionName. Sample players shown for auditing. */
    private static final Map<String, String> POSITION_NAME_BY_FEED_ID = Map.ofEntries(
            Map.entry("5046ec94-65c3-52eb-b54b-e57a002388ae", "Prop"),         // Ntlabakanye, Angus Bell, Thomas Gallo
            Map.entry("40e24955-23f9-515e-8aae-75fbaca1cf47", "Hooker"),       // Tom Stewart, Ewan Ashman, Grobbelaar
            Map.entry("38062c51-113f-558f-adfc-07b4ca5c87e4", "Lock"),         // Joe McCarthy, Ruan Nortje, Ben Carter
            Map.entry("e033e4c4-0dae-5fb3-93c4-05ee5e4ace0e", "Loose Forward"),// Aaron Wainwright, Nick Timoney (flankers)
            Map.entry("86612e7b-b360-5c26-b7c0-c44b7e27d617", "Loose Forward"),// Evan Roos, Caelan Doris, Gavin Coombes (no. 8s)
            Map.entry("3ee11033-e20a-56c9-a6a4-8fb38a78b2b7", "Loose Forward"),// Todd Lawlor, Evan Rees (2-player edge bucket)
            Map.entry("68256872-ea08-5935-a3a8-2e5336cca870", "Scrum Half"),   // Embrose Papier, Craig Casey, Jamie Dobie
            Map.entry("01bd0f01-758d-545f-94ef-4adaf530144e", "Fly Half"),     // Feinberg-Mngomezulu, Sam Prendergast, Jack Walsh
            Map.entry("10c50b38-583e-59d5-83da-98dd93a1a8b1", "Centre"),       // Henco van Wyk, Sione Tuipulotu, Nankivell
            Map.entry("71405551-7e54-5337-8b29-62e71d436aee", "Wing"),         // Darcy Graham, Sebastian de Klerk, Zac Ward
            Map.entry("280aa066-4b25-57db-9a00-56d5205e2e9c", "Fullback")      // Quan Horn, Cam Winnett, Matt Gallagher
    );

    /** Our club name for a feed clubId, or empty if the UUID is not mapped. */
    public static Optional<String> clubName(String feedClubId) {
        return Optional.ofNullable(CLUB_NAME_BY_FEED_ID.get(feedClubId));
    }

    /** Our position name for a feed positionId, or empty if the UUID is not mapped. */
    public static Optional<String> positionName(String feedPositionId) {
        return Optional.ofNullable(POSITION_NAME_BY_FEED_ID.get(feedPositionId));
    }
}
