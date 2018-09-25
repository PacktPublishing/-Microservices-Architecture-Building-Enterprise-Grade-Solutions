package com.packtpub.config;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.packtpub.events.PublicationNotifier;
import com.packtpub.monitoring.cloudwatch.CloudwatchMetricsEmitter;
import com.packtpub.songs.SongPublicationService;
import com.packtpub.songs.repository.SongsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@Import(EnvironmentConfiguration.class)
public class MainConfiguration {

    private static final String STAGE_PROPERTY_NAME = "stage";
    private static final String REGION_PROPERTY_NAME = "region";

    private static final String PUBLISHED_SONGS_QUEUE = "published_songs_queue";

    @Autowired
    private ConfigurableEnvironment environment;

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        AmazonDynamoDB dynamoDB = getDynamoDB();

        return new DynamoDBMapper(dynamoDB);
    }

    @Bean
    public SongsRepository songsRepository(final DynamoDBMapper dynamoDBMapper) {
        return new SongsRepository(dynamoDBMapper);
    }

    @Bean
    public SongPublicationService songPublicationService(final PublicationNotifier publicationNotifier,
                                                         final SongsRepository songsRepository) {
        return new SongPublicationService(songsRepository, publicationNotifier);
    }

    @Bean
    public PublicationNotifier publicationNotifier() {
        final String region = environment.getProperty(REGION_PROPERTY_NAME);

        return new PublicationNotifier(
                AmazonSQSClientBuilder.standard().
                        withRegion(region).build(),
                PUBLISHED_SONGS_QUEUE
        );
    }

    @Bean
    public CloudwatchMetricsEmitter metricsEmitter() {
        final String region = environment.getProperty(REGION_PROPERTY_NAME);
        final String stage = environment.getProperty(STAGE_PROPERTY_NAME);

        AmazonCloudWatch cloudwatchClient = AmazonCloudWatchClientBuilder.standard()
                .withRegion(region)
                .build();

        return new CloudwatchMetricsEmitter(cloudwatchClient, stage);
    }

    private AmazonDynamoDB getDynamoDB() {
        final String stage = environment.getProperty(STAGE_PROPERTY_NAME);
        final String region = environment.getProperty(REGION_PROPERTY_NAME);

        switch (stage) {
            case "dev":
                return AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8000", region))
                        .build();
            case "prod":
                return AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build();
            default:
                throw new RuntimeException("Stage defined in properties unknown: " + stage);
        }
    }
}
