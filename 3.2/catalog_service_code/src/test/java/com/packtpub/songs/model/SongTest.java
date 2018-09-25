package com.packtpub.songs.model;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class SongTest {

    private static final String SONG_ID = "4bc93d9e-ab07-4d00-b1c8-8e7a7baaa786";
    private static final String AUTHOR_ID = "b10eb15c-4892-4a36-96b3-b445d909921";
    private static final Date DATE = Date.from(Instant.now());
    private static long DURATION_IN_SECONDS = 150;
    private static String ARTIFACT_URI = "s3://a_random_bucket/a_random_file.mp4";

    private static final Song SONG = new Song(
            SONG_ID,
            AUTHOR_ID,
            DATE,
            DURATION_IN_SECONDS,
            ARTIFACT_URI
    );

    private static final String SONG_JSON_FORMAT = "{ " +
            "\"id\": \"%s\", " +
            "\"author_id\": \"%s\", " +
            "\"release_date\": %s, " +
            "\"duration_in_seconds\": %s, " +
            "\"artifact_uri\": \"%s\" " +
            "}";
    private static final String SONG_JSON = String.format(SONG_JSON_FORMAT,
            SONG_ID,
            AUTHOR_ID,
            DATE.getTime(),
            DURATION_IN_SECONDS,
            ARTIFACT_URI);

    private static final String RANDOM_JSON = "{ \"number\": 1 }";

    @Test
    public void testSerialisationWorks() throws JSONException {
        final String json = SONG.toJson();

        JSONAssert.assertEquals(SONG_JSON, json, false);
    }

    @Test
    public void testDeserialisationWorksForCorrectFormat() {
        final Song song = Song.fromJson(SONG_JSON);

        assertEquals(SONG.getId(), song.getId());
        assertEquals(SONG.getAuthorID(), song.getAuthorID());
        assertEquals(SONG.getReleaseDate(), song.getReleaseDate());
        assertEquals(SONG.getArtifactUri(), song.getArtifactUri());
        assertEquals(SONG.getDurationInSeconds(), song.getDurationInSeconds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserialisationFailsForIncorrectFormat() {
        Song.fromJson(RANDOM_JSON);
    }

    @Test
    public void songEqualityIsBasedOnlyOnIdentifier() {
        Song sameSong = new Song(SONG_ID, AUTHOR_ID, DATE, 2, ARTIFACT_URI);

        assertEquals(SONG, sameSong);
    }
}
