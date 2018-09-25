package catalog.discovery;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearch;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchClientBuilder;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class ApplicationConfig {

    private static final String SEARCH_DOMAIN = "published-songs";

    @Bean
    public AmazonSQS amazonSQS() {
        return AmazonSQSClientBuilder.standard()
                .withRegion(Regions.US_EAST_1.getName())
                .build();
    }

    @Bean
    public AmazonCloudSearchDomain cloudSearchDomain() {
        AmazonCloudSearch cloudSearch = AmazonCloudSearchClientBuilder.standard()
                .withRegion(Regions.US_EAST_1.getName())
                .build();

        DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest();
        describeDomainsRequest.setDomainNames(Collections.singletonList(SEARCH_DOMAIN));
        final String domainEndpoint = cloudSearch.describeDomains(describeDomainsRequest)
                .getDomainStatusList().get(0)
                .getDocService().getEndpoint();

        AmazonCloudSearchDomainClient domainClient = new AmazonCloudSearchDomainClient();
        domainClient.setEndpoint(domainEndpoint);

        return domainClient;
    }

}
