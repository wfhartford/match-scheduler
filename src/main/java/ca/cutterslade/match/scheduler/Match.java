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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

final class Match {

  private final ImmutableSet<Team> teams;
  private Slot slot;
  private boolean important;

  boolean isImportant() {
    return important;
  }

  void setImportant(boolean important) {
    this.important = important;
  }

  static ImmutableSet<Match> make(int number, Set<Team> teams) {
    ImmutableCollection<Collection<Team>> tieredTeams = Multimaps.index(teams, new Function<Team, Tier>() {

      @Override
      public Tier apply(Team team) {
        return team.getTier();
      }
    }).asMap().values();
    int matchesPerTier = number / tieredTeams.size();
    ImmutableSet.Builder<Match> b = ImmutableSet.builder();
    for (Collection<Team> tier : tieredTeams) {
      ImmutableSet.Builder<Match> tierMatches = ImmutableSet.builder();
      for (Team t1 : tier)
        for (Team t2 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1)))))
          for (Team t3 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1, t2)))))
            for (Team t4 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1, t2, t3)))))
              tierMatches.add(new Match(ImmutableSet.of(t1, t2, t3, t4)));
      ImmutableSet<Match> possibleMatches = tierMatches.build();
      ImmutableSet<Match> mostUnique = mostUnique(possibleMatches, tier.size());
      for (Match m : mostUnique) m.setImportant(true);
      int excluded = possibleMatches.size() - mostUnique.size();
      Iterable<Match> nonUnique = ImmutableSet.of();
      int threshold = 2;
      while (excluded > 0) {
        ImmutableSet<Match> disimilar = filterSimilar(possibleMatches, Iterables.concat(mostUnique, nonUnique), threshold++);
        nonUnique = Iterables.concat(nonUnique, disimilar);
        excluded -= disimilar.size();
      }

      List<Match> goodOnes = Lists.newArrayList(mostUnique);
      Multiset<Team> matchCount = HashMultiset.create();
      for (Match m : mostUnique)
        matchCount.addAll(m.getTeams());

      Match lastUsed = null;
      outer: for (Match m : Iterables.cycle(nonUnique)) {
        if (m == lastUsed) throw new AssertionError();
        if (goodOnes.contains(m)) continue;
        int maxEach = (matchCount.size() / tier.size()) + 1;
        for (Team t : m.getTeams())
          if (maxEach == matchCount.count(t)) continue outer;
        goodOnes.add(m);
        lastUsed = m;
        matchCount.addAll(m.getTeams());
        if (goodOnes.size() == matchesPerTier) break;
      }

      int matchesEach = 0;
      for (Multiset.Entry<Team> e : matchCount.entrySet()) {
        if (0 == matchesEach) matchesEach = e.getCount();
        else if (matchesEach != e.getCount()) throw new AssertionError();
      }
      b.addAll(goodOnes);
    }
    return b.build();
  }

  private static ImmutableSet<Match> filterSimilar(ImmutableSet<Match> matches, Iterable<Match> existing, int threshold) {
    List<Match> chosen = Lists.newArrayList();
    outer: for (Match m : matches) {
      for (Match o : Iterables.concat(existing, chosen))
        if (Sets.intersection(m.getTeams(), o.getTeams()).size() > threshold) continue outer;
      chosen.add(m);
    }
    return ImmutableSet.copyOf(chosen);
  }

  static ImmutableSet<Match> mostUnique(ImmutableSet<Match> matches, int teams) {
    final int teamsPerMatch = matches.iterator().next().getTeams().size();
    // XXX does this formula hold for other than 48 teams with 4 per match?
    final int nUniqueMatches = (teams / teamsPerMatch) * ((teams / teamsPerMatch) + 1);
    int participants = 0;
    List<Match> unique = Lists.newArrayList();
    for (Match m : Iterables.cycle(matches)) {
      int maxInterSize = 0;
      for (Match sm : unique) {
        int interSize = Sets.intersection(m.getTeams(), sm.getTeams()).size();
        maxInterSize = Math.max(maxInterSize, interSize);
      }
      int limit = Math.min(participants / teams, 1);
      if (maxInterSize == limit) {
        unique.add(m);
        participants += teamsPerMatch;
      }
      if (unique.size() == nUniqueMatches) break;
    }
    return ImmutableSet.copyOf(unique);
  }

  Match(ImmutableSet<Team> teams) {
    if (null == teams) throw new IllegalArgumentException("teams may not be null");
    this.teams = teams;
  }

  Slot getSlot() {
    return slot;
  }

  void setSlot(Slot slot) {
    this.slot = slot;
  }

  ImmutableSet<Team> getTeams() {
    return teams;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + teams.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Match other = (Match) obj;
    if (!teams.equals(other.teams)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Match [teams=" + teams + ", slot=" + slot + "]";
  }

  String teamsString() {
    StringBuilder b = new StringBuilder();
    for (Team t : teams)
      b.append(t.getName()).append(',');
    b.setLength(b.length() - 1);
    if (isImportant()) b.append('*');
    return b.toString();
  }
}
