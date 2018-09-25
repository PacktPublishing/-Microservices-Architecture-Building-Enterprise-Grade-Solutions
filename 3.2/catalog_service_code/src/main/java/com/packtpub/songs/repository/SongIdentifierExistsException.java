package com.packtpub.songs.repository;

public class SongIdentifierExistsException extends IllegalArgumentException {

    public SongIdentifierExistsException(final String identifier) {
        super("Identifier already exists:" + identifier);
    }
}
