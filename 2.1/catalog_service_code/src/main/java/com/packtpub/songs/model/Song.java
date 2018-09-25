package com.packtpub.songs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
public class Song {

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String id;

    @JsonProperty("author_id")
    private String authorID;

    @JsonProperty("release_date")
    private Date releaseDate;

    @JsonProperty("duration_in_seconds")
    private long durationInSeconds;

    @JsonProperty("artifact_uri")
    private String artifactUri;

    /**
     * De-serialises a song from a json payload
     * @param json the json payload to be deserialised
     * @return the equivalent song object
     *
     * Note: method throws an IllegalArgumentException, if the json was not of the right format
     */
    public static Song fromJson(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Song.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new IllegalArgumentException("Song de-serialisation failed: " + json, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serialises a song to a json payload
     * @return the equivalent json string
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
