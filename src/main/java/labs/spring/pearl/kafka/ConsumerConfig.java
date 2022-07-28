package labs.spring.pearl.kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Configuration
@SuppressWarnings("unused")
public class ConsumerConfig {
  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;

  @Value(value = "${spring.kafka.consumer.group-id}")
  private String consumerGroupId;

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(
      new HashMap<>() {{
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        put(GROUP_ID_CONFIG, consumerGroupId);
        put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      }}
    );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

  @Bean
  public RetryTopicConfiguration retryTopicConfig(
    KafkaProperties props,
    ConcurrentKafkaListenerContainerFactory<String, String> factory,
    KafkaTemplate<String, String> template
  ) {
    var cfg     = props.getRetry().getTopic();
    var backoff = new FixedBackOff(cfg.getDelay().toMillis(), cfg.getAttempts());

    return RetryTopicConfigurationBuilder
      .newInstance()
      .fixedBackOff(backoff.getInterval())
      .maxAttempts(Math.toIntExact(backoff.getMaxAttempts()))
      .useSingleTopicForFixedDelays()
      .retryTopicSuffix(".retry")
      .dltSuffix(".dlt")
      .doNotRetryOnDltFailure()
      .listenerFactory(factory)
      .create(template);
  }
}
