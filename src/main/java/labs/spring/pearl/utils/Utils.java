package labs.spring.pearl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import labs.spring.pearl.models.User;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {
  private static final Random random     = new Random(32767);
  private static final String alphaChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  public static void println(String spec, Object... args) {
    System.out.println(s(spec, args));
  }

  public static String s(String spec, Object... args) {
    return MessageFormat.format(spec, args);
  }

  public static <T> void runIf(T input, Predicate<T> pred, Consumer<T> fn) {
    if (pred.test(input)) {
      fn.accept(input);
    }
  }

  public static <T> String toJson(T obj) throws JsonProcessingException {
    return new ObjectMapper()
      .writer()
      .withDefaultPrettyPrinter()
      .writeValueAsString(obj);
  }

  public static String randomString(int len, String chars) {
    var noOfChars = chars.length();

    return IntStream
      .range(0, len)
      .mapToObj(i -> String.valueOf(chars.charAt(random.nextInt(noOfChars))))
      .collect(Collectors.joining());
  }

  public static User sampleUser() {
    return new User(
      UUID.randomUUID().toString(),
      randomString(10, alphaChars),
      random.nextInt(20, 100)
    );
  }
}
