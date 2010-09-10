package ca.cutterslade.match.scheduler;

import static com.google.common.collect.Iterables.cycle;
import static com.google.common.collect.Iterables.limit;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class AlternatingIterableTest {

  @Test
  public void oneTwoTest() {
    test(ImmutableList.of(1, 2, 1, 2, 1, 2), limit(cycle(1), 3), limit(cycle(2), 3));
  }

  @Test
  public void threeTest() {
    test(ImmutableList.of(1, 2, 3, 1, 2, 3), limit(cycle(1), 2), limit(cycle(2), 2), limit(cycle(3), 2));
  }

  @Test
  public void firstShortTest() {
    test(ImmutableList.of(1, 2, 1, 2, 2), limit(cycle(1), 2), limit(cycle(2), 3));
  }

  @Test
  public void lastShortTest() {
    test(ImmutableList.of(1, 2, 1, 2, 1), limit(cycle(1), 3), limit(cycle(2), 2));
  }

  @Test
  public void middleShortTest() {
    test(ImmutableList.of(1, 2, 3, 1, 2, 3, 1, 2), limit(cycle(1), 3), limit(cycle(2), 3), limit(cycle(3), 2));
  }

  @Test
  public void middleLongTest() {
    test(ImmutableList.of(1, 2, 3, 1, 2, 3, 2, 2), limit(cycle(1), 2), limit(cycle(2), 4), limit(cycle(3), 2));
  }

  private <T> void test(Iterable<T> expected, Iterable<T>... inputs) {
    Assert.assertEquals(ImmutableList.copyOf(expected), ImmutableList.copyOf(new AlternatingIterable<T>(inputs)));
  }
}
