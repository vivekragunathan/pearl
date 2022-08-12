package labs.spring.pearl.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
public record UsersConfig(
  @ConfigurationProperties(prefix = "app.users.spring")
  String spring,
  @ConfigurationProperties(prefix = "app.users.kafka")
  TopicCfg kafka
){}
