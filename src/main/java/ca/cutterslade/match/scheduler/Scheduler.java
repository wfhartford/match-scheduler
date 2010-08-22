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

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public final class Scheduler {
  private final Configuration config;
  private final ImmutableSet<Gym> gyms;
  private final ImmutableSet<Court> courts;
  private final ImmutableSet<Day> days;
  private final ImmutableSet<Time> times;
  private final ImmutableSet<Slot> slots;
  private final ImmutableSet<Tier> tiers;
  private final ImmutableSet<Team> teams;
  private final ImmutableSet<Match> matches;
  private final ImmutableMap<Day, ImmutableMap<Time, ImmutableSet<Match>>> dayTimeMatches;
  private final ImmutableMap<Day, ImmutableSet<Match>> dayMatches;
  private final ImmutableMap<Slot, Match> slotMatches;

  public Scheduler(Configuration config, Set<String> teams, Set<String> tiers, Set<String> gyms, Set<String> courts, Set<String> times, Set<String> days) {
    if (null == config) throw new IllegalArgumentException("config may not be null");
    if (null == teams) throw new IllegalArgumentException("teams may not be null");
    if (null == tiers) throw new IllegalArgumentException("tiers may not be null");
    if (null == gyms) throw new IllegalArgumentException("gyms may not be null");
    if (null == courts) throw new IllegalArgumentException("courts may not be null");
    if (null == times) throw new IllegalArgumentException("times may not be null");
    if (null == days) throw new IllegalArgumentException("days may not be null");
    this.config = config;
    this.gyms = Gym.forNames(gyms);
    this.courts = Court.forNames(courts, this.gyms);
    this.days = Day.forNames(days);
    this.times = Time.forNames(times);
    this.slots = Slot.forNames(this.times, this.courts, this.days);
    this.tiers = Tier.forNames(tiers);
    this.teams = Team.forNames(teams, this.tiers);
    this.matches = new MatchMaker(config, slots, this.teams).getMatches();
    ImmutableMap.Builder<Day, ImmutableMap<Time, ImmutableSet<Match>>> dayTimeMatches = ImmutableMap.builder();
    for (Day day : this.days) {
      ImmutableMap.Builder<Time, ImmutableSet<Match>> timeMatches = ImmutableMap.builder();
      for (Time time : this.times) {
        ImmutableSet.Builder<Match> b = ImmutableSet.builder();
        for (Match m : matches)
          if (m.getDay().equals(day) && m.getTime().equals(time)) b.add(m);
        timeMatches.put(time, b.build());
      }
      dayTimeMatches.put(day, timeMatches.build());
    }
    this.dayTimeMatches = dayTimeMatches.build();
    ImmutableMap.Builder<Day, ImmutableSet<Match>> dayMatches = ImmutableMap.builder();
    for (Day day : this.days)
      dayMatches.put(day, ImmutableSet.copyOf(Iterables.concat(this.dayTimeMatches.get(day).values())));
    this.dayMatches = dayMatches.build();
    slotMatches = Maps.uniqueIndex(matches, new Function<Match, Slot>() {

      @Override
      public Slot apply(Match match) {
        return match.getSlot();
      }
    });
  }

  public Scheduler(Configuration config, int nTeams, int nTiers, int nGyms, int nCourts, int nTimes, int nDays) {
    this(config, setOf(nTeams), setOf(nTiers), setOf(nGyms), setOf(nCourts), setOf(nTimes), setOf(nDays));
  }

  private static ImmutableSet<String> setOf(int n) {
    ImmutableSet.Builder<String> b = ImmutableSet.builder();
    for (int i = 0; i < n; i++)
      b.add(String.valueOf(i));
    return b.build();
  }

  public Configuration getConfig() {
    return config;
  }

  public ImmutableSet<Gym> getGyms() {
    return gyms;
  }

  public ImmutableSet<Court> getCourts() {
    return courts;
  }

  public ImmutableSet<Day> getDays() {
    return days;
  }

  public ImmutableSet<Time> getTimes() {
    return times;
  }

  public ImmutableSet<Slot> getSlots() {
    return slots;
  }

  public ImmutableSet<Tier> getTiers() {
    return tiers;
  }

  public ImmutableSet<Team> getTeams() {
    return teams;
  }

  public ImmutableSet<Match> getMatches() {
    return matches;
  }

  public ImmutableSet<Match> getMatches(Day day) {
    return dayMatches.get(day);
  }

  public ImmutableSet<Match> getMatches(Day day, Time time) {
    return dayTimeMatches.get(day).get(time);
  }

  public Match getMatch(Day day, Time time, Court court) {
    return slotMatches.get(new Slot(time, day, court));
  }

}
