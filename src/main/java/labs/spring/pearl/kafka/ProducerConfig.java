package labs.spring.pearl.kafka;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;

import static org.apache.kafka.clients.CommonClientConfigs.*;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@Configuration
@SuppressWarnings("unused")
public class ProducerConfig {
  @Bean
  public KafkaTemplate<String, String> kafkaTemplate(KafkaProperties props) {
    return new KafkaTemplate<>(producerFactory(props));
  }

  @Bean
  public ProducerFactory<String, String> producerFactory(KafkaProperties props) {
    return new DefaultKafkaProducerFactory<>(
      new HashMap<>() {{
        put(BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      }}
    );
  }
}
