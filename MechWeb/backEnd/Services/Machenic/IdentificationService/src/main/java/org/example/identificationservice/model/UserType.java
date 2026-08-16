package org.example.identificationservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserType {
    Mechanic,
    TowTruck;

    @JsonCreator
    public static UserType fromString(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (normalized.contains("TOW")) {
            return TowTruck;
        }
        return Mechanic;
    }
}