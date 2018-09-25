package com.packtpub.songs;

import com.packtpub.events.PublicationNotifier;
import com.packtpub.songs.model.Song;
import com.packtpub.songs.repository.SongsRepository;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class SongPublicationService {

    private SongsRepository songsRepository;

    private PublicationNotifier publicationNotifier;

    /**
     * Retrieve a published song by the given identifier
     * @param songIdentifier the identifier of the song
     * @return the published song with that identifier, if exists
     *          otherwise an empty optional.
     */
    public Optional<Song> getSong(final String songIdentifier) {
        return songsRepository.getSong(songIdentifier);
    }

    /**
     * Publishes the song, which consists of the following processes:
     * - stores the song in the datastore
     * - publishes an event for the published song
     * @param song the newly published song
     */
    public void publishSong(final Song song) {
        songsRepository.storeSong(song);
        publicationNotifier.onSongPublished(song);
    }

}
