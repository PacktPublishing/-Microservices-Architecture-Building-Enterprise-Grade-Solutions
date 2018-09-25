package com.packtpub.songs.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.packtpub.songs.model.Song;
import com.packtpub.songs.repository.dynamodb.DynamoDBSongItem;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SongsRepositoryTest {

    private static final String SONG_ID = "a_random_song_id";
    private static final String AUTHOR_ID = "a_random_author_id";
    private static final long SONG_RELEASE_DATE = 1000_000;
    private static final long SONG_DURATION_IN_SECONDS = 360;
    private static final String SONG_ARTIFACT_URI = "s3://bucket/recording.mp4";

    private static final DynamoDBSongItem SONG_ITEM = new DynamoDBSongItem();

    private SongsRepository songsRepository;

    private final DynamoDBMapper mockDynamoDBMapper = mock(DynamoDBMapper.class);

    @Before
    public void setup() {
        this.songsRepository = new SongsRepository(mockDynamoDBMapper);

        SONG_ITEM.setId(SONG_ID);
        SONG_ITEM.setAuthorID(AUTHOR_ID);
        SONG_ITEM.setReleaseDateInEpochMillis(SONG_RELEASE_DATE);
        SONG_ITEM.setDurationInSeconds(SONG_DURATION_IN_SECONDS);
        SONG_ITEM.setArtifactUri(SONG_ARTIFACT_URI);
    }

    @Test
    public void whenSongExistsInDynamo_RepositoryReturnsIt() {
        when(mockDynamoDBMapper.load(DynamoDBSongItem.class, SONG_ID))
                .thenReturn(SONG_ITEM);

        Optional<Song> optionalSong = songsRepository.getSong(SONG_ID);

        assertTrue(optionalSong.isPresent());
        Song song = optionalSong.get();
        assertEquals(SONG_ID, song.getId());
        assertEquals(AUTHOR_ID, song.getAuthorID());
        assertEquals(SONG_RELEASE_DATE, song.getReleaseDate().toInstant().toEpochMilli());
        assertEquals(SONG_DURATION_IN_SECONDS, song.getDurationInSeconds());
        assertEquals(SONG_ARTIFACT_URI, song.getArtifactUri());
    }

    @Test
    public void whenSongDoesNotExistInDynamo_RepositoryReturnsEmptyOptional() {
        when(mockDynamoDBMapper.load(DynamoDBSongItem.class, SONG_ID))
                .thenReturn(null);

        Optional<Song> optionalSong = songsRepository.getSong(SONG_ID);

        assertFalse(optionalSong.isPresent());
    }

    @Test
    public void whenNoOtherSongExistsWithSameId_RepositoryStoresSuccessfullyTheSong() {
        Song songToStore = new Song(SONG_ID, AUTHOR_ID, new Date(SONG_RELEASE_DATE), SONG_DURATION_IN_SECONDS, SONG_ARTIFACT_URI);

        songsRepository.storeSong(songToStore);
    }

    @Test(expected = SongIdentifierExistsException.class)
    public void whenOtherSongExistsWithSameId_RepositoryThrowsException() {
        Song songToStore = new Song(SONG_ID, AUTHOR_ID, new Date(SONG_RELEASE_DATE), SONG_DURATION_IN_SECONDS, SONG_ARTIFACT_URI);

        doThrow(ConditionalCheckFailedException.class)
                .when(mockDynamoDBMapper).save(any(DynamoDBSongItem.class), any(DynamoDBSaveExpression.class));

        songsRepository.storeSong(songToStore);
    }

}
