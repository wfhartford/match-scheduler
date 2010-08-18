package ca.cutterslade.match.scheduler;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

final class Slot {
  private final Time time;
  private final Day day;
  private final Court court;

  static ImmutableSet<Slot> forNames(Set<Time> times, Set<Court> courts, Set<Day> days) {
    ImmutableSet.Builder<Slot> b = ImmutableSet.builder();
    for (Day day : days)
      for (Court court : courts)
        for (Time time : times)
          b.add(new Slot(time, day, court));
    return b.build();
  }

  public Slot(Time time, Day day, Court court) {
    if (null == time) throw new IllegalArgumentException("time may not be null");
    if (null == court) throw new IllegalArgumentException("court may not be null");
    if (null == day) throw new IllegalArgumentException("day may not be null");
    this.time = time;
    this.day = day;
    this.court = court;
  }

  public Time getTime() {
    return time;
  }

  public Day getDay() {
    return day;
  }

  public Court getCourt() {
    return court;
  }

  public Gym getGym() {
    return court.getGym();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + court.hashCode();
    result = prime * result + day.hashCode();
    result = prime * result + time.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Slot other = (Slot) obj;
    if (!court.equals(other.court)) return false;
    if (!day.equals(other.day)) return false;
    if (!time.equals(other.time)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Time [court=" + court + ", day=" + day + ", time=" + time + "]";
  }

}
