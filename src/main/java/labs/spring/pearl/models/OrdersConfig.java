package labs.spring.pearl.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
public record OrdersConfig(
  @ConfigurationProperties(prefix = "app.orders.hello")
  String hello,
  @ConfigurationProperties(prefix = "app.orders.kafka")
  TopicCfg kafka
){}
