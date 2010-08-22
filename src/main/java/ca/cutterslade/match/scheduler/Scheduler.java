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

import com.google.common.collect.ImmutableSet;

public final class Scheduler {
  private final ImmutableSet<Gym> gyms;
  private final ImmutableSet<Court> courts;
  private final ImmutableSet<Day> days;
  private final ImmutableSet<Time> times;
  private final ImmutableSet<Slot> slots;
  private final ImmutableSet<Tier> tiers;
  private final ImmutableSet<Team> teams;

  public Scheduler(Set<String> teams, Set<String> tiers, Set<String> gyms, Set<String> courts, Set<String> times, Set<String> days) {
    this.gyms = Gym.forNames(gyms);
    this.courts = Court.forNames(courts, this.gyms);
    this.days = Day.forNames(days);
    this.times = Time.forNames(times);
    this.slots = Slot.forNames(this.times, this.courts, this.days);
    this.tiers = Tier.forNames(tiers);
    this.teams = Team.forNames(teams, this.tiers);
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

  public ImmutableSet<Match> getMatches() {
    return getMatches(Configuration.DEFAULT_CONFIGURATION);
  }

  public ImmutableSet<Match> getMatches(Configuration config) {
    if (null == config) throw new IllegalArgumentException("config may not be null");
    return new MatchMaker(config, slots, teams).getMatches();
  }
  
}
