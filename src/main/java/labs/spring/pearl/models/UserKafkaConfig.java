package labs.spring.pearl.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "app.users")
public record UserKafkaConfig(String topic, String groupId) { }
