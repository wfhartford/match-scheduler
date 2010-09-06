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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public final class Tier implements Serializable {

  /**
   * 1
   */
  private static final long serialVersionUID = 1L;

  private final String name;

  private final Predicate<Team> teamPredicate = new Predicate<Team>() {

    @Override
    public boolean apply(Team input) {
      return input.getTier().equals(Tier.this);
    }
  };

  static ImmutableSet<Tier> forNames(Set<String> names) {
    return ImmutableSet.copyOf(Collections2.transform(names, new Function<String, Tier>() {

      @Override
      public Tier apply(String name) {
        return new Tier(name);
      }
    }));
  }

  Tier(String name) {
    if (null == name) throw new IllegalArgumentException(name);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Predicate<Team> getPredicate() {
    return teamPredicate;
  }

  public Iterable<Team> getTeams(Iterable<Team> allTeams) {
    return Iterables.filter(allTeams, teamPredicate);
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
    Tier other = (Tier) obj;
    if (!name.equals(other.name)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Tier [name=" + name + "]";
  }

}
