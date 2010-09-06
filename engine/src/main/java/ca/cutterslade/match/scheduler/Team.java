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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class Team implements Serializable {

  /**
   * 1
   */
  private static final long serialVersionUID = 1L;

  public static final Predicate<Team> BYE_PREDICATE = new Predicate<Team>() {

    @Override
    public boolean apply(Team input) {
      return input.isBye();
    }
  };

  public static final Pattern BYE_PATTERN = Pattern.compile("B\\d+");

  private final String name;

  private final Tier tier;

  private final boolean isBye;

  static ImmutableSet<Team> forNames(Set<String> names, Set<Tier> tiers, int tierSize) {
    ImmutableSet.Builder<Team> teams = ImmutableSet.builder();
    Iterator<String> tit = names.iterator();
    for (Tier t : tiers) {
      List<Team> tierTeams = Lists.newArrayList();
      while (tit.hasNext() && tierSize > tierTeams.size())
        tierTeams.add(new Team(tit.next(), t));
      teams.addAll(tierTeams);
      if (!tit.hasNext()) break;
    }
    return teams.build();
  }

  Team(String name, Tier tier) {
    if (null == name) throw new IllegalArgumentException("name may not be null");
    if (null == tier) throw new IllegalArgumentException("tier may not be null");
    this.name = name;
    this.tier = tier;
    this.isBye = BYE_PATTERN.matcher(name).matches();
  }

  public Tier getTier() {
    return tier;
  }

  public String getName() {
    return name;
  }

  public boolean isBye() {
    return isBye;
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
    return "Team " + name + "(" + tier.getName() + ")";
  }

}
