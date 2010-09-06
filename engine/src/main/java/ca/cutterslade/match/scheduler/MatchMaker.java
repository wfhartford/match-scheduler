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
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
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

  private static class PossibleMatchesCallable implements Callable<Set<ImmutableSet<Team>>> {

    private static final Function<Collection<Team>, Callable<Set<ImmutableSet<Team>>>> CTOR_FN = new Function<Collection<Team>, Callable<Set<ImmutableSet<Team>>>>() {

      @Override
      public Callable<Set<ImmutableSet<Team>>> apply(Collection<Team> from) {
        return new PossibleMatchesCallable(from);
      }
    };

    static Iterable<Callable<Set<ImmutableSet<Team>>>> forTiers(Iterable<Collection<Team>> tiers) {
      return Iterables.transform(tiers, CTOR_FN);
    }

    private Collection<Team> tier;

    PossibleMatchesCallable(Collection<Team> tier) {
      this.tier = tier;
    }

    @Override
    public Set<ImmutableSet<Team>> call() throws Exception {
      Set<ImmutableSet<Team>> tierMatches = Sets.newHashSet();
      for (Team t1 : tier)
        for (Team t2 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1)))))
          for (Team t3 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1, t2)))))
            for (Team t4 : Collections2.filter(tier, Predicates.not(Predicates.in(ImmutableSet.of(t1, t2, t3)))))
              tierMatches.add(ImmutableSet.of(t1, t2, t3, t4));
      return tierMatches;
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

  private final Executor executor = new Executor();

  private final Configuration configuration;

  private final ImmutableSet<Team> teams;

  private final ImmutableMultimap<Tier, Team> tiers;

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

    ImmutableMultimap.Builder<Day, Slot> days = ImmutableMultimap.builder();
    for (Slot s : slots)
      days.put(s.getDay(), s);
    this.days = days.build();
  }

  ImmutableSet<Match> getMatches() throws InterruptedException {
    ImmutableSet<ImmutableSet<Team>> matches = executor.union(PossibleMatchesCallable.forTiers(this.tiers.asMap().values()));
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
      final Collection<Slot> slots;
      if (configuration.isRandomizeSlotOrder()) {
        List<Slot> s = Lists.newArrayList(this.days.get(day));
        Collections.shuffle(s);
        slots = s;
      }
      else slots = this.days.get(day);
      for (Slot s : slots) {
        int leastSadFaces = Integer.MAX_VALUE;
        ImmutableSet<Team> bestMatch = null;
        for (ImmutableSet<Team> m : matches) {
          if (teams.containsAll(m)) {
            int sadFaces = getSadFaces(s, m, Iterables.concat(existing, made), leastSadFaces);
            if (sadFaces < leastSadFaces) {
              bestMatch = m;
              if (0 == sadFaces) break;
              leastSadFaces = sadFaces;
            }
          }
        }
        final Match m;
        if (configuration.isRandomizeMatchOrder()) {
          List<Team> r = Lists.newArrayList(bestMatch);
          Collections.shuffle(r);
          m = new Match(r, s);
        }
        else m = new Match(bestMatch, s);
        made.add(m);
        teams.removeAll(bestMatch);
      }
    }
    return ImmutableSet.copyOf(made);
  }

  private int getSadFaces(Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit) throws InterruptedException {
    List<SadFacesCallable> sfcs = Lists.newArrayList();
    for (SadFaceFactor f : SadFaceFactor.values())
      sfcs.add(new SadFacesCallable(f, configuration.getFactor(f), slot, match, existingMatches, limit));
    return executor.sum(sfcs);
  }

}
