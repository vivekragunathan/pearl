package labs.spring.pearl.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "app")
public record AppConfig(
  int magicNumber,
  @ConfigurationProperties(prefix = "app.users")
  UsersConfig users,
  @ConfigurationProperties(prefix = "app.orders")
  OrdersConfig orders
) {
}
