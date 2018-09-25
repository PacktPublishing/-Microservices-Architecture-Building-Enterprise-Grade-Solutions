package com.packtpub.controller;

import com.packtpub.grpc.catalog.SongIdentifier;
import com.packtpub.songs.model.Song;
import com.packtpub.songs.repository.SongIdentifierExistsException;
import com.packtpub.songs.repository.SongsRepository;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Date;
import java.util.Optional;

@Controller
public class BaseController extends com.packtpub.grpc.catalog.CatalogServiceGrpc.CatalogServiceImplBase {

    @Autowired
    private SongsRepository songsRepository;

    /**
     * Method used to start the gRPC server.
     * @throws IOException
     */
    public void init() throws IOException {
        Server server = NettyServerBuilder
                .forAddress(new InetSocketAddress("127.0.0.1", 9001))
                .addService(this)
                .build();
        server.start();
    }

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

    /* RPC calls */

    @Override
    public void getSong(final SongIdentifier request,
                        final StreamObserver<com.packtpub.grpc.catalog.Song> responseObserver) {
        final Optional<Song> optionalSong = songsRepository.getSong(request.getId());
        if (!optionalSong.isPresent()) {
            responseObserver.onError(new IllegalArgumentException("No song with identifier: " + request.getId()));
        }

        final Song song = optionalSong.get();
        final com.packtpub.grpc.catalog.Song protoSong = com.packtpub.grpc.catalog.Song.newBuilder()
                .setId(song.getId())
                .setAuthorId(song.getAuthorID())
                .setArtifactUri(song.getArtifactUri())
                .setDurationInSeconds(song.getDurationInSeconds())
                .setReleaseDate(song.getReleaseDate().toInstant().toEpochMilli())
                .build();

        responseObserver.onNext(protoSong);
        responseObserver.onCompleted();
    }

    @Override
    public void publishSong(final com.packtpub.grpc.catalog.Song requestSong,
                            final StreamObserver<com.packtpub.grpc.catalog.Empty> responseObserver) {
        final Song song = new Song(
                requestSong.getId(),
                requestSong.getAuthorId(),
                new Date(requestSong.getReleaseDate()),
                requestSong.getDurationInSeconds(),
                requestSong.getArtifactUri());

        try {
            songsRepository.storeSong(song);

            responseObserver.onCompleted();
        } catch(SongIdentifierExistsException exception) {
            responseObserver.onError(exception);
        }
    }

}