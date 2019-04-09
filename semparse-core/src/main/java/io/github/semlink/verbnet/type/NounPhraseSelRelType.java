package io.github.semlink.verbnet.type;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public enum NounPhraseSelRelType {

    ABSTRACT,
    ANIMAL,
    ANIMATE,
    BIOTIC,
    BODY_PART,
    COMESTIBLE,
    COMMUNICATION,
    CONCRETE,
    CURRENCY,
    ELONGATED,
    EVENTIVE,
    FORCE,
    GARMENT,
    HUMAN,
    INT_CONTROL,
    LOCATION,
    MACHINE,
    NONRIGID,
    ORGANIZATION,
    PLURAL,
    POINTY,
    REFL,
    REGION,
    SOLID,
    SOUND,
    STATE,
    SUBSTANCE,
    TIME,
    VEHICLE;

    public static Optional<NounPhraseSelRelType> fromString(@NonNull String string) {
        try {
            return Optional.of(valueOf(string.toUpperCase().trim()));
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}