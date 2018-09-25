package com.packtpub.controller;

import com.packtpub.songs.model.Song;
import com.packtpub.songs.repository.SongIdentifierExistsException;
import com.packtpub.songs.repository.SongsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class BaseController {

    @Autowired
    private SongsRepository songsRepository;

    @RequestMapping(value = "songs/{song_id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSong(@PathVariable("song_id") String songIdentifier, ModelMap model) {
        final Optional<Song> song = songsRepository.getSong(songIdentifier);
        if (!song.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        final String jsonResponse = song.get().toJson();
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    @RequestMapping(value = {"/songs"},
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> publishSong(@RequestBody String jsonPayload) {
        try {
            final Song song = Song.fromJson(jsonPayload);
            songsRepository.storeSong(song);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch(SongIdentifierExistsException e) {
            final String jsonMessage = String.format("{ message: \"%s\" }", e.getMessage());
            return new ResponseEntity<>(jsonMessage, HttpStatus.BAD_REQUEST);
        }
    }
}