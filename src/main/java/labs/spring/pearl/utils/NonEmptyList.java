package labs.spring.pearl.utils;

import java.util.*;
import java.util.stream.Collectors;

public class NonEmptyList<T> {

  private final List<T> items = new ArrayList<>();

  @SafeVarargs
  public NonEmptyList(T head, T... tail) {
    items.add(head);
    items.addAll(Arrays.asList(tail));
  }

  public List<T> toList() {
    return Collections.unmodifiableList(this.items);
  }

  public Set<T> toSet() {
    return Set.copyOf(this.items);
  }

  private NonEmptyList(Collection<T> items) {
    assert(!items.isEmpty());
    this.items.addAll(items);
  }

  public static <A> Optional<NonEmptyList<A>> of(List<A> items) {
    return items.isEmpty()
           ? Optional.empty()
           : Optional.of(new NonEmptyList<>(items));
  }
}
