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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public final class Scheduler {
  private static final int SAD_FACE_GYM_WEIGHT = 2;
  private static final int SAD_FACE_TIME_WEIGHT = 1;

  private final ImmutableSet<Gym> gyms;
  private final ImmutableSet<Court> courts;
  private final ImmutableSet<Day> days;
  private final ImmutableSet<Time> times;
  private final ImmutableSet<Slot> slots;
  private final ImmutableSet<Tier> tiers;
  private final ImmutableSet<Team> teams;
  private final ImmutableSet<Match> matches;

  public Scheduler(Set<String> teams, Set<String> tiers, Set<String> gyms, Set<String> courts, Set<String> times, Set<String> days) {
    this.gyms = Gym.forNames(gyms);
    this.courts = Court.forNames(courts, this.gyms);
    this.days = Day.forNames(days);
    this.times = Time.forNames(times);
    this.slots = Slot.forNames(this.times, this.courts, this.days);
    this.tiers = Tier.forNames(tiers);
    this.teams = Team.forNames(teams, this.tiers);
    this.matches = Match.make(slots.size(), this.teams);
  }

  public Scheduler(int nTeams, int nTiers, int nGyms, int nCourts, int nTimes, int nDays) {
    this(setOf(nTeams), setOf(nTiers), setOf(nGyms), setOf(nCourts), setOf(nTimes), setOf(nDays));
  }

  private static ImmutableSet<String> setOf(int n) {
    ImmutableSet.Builder<String> b = ImmutableSet.builder();
    for (int i = 0; i < n; i++)
      b.add(String.valueOf(i));
    return b.build();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    final Scheduler s = new Scheduler(48, 3, 3, 2, 2, 12);
    s.slotMatches();
    if (!Collections2.filter(s.matches, new Predicate<Match>() {

      @Override
      public boolean apply(Match input) {
        return null == input.getSlot();
      }
    }).isEmpty()) throw new AssertionError();
    final int optimalMatchesAtGym = s.days.size() / s.gyms.size();
    int subOptimalMatchesAtGym = 0;
    for (Gym g : s.gyms) {
      System.out.print("Gym " + g.getName() + ": ");
      for (Team t : s.teams) {
        int matchesAtGym = s.matchesAtGym(t, g);
        subOptimalMatchesAtGym += Math.abs(matchesAtGym - optimalMatchesAtGym);
        System.out.print("Team " + t.getName() + ": " + matchesAtGym + "; ");
      }
      System.out.println();
    }
    System.out.println("Sub optimal by " + subOptimalMatchesAtGym);
    final int optimalMatchesAtTime = s.days.size() / s.times.size();
    int subOptimalMatchesAtTime = 0;
    for (Time time : s.times) {
      System.out.print("Time " + time.getName() + ": ");
      for (Team team : s.teams) {
        int matchesAtTime = s.matchesAtTime(team, time);
        subOptimalMatchesAtTime += Math.abs(matchesAtTime - optimalMatchesAtTime);
        System.out.print("Team " + team.getName() + ": " + matchesAtTime + "; ");
      }
      System.out.println();
    }
    System.out.println("Sub optimal by " + subOptimalMatchesAtTime);

    Map<Slot, Match> slotMatches = Maps.uniqueIndex(s.matches, new Function<Match, Slot>() {

      @Override
      public Slot apply(Match from) {
        return from.getSlot();
      }
    });
    System.out.println();
    System.out.print(s.summary(slotMatches));
  }

  private String summary(Map<Slot, Match> slotMatches) {
    StringWriter w = new StringWriter();
    PrintWriter p = new PrintWriter(w);
    String columnFormat = " %12s";
    p.printf(columnFormat, "Day");
    p.printf(columnFormat, "Time");
    for (Court c : courts) {
      p.printf(columnFormat, c.getGym().getName() + '-' + c.getName());
    }
    p.println();
    for (Day d : days)
      for (Time t : times) {
        p.printf(columnFormat, d.getName());
        p.printf(columnFormat, t.getName());
        for (Court c : courts) {
          Match m = slotMatches.get(new Slot(t, d, c));
          p.printf(columnFormat, m.teamsString());
        }
        p.println();
      }
    p.close();
    return w.toString();
  }

  private void slotMatches() {
    for (Map.Entry<Day, Collection<Match>> e : pickMatchDays().asMap().entrySet()) {
      fillSlots(e.getValue(), slotsFor(e.getKey()));
    }
  }

  private void fillSlots(Collection<Match> matches, Iterable<Slot> slots) {
    System.out.print("filling: ");
    Set<Match> placed = Sets.newHashSet();
    Set<Slot> used = Sets.newHashSet();
    Collection<Match> unplaced = Collections2.filter(matches, Predicates.not(Predicates.in(placed)));
    Iterable<Slot> unused = Iterables.filter(slots, Predicates.not(Predicates.in(used)));
    int limit = leastSadFaces();
    while (!unplaced.isEmpty()) {
      for (Match m : unplaced)
        for (Slot s : unused)
          if (limit >= sadFaceCount(m, s)) {
            System.out.print(placed.size() + " ");
            m.setSlot(s);
            placed.add(m);
            used.add(s);
            break;
          }
      limit++;
    }
    System.out.println(":filled " + limit);
  }

  private int sadFaceCount(Match m, Slot s) {
    return matchesAtGym(m, s.getGym()) * SAD_FACE_GYM_WEIGHT + matchesAtTime(m, s.getTime()) * SAD_FACE_TIME_WEIGHT;
  }

  private int matchesAtGym(Match match, Gym gym) {
    int times = 0;
    for (Match m : matches)
      if (null != m.getSlot() && m.getSlot().getGym().equals(gym)) times += Sets.intersection(m.getTeams(), match.getTeams()).size();
    return times;
  }

  private int matchesAtTime(Match match, Time time) {
    int times = 0;
    for (Match m : matches)
      if (null != m.getSlot() && m.getSlot().getTime().equals(time)) times += Sets.intersection(m.getTeams(), match.getTeams()).size();
    return times;
  }

  private int matchesAtGym(Team team, Gym gym) {
    int times = 0;
    for (Match m : matches)
      if (null != m.getSlot() && m.getSlot().getGym().equals(gym) && m.getTeams().contains(team)) times++;
    return times;
  }

  private int matchesAtTime(Team team, Time time) {
    int times = 0;
    for (Match m : matches)
      if (null != m.getSlot() && m.getSlot().getTime().equals(time) && m.getTeams().contains(team)) times++;
    return times;
  }

  private int leastSadFaces() {
    return leastMatchesAtGym() * SAD_FACE_GYM_WEIGHT + leatMatchesAtTime() * SAD_FACE_TIME_WEIGHT;
  }

  private int leastMatchesAtGym() {
    int matches = Integer.MAX_VALUE;
    for (Team t : teams)
      for (Gym g : gyms) {
        int n = matchesAtGym(t, g);
        if (0 == n) return n;
        else if (n < matches) matches = n;
      }
    return matches;
  }

  private int leatMatchesAtTime() {
    int matches = Integer.MAX_VALUE;
    for (Team team : teams)
      for (Time time : times) {
        int n = matchesAtTime(team, time);
        if (0 == n) return n;
        else if (n < matches) matches = n;
      }
    return matches;
  }

  private Multimap<Day, Match> pickMatchDays() {
    Multimap<Day, Match> matchDays = HashMultimap.create();
    outer: for (Match m : matches) {
      for (Day d : days) {
        if (noTeamPlaysOn(d, m, matchDays)) {
          matchDays.put(d, m);
          continue outer;
        }
      }
      throw new AssertionError();
    }
    return matchDays;
  }

  private Iterable<Slot> slotsFor(final Day day) {
    return Iterables.filter(slots, new Predicate<Slot>() {

      @Override
      public boolean apply(Slot slot) {
        return slot.getDay().equals(day);
      }
    });
  }

  private boolean noTeamPlaysOn(Day day, Match match, Multimap<Day, Match> matchDays) {
    for (Match m : matchDays.get(day))
      if (!Sets.intersection(match.getTeams(), m.getTeams()).isEmpty()) return false;
    return true;
  }
}
