package com.packtpub.events;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.packtpub.songs.model.Song;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PublicationNotifier {

    private AmazonSQS sqsClient;

    private String queueName;

    /**
     * Persists a publication event for a newly published song
     * @param newSong the new song that was just published.
     */
    public void onSongPublished(final Song newSong) {
        final SendMessageRequest messageRequest =
                new SendMessageRequest(queueName, newSong.toJson());
        sqsClient.sendMessage(messageRequest);
    }

}
