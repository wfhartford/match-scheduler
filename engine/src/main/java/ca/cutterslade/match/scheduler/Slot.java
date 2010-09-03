/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ca.cutterslade.match.scheduler;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class Slot implements Serializable {

  /**
   * 1
   */
  private static final long serialVersionUID = 1L;

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

  Slot(Time time, Day day, Court court) {
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
    return "Slot [gym=" + court.getGym().getName() + ", court=" + court.getName() + ", day=" + day.getName() + ", time=" + time.getName() + "]";
  }

}
