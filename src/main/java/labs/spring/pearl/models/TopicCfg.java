package labs.spring.pearl.models;

import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
public record TopicCfg (
  String groupId,
  String topic,
  String retryTopic,
  String deadLetterTopic
) {}