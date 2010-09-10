package ca.cutterslade.match.scheduler;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public final class AlternatingIterable<T> implements Iterable<T> {

  private final ImmutableList<Iterable<T>> iterables;

  public AlternatingIterable(Iterator<? extends Iterable<T>> iters) {
    this.iterables = ImmutableList.copyOf(iters);
  }

  public AlternatingIterable(Iterable<? extends Iterable<T>> iters) {
    this(iters.iterator());
  }

  public AlternatingIterable(Iterable<T>[] iters) {
    this(Arrays.asList(iters));
  }

  static class IteratorFunction<T> implements Function<Iterable<T>, Iterator<T>> {

    private static final IteratorFunction<Object> INSTANCE = new IteratorFunction<Object>();

    public static <T> IteratorFunction<T> get() {
      @SuppressWarnings("unchecked")
      IteratorFunction<T> i = (IteratorFunction<T>) INSTANCE;
      return i;
    }

    @Override
    public Iterator<T> apply(Iterable<T> from) {
      return from.iterator();
    }
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      private final ImmutableSet<Iterator<T>> iterators = ImmutableSet.copyOf(Iterables.transform(iterables, IteratorFunction.<T> get()));

      private final Set<Iterator<T>> exhausted = Sets.newHashSet();

      private Iterator<Iterator<T>> currentIterator = notExhausted();

      private boolean gotNext = false;

      private T next;

      private Iterator<Iterator<T>> notExhausted() {
        return Iterables.filter(iterators, Predicates.not(Predicates.in(exhausted))).iterator();
      }

      @Override
      public boolean hasNext() {
        if (gotNext) return true;
        if (null == currentIterator) return false;
        if (!currentIterator.hasNext()) {
          currentIterator = notExhausted();
          if (!currentIterator.hasNext()) {
            currentIterator = null;
            return false;
          }
        }
        while (!gotNext) {
          Iterator<T> it = currentIterator.next();
          if (!it.hasNext()) {
            exhausted.add(it);
            if (!currentIterator.hasNext()) {
              currentIterator = notExhausted();
              if (!currentIterator.hasNext()) {
                currentIterator = null;
                return false;
              }
            }
          }
          else {
            next = it.next();
            gotNext = true;
          }
        }
        return gotNext;
      }

      @Override
      public T next() {
        if (!hasNext()) throw new NoSuchElementException();
        gotNext = false;
        return next;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
