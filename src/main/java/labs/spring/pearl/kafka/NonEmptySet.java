package labs.spring.pearl.kafka;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NonEmptySet<T> {
  private final Set<T> items = new HashSet<>();

  @SafeVarargs
  public NonEmptySet(T first, T... rest) {
    this.items.add(first);
    this.items.addAll(Arrays.asList(rest));
  }

  public int size() {
    return this.items.size();
  }

  public Set<T> toSet() {
    return Collections.unmodifiableSet(this.items);
  }
}
