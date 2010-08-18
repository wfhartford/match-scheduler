package ca.cutterslade.match.scheduler;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;

final class Gym {
  private final String name;

  static ImmutableSet<Gym> forNames(Set<String> names) {
    return ImmutableSet.copyOf(Collections2.transform(names, new Function<String, Gym>() {

      @Override
      public Gym apply(String name) {
        return new Gym(name);
      }
    }));
  }

  public Gym(String name) {
    if (null == name) throw new IllegalArgumentException("name may not be null");
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Gym other = (Gym) obj;
    if (!name.equals(other.name)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Gym [name=" + name + "]";
  }

}
