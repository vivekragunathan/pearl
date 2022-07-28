package labs.spring.pearl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import labs.spring.pearl.models.User;
import labs.spring.pearl.models.UserKafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static labs.spring.pearl.utils.Utils.*;

@RestController
@SuppressWarnings("unused")
public class PearlRestController {
  private static final Logger logger = LoggerFactory.getLogger(PearlRestController.class);

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final UserKafkaConfig               config;

  public PearlRestController(
    KafkaTemplate<String, String> kafkaTemplate,
    UserKafkaConfig config
  ) {
    this.kafkaTemplate = kafkaTemplate;
    this.config = config;
  }

  @GetMapping("/")
  public String index() {
    return "Hello Spring Boot!";
  }

  @GetMapping("/users/kafka")
  public User publishSampleUserToKafka() throws JsonProcessingException {
    var user = sampleUser();

    kafkaTemplate
      .send(config.topic(), user.id(), toJson(user))
      .addCallback(
        this::whenPublishedToKafkaSuccessful,
        this::whenPublishToKafkaFailed
      );

    kafkaTemplate.flush();

    return user;
  }

  private void whenPublishToKafkaFailed(Throwable ex) {
    if (ex instanceof KafkaProducerException ke) {
      final var user  = ke.getFailedProducerRecord().value();
      logger.info(s("Failed to publish to Kafka: {0}", user), ex);
    } else {
      logger.info("Encountered unknown error when publishing to Kafka", ex);
    }
  }

  private void whenPublishedToKafkaSuccessful(SendResult<String, String> result) {
    try {
      var user = User.fromJson(Objects.requireNonNull(result).getProducerRecord().value());
      logger.info("Published user to Kafka: {}", user);
    } catch (JsonProcessingException ex) {
      logger.error("Error reading producer record from bytes", ex);
    }
  }
}

