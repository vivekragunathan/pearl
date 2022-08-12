package labs.spring.pearl.kafka;

import labs.spring.pearl.models.AppConfig;
import labs.spring.pearl.models.TopicCfg;
import org.springframework.kafka.retrytopic.DestinationTopic;
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory;
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class TopicNamingProviderFactory implements RetryTopicNamesProviderFactory {
  private final Map<String, TopicCfg> topicCfgMap;

  public TopicNamingProviderFactory(AppConfig config) {
    this.topicCfgMap = buildTopicCfgMap(config);
  }

  private static Map<String, TopicCfg> buildTopicCfgMap(AppConfig config) {
    return new HashMap<>() {{
      put(config.users().kafka().topic(), config.users().kafka());
      put(config.orders().kafka().topic(), config.orders().kafka());
    }};
  }

  @Override
  public RetryTopicNamesProvider createRetryTopicNamesProvider(DestinationTopic.Properties properties) {
    var topicCfgMap = this.topicCfgMap;

    if (properties.isMainEndpoint()) {
      return new SuffixingRetryTopicNamesProvider(properties);
    } else if (properties.isDltTopic()) {
      return new SuffixingRetryTopicNamesProvider(properties) {
        @Override
        public String getTopicName(String topic) {
          return Optional
            .ofNullable(topicCfgMap.get(topic))
            .map(TopicCfg::deadLetterTopic)
            .orElse(super.getTopicName(topic));
        }
      };
    } else {
      return new SuffixingRetryTopicNamesProvider(properties) {
        @Override
        public String getTopicName(String topic) {
          return Optional
            .ofNullable(topicCfgMap.get(topic))
            .map(TopicCfg::retryTopic)
            .orElse(super.getTopicName(topic));
        }
      };
    }
  }
}
