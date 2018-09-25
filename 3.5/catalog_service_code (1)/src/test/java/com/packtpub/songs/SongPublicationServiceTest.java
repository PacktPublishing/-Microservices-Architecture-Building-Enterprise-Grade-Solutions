package com.packtpub.songs;

import com.packtpub.events.PublicationNotifier;
import com.packtpub.songs.model.Song;
import com.packtpub.songs.repository.SongIdentifierExistsException;
import com.packtpub.songs.repository.SongsRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SongPublicationServiceTest {

    private static final Song SONG = new Song("id", "author-id", Date.from(Instant.now()), 100, "http://site.com/song.mp4");

    @Mock
    private SongsRepository songsRepository;
    @Mock
    private PublicationNotifier publicationNotifier;

    private SongPublicationService songPublicationService;

    @Before
    public void setup() {
        songPublicationService = new SongPublicationService(songsRepository, publicationNotifier);
    }

    @Test
    public void testGetSongReturnsSongReturnedByRepository() {
        when(songsRepository.getSong("id"))
                .thenReturn(Optional.of(SONG));

        final Optional<Song> optionalSong = songPublicationService.getSong("id");

        assertTrue(optionalSong.isPresent());
        assertEquals(SONG, optionalSong.get());
    }

    @Test
    public void testGetSongReturnsEmptyOptional_WhenRepositoryReturnsNoSong() {
        when(songsRepository.getSong(any()))
                .thenReturn(Optional.empty());

        final Optional<Song> optionalSong = songPublicationService.getSong("random-id");

        assertFalse(optionalSong.isPresent());
    }

    @Test
    public void testPublishSongStoresSongInRepositoryAndPublishesEvent() {
        songPublicationService.publishSong(SONG);

        verify(songsRepository).storeSong(SONG);
        verify(publicationNotifier).onSongPublished(SONG);
    }

    @Test(expected = SongIdentifierExistsException.class)
    public void whenAnotherSongExistsWithSameId_ExceptionIsThrownAndNoEventPublished() {
        doThrow(new SongIdentifierExistsException("id"))
                .when(songsRepository).storeSong(SONG);

        songPublicationService.publishSong(SONG);

        verifyZeroInteractions(publicationNotifier);
    }

}
