package labs.spring.pearl.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import labs.spring.pearl.models.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static labs.spring.pearl.utils.Utils.s;

@Component
@SuppressWarnings("unused")
public class UserKafkaListener {
  private static final Logger logger = LoggerFactory.getLogger(UserKafkaListener.class);

  @KafkaListener(topics = "${app.users.kafka.topic}", containerFactory = "listenerContainerFactory")
  public void listenHeartRate(ConsumerRecord<String, String> record) {
    try {
      logger.info("Received user record in Kafka: ({})", record.key());
      processMessage(record.key(), User.fromJson(record.value()));
    } catch (JsonProcessingException e) {
      logger.error("Error deserializing message in topic {}: {}", record.topic(), e.getMessage());
    }
  }

  private void processMessage(String key, User user) {
    logger.info("Processing user({}): {} ...", key, user);

    if (user.age() >= 40) {
      final var msg = s("Encountered error processing user record {0}", key);
      throw new RuntimeException(msg);
    }
  }
}
