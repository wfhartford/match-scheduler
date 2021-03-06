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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

public final class Match implements Serializable {

  /**
   * 1
   */
  private static final long serialVersionUID = 1L;

  private final ImmutableSet<Team> teams;

  private final Slot slot;
  
  /**
   * @param bestMatch
   * @param s
   */
  public Match(Iterable<Team> teams, Slot slot) {
    if (null == teams) throw new IllegalArgumentException("teams may not be null");
    if (null == slot) throw new IllegalArgumentException("slot may not be null");
    this.teams = ImmutableSet.copyOf(teams);
    this.slot = slot;
  }

  public Slot getSlot() {
    return slot;
  }

  public Day getDay() {
    return slot.getDay();
  }

  public Time getTime() {
    return slot.getTime();
  }

  public Gym getGym() {
    return slot.getGym();
  }

  public Court getCourt() {
    return slot.getCourt();
  }

  public ImmutableSet<Team> getTeams() {
    return teams;
  }
  
  public Tier getTier() {
    return teams.iterator().next().getTier();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + slot.hashCode();
    result = prime * result + teams.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Match other = (Match) obj;
    if (!slot.equals(other.slot)) return false;
    if (!teams.equals(other.teams)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Match [slot=" + slot + ", teams=" + teamString() + "]";
  }

  public String teamString() {
    StringBuilder b = new StringBuilder();
    for (Team t : teams)
      b.append(t.getName()).append(',');
    b.setLength(b.length() - 1);
    return b.toString();
  }
}