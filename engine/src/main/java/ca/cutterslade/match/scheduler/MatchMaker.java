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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author W.F. Hartford
 * 
 */
final class MatchMaker {

  private static final class PossibleMatchesCallable implements Callable<Set<ImmutableSet<Team>>> {

    private static final class ConstructorFunction implements Function<Collection<Team>, Callable<Set<ImmutableSet<Team>>>> {

      private final int teamsPerMatch;

      public ConstructorFunction(int teamsPerMatch) {
        this.teamsPerMatch = teamsPerMatch;
      }

      @Override
      public Callable<Set<ImmutableSet<Team>>> apply(Collection<Team> from) {
        return new PossibleMatchesCallable(from, teamsPerMatch);
      }
    }

    static Iterable<Callable<Set<ImmutableSet<Team>>>> forTiers(Iterable<Collection<Team>> tiers, int teamsPerMatch) {
      return Iterables.transform(tiers, new ConstructorFunction(teamsPerMatch));
    }

    private final Collection<Team> tier;

    private final int teamsPerMatch;

    PossibleMatchesCallable(Collection<Team> tier, int teamsPerMatch) {
      this.tier = tier;
      this.teamsPerMatch = teamsPerMatch;
    }

    @Override
    public Set<ImmutableSet<Team>> call() throws Exception {
      Set<ImmutableSet<Team>> matches = Sets.newLinkedHashSet();
      build(Sets.<Team> newLinkedHashSet(), matches);
      return matches;
    }

    private void build(Set<Team> match, Set<ImmutableSet<Team>> matches) {
      Iterable<Team> options = match.isEmpty() ? this.tier : Iterables.filter(this.tier, Predicates.not(Predicates.in(match)));
      for (Team t : options) {
        Set<Team> thisMatch = Sets.newLinkedHashSet(match);
        thisMatch.add(t);
        if (thisMatch.size() == teamsPerMatch) matches.add(ImmutableSet.copyOf(thisMatch));
        else build(thisMatch, matches);
      }
    }
  }

  private static class SadFacesCallable implements Callable<Integer> {

    private final SadFaceFactor factor;

    private final int weight;

    private final Slot slot;

    private final ImmutableSet<Team> match;

    private final Iterable<Match> existingMatches;

    private final int limit;

    SadFacesCallable(SadFaceFactor factor, int weight, Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit) {
      this.factor = factor;
      this.weight = weight;
      this.slot = slot;
      this.match = match;
      this.existingMatches = existingMatches;
      this.limit = limit;
    }

    @Override
    public Integer call() throws Exception {
      return weight * factor.getSadFaces(slot, match, existingMatches, limit);
    }

  }

  private static final Random RANDOM = new Random();

  private final Executor executor = new Executor();

  private final Configuration configuration;

  private final ImmutableSet<Team> teams;

  private final ImmutableMultimap<Tier, Team> tiers;

  private final ImmutableMultimap<Day, Slot> days;

  private final int teamSize;

  MatchMaker(Configuration configuration, ImmutableSet<Slot> slots, ImmutableSet<Team> teams, int teamSize) {
    if (null == configuration) throw new IllegalArgumentException("configuration may not be null");
    if (null == slots) throw new IllegalArgumentException("slots may not be null");
    if (null == teams) throw new IllegalArgumentException("teams may not be null");
    if (2 > teamSize) throw new IllegalArgumentException("size must be two or greater");
    this.configuration = configuration;
    this.teams = teams;
    ImmutableMultimap.Builder<Tier, Team> tiers = ImmutableMultimap.builder();
    for (Team t : teams)
      tiers.put(t.getTier(), t);
    this.tiers = tiers.build();

    ImmutableMultimap.Builder<Day, Slot> days = ImmutableMultimap.builder();
    for (Slot s : slots)
      days.put(s.getDay(), s);
    this.days = days.build();
    this.teamSize = teamSize;
  }

  ImmutableSet<Match> getMatches() throws InterruptedException {
    ImmutableSet<ImmutableSet<Team>> matches = executor.union(PossibleMatchesCallable.forTiers(this.tiers.asMap().values(), teamSize));
    ImmutableSet.Builder<Match> b = ImmutableSet.builder();
    final Collection<Day> days;
    if (configuration.isRandomizeDayOrder()) {
      List<Day> d = Lists.newArrayList(this.days.keySet());
      Collections.shuffle(d);
      days = d;
    }
    else days = this.days.keySet();
    for (Day d : days)
      b.addAll(getMatchesForDay(d, b.build(), matches));
    return b.build();
  }

  private Iterable<Match> getMatchesForDay(Day day, Iterable<Match> existing, ImmutableSet<ImmutableSet<Team>> matches) throws InterruptedException {
    Set<Team> teams = Sets.newHashSet(this.teams);
    Set<Match> made = Sets.newHashSet();
    while (!teams.isEmpty()) {
      for (Slot s : getDaySlots(day)) {
        final ImmutableSet<Team> bestMatch = getBestMatch(Iterables.concat(existing, made), matches, teams, s);
        made.add(makeMatch(s, bestMatch));
        teams.removeAll(bestMatch);
      }
    }
    return ImmutableSet.copyOf(made);
  }

  private Collection<Slot> getDaySlots(Day day) {
    Collection<Slot> slots = this.days.get(day);
    if (configuration.isRandomizeSlotOrder()) {
      List<Slot> s = Lists.newArrayList(slots);
      Collections.shuffle(s, RANDOM);
      slots = s;
    }
    return slots;
  }

  private ImmutableSet<Team> getBestMatch(Iterable<Match> existing, ImmutableSet<ImmutableSet<Team>> matches, Set<Team> teams, Slot s) throws InterruptedException {
    int leastSadFaces = Integer.MAX_VALUE;
    List<ImmutableSet<Team>> bestMatches = Lists.newArrayList();
    for (ImmutableSet<Team> m : matches) {
      if (teams.containsAll(m)) {
        int sadFaces = getSadFaces(s, m, existing, leastSadFaces);
        if (sadFaces < leastSadFaces) {
          bestMatches.clear();
          bestMatches.add(m);
          if (0 == sadFaces) break;
          leastSadFaces = sadFaces;
        }
        else if (sadFaces == leastSadFaces) {
          bestMatches.add(m);
        }
      }
    }
    return 1 == bestMatches.size() ? Iterables.getOnlyElement(bestMatches) : bestMatches.get(RANDOM.nextInt(bestMatches.size()));
  }

  private int getSadFaces(Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit) throws InterruptedException {
    List<SadFacesCallable> sfcs = Lists.newArrayList();
    for (SadFaceFactor f : SadFaceFactor.values())
      sfcs.add(new SadFacesCallable(f, configuration.getFactor(f), slot, match, existingMatches, limit));
    return executor.sum(sfcs);
  }

  private Match makeMatch(Slot s, final ImmutableSet<Team> bestMatch) {
    final Match m;
    if (configuration.isRandomizeMatchOrder()) {
      List<Team> r = Lists.newArrayList(bestMatch);
      Collections.shuffle(r, RANDOM);
      m = new Match(r, s);
    }
    else m = new Match(bestMatch, s);
    return m;
  }

}
