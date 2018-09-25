package catalog.discovery;

import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchdomain.model.ContentType;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class Application {

    private static final String BATCH_OPERATION_TEMPLATE = "[{\"type\": \"add\", \"id\": \"%s\", \"fields\": %s}]";

    private static final int PARALLEL_WORKER_THREADS = 10;

    private static final int LONG_POLLING_INTERVAL_SECONDS = 10;

    private static final String QUEUE_NAME = "published_songs_queue";
    
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        AmazonSQS sqs = ctx.getBean(AmazonSQS.class);
        AmazonCloudSearchDomainClient cloudSearchDomainClient = ctx.getBean(AmazonCloudSearchDomainClient.class);
        ExecutorService executorService = Executors.newFixedThreadPool(PARALLEL_WORKER_THREADS);

        final String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();


        executorService.submit(() -> {
            while (true) {
                consumeMessagesFromQueue(queueUrl, sqs, cloudSearchDomainClient);
            }
        });
    }

    private static void consumeMessagesFromQueue(final String queueUrl, final AmazonSQS sqs, final AmazonCloudSearchDomainClient cloudSearchDomainClient){
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withWaitTimeSeconds(LONG_POLLING_INTERVAL_SECONDS);
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        System.out.println("Messages received: " + messages.size());

        messages.forEach(message -> {
            try {
                System.out.println("Message: " + message.getBody());
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<HashMap<String, String>> typeRef
                        = new TypeReference<HashMap<String, String>>() {};
                Map<String, String> map = mapper.readValue(message.getBody(), typeRef);
                final String songID = map.get("id");

                final String documentsContent = String.format(BATCH_OPERATION_TEMPLATE, songID, message.getBody());
                System.out.println("Batch: " + documentsContent);
                UploadDocumentsRequest request = new UploadDocumentsRequest();
                request.setContentType(ContentType.Applicationjson);
                request.setDocuments(new StringInputStream(documentsContent));
                request.setContentLength((long) documentsContent.getBytes().length);

                cloudSearchDomainClient.uploadDocuments(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));
        });
    }

}
