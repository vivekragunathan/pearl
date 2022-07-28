package labs.spring.pearl.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record User(String id, String name, int age) {
  public static User fromJson(String json) throws JsonProcessingException {
    return new ObjectMapper().readValue(json, User.class);
  }
}
