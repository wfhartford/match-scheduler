package ca.cutterslade.match.scheduler;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

final class Court {
  private final String name;
  private final Gym gym;

  static ImmutableSet<Court> forNames(Set<String> names, Set<Gym> gyms) {
    ImmutableSet.Builder<Court> b = ImmutableSet.builder();
    for (Gym gym : gyms)
      for (String name : names)
        b.add(new Court(name, gym));
    return b.build();
  }

  public Court(String name, Gym gym) {
    if (null == name) throw new IllegalArgumentException("name may not be null");
    if (null == gym) throw new IllegalArgumentException("gym may not be null");
    this.name = name;
    this.gym = gym;
  }

  public String getName() {
    return name;
  }

  public Gym getGym() {
    return gym;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + gym.hashCode();
    result = prime * result + name.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Court other = (Court) obj;
    if (!gym.equals(other.gym)) return false;
    if (!name.equals(other.name)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Court [gym=" + gym + ", name=" + name + "]";
  }

}
