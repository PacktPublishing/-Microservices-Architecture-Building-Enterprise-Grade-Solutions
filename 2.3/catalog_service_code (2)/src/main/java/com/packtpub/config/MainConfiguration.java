package com.packtpub.config;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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
