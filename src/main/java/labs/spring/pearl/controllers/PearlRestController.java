package labs.spring.pearl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import labs.spring.pearl.models.AppConfig;
import labs.spring.pearl.models.OrdersConfig;
import labs.spring.pearl.models.User;
import labs.spring.pearl.models.UsersConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static labs.spring.pearl.utils.Utils.*;

@RestController
@SuppressWarnings("unused")
public class PearlRestController {
  private static final Logger logger = LoggerFactory.getLogger(PearlRestController.class);

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final AppConfig                     config;
  private final UsersConfig                   usersCfg;
  private final OrdersConfig                  ordersCfg;

  public PearlRestController(KafkaTemplate<String, String> kafkaTemplate, AppConfig config) {
    this.kafkaTemplate = kafkaTemplate;
    this.config = config;
    this.usersCfg = config.users();
    this.ordersCfg = config.orders();
  }

  @GetMapping("/")
  public String index() {
    return "Hello Spring Boot!";
  }

  @GetMapping("/configs")
  public List<Object> configs() {
    return Arrays.asList(usersCfg, ordersCfg, config);
  }

  @GetMapping("/users/kafka")
  public User publishSampleUserToKafka(
    @RequestHeader(value = "age", required = false, defaultValue = "0")
    int ageOverride
  ) throws JsonProcessingException {
    var user = sampleUser(ageOverride);

    kafkaTemplate
      .send(usersCfg.kafka().topic(), user.id(), toJson(user))
      .addCallback(
        this::whenPublishedToKafkaSuccessful,
        this::whenPublishToKafkaFailed
      );

    kafkaTemplate.flush();

    return user;
  }

  private void whenPublishToKafkaFailed(Throwable ex) {
    if (ex instanceof KafkaProducerException ke) {
      final var user = ke.getFailedProducerRecord().value();
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

