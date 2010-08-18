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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

final class Team {
  private final String name;
  private final Tier tier;

  static ImmutableSet<Team> forNames(Set<String> names, Set<Tier> tiers) {
    if (0 != names.size() % tiers.size()) throw new IllegalArgumentException("number of teams must be divisable by number of tiers");
    ImmutableSet.Builder<Team> teams = ImmutableSet.builder();
    Iterable<List<String>> it = Iterables.partition(names, names.size() / tiers.size());
    Iterator<Tier> tierIt = tiers.iterator();
    for (List<String> teamNames : it) {
      Tier t = tierIt.next();
      for (String name : teamNames) {
        teams.add(new Team(name, t));
      }
    }
    return teams.build();
  }

  Team(String name, Tier tier) {
    if (null == name) throw new IllegalArgumentException("name may not be null");
    if (null == tier) throw new IllegalArgumentException("tier may not be null");
    this.name = name;
    this.tier = tier;
  }

  Tier getTier() {
    return tier;
  }

  String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + tier.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Team other = (Team) obj;
    if (!name.equals(other.name)) return false;
    if (!tier.equals(other.tier)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Team [name=" + name + ", tier=" + tier + "]";
  }

}
