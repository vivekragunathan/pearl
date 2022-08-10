package labs.spring.pearl.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.retrytopic.DestinationTopic;
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory;

public class TopicNamingProviderFactory implements RetryTopicNamesProviderFactory {
  @Override
  public RetryTopicNamesProvider createRetryTopicNamesProvider(DestinationTopic.Properties properties) {
    return null;
  }

  private void handleFetchedRecords(ConsumerRecords<String, String> records) {
    records.partitions().forEach(partition -> {
      var ss = records.records(partition);
    });
  }
}
