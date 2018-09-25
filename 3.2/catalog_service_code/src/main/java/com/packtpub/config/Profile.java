package com.packtpub.config;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

enum Profile {
    DEVELOPMENT("dev"),
    INTEGRATION("integ"),
    PRODUCTION("prod");

    @Getter
    private String id;

    Profile(final String id) {
        this.id = id;
    }

    public static Profile of(final String name) {
        Optional<Profile> matchedProfile = Arrays.stream(Profile.values())
                .filter(profile -> profile.id.equals(name))
                .findFirst();

        return matchedProfile
                .orElseThrow(() -> new IllegalArgumentException("No profile with name: " + name));
    }

    public static Profile getDefault() {
        return DEVELOPMENT;
    }
}
