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
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author W.F. Hartford
 * 
 */
final class MatchMaker {

  private final Configuration configuration;
  private final ImmutableSet<Team> teams;
  private final ImmutableMultimap<Tier, Team> tiers;
  private final ImmutableSet<ImmutableSet<Team>> matches;
  private final ImmutableMultimap<Day, Slot> days;

  MatchMaker(Configuration configuration, ImmutableSet<Slot> slots, ImmutableSet<Team> teams) {
    if (null == configuration) throw new IllegalArgumentException("configuration may not be null");
    if (null == slots) throw new IllegalArgumentException("slots may not be null");
    if (null == teams) throw new IllegalArgumentException("teams may not be null");
    this.configuration = configuration;
    this.teams = teams;
    ImmutableMultimap.Builder<Tier, Team> tiers = ImmutableMultimap.builder();
    for (Team t : teams)
      tiers.put(t.getTier(), t);
    this.tiers = tiers.build();

    ImmutableSet.Builder<ImmutableSet<Team>> matches = ImmutableSet.builder();
    for (Map.Entry<Tier, Collection<Team>> e : this.tiers.asMap().entrySet())
      matches.addAll(getPossibleMatches(e.getValue()));
    this.matches = matches.build();
    ImmutableMultimap.Builder<Day, Slot> days = ImmutableMultimap.builder();
    for (Slot s : slots)
      days.put(s.getDay(), s);
    this.days = days.build();
  }

  ImmutableSet<Match> getMatches() {
    ImmutableSet.Builder<Match> b = ImmutableSet.builder();
    for (Day d : days.keySet())
      b.addAll(getMatchesForDay(d, b.build()));
    return b.build();
  }

  private Iterable<Match> getMatchesForDay(Day day, Iterable<Match> existing) {
    Set<Team> teams = Sets.newHashSet(this.teams);
    Set<Match> made = Sets.newHashSet();
    while (!teams.isEmpty()) {
      for (Slot s : this.days.get(day)) {
        int leastSadFaces = Integer.MAX_VALUE;
        ImmutableSet<Team> bestMatch = null;
        for (ImmutableSet<Team> m : this.matches) {
          if (teams.containsAll(m)) {
            int sadFaces = getSadFaces(s, m, Iterables.concat(existing, made), leastSadFaces);
            if (sadFaces < leastSadFaces) {
              leastSadFaces = sadFaces;
              bestMatch = m;
            }
          }
        }
        Match m = new Match(bestMatch, s);
        made.add(m);
        teams.removeAll(bestMatch);
      }
    }
    return ImmutableSet.copyOf(made);
  }

  /**
   * @param limit 
   * @param s
   * @param m
   * @param iterable
   * @return
   */
  private int getSadFaces(Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit) {
    int totalFactor = 0;
    for (SadFaceFactor f : SadFaceFactor.values()) {
      int factor = configuration.getFactor(f);
      if (0 != factor) totalFactor += factor * f.getSadFaces(slot, match, existingMatches, limit - totalFactor);
      if (totalFactor > limit) break;
    }
    return totalFactor;
  }

  private static ImmutableSet<ImmutableSet<Team>> getPossibleMatches(Collection<Team> tier) {
    ImmutableSet.Builder<ImmutableSet<Team>> tierMatches = ImmutableSet.builder();
    for (Team t1 : tier)
      for (Team t2 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1)))))
        for (Team t3 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1, t2)))))
          for (Team t4 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1, t2, t3)))))
            tierMatches.add(ImmutableSet.of(t1, t2, t3, t4));
    return tierMatches.build();
  }
}
