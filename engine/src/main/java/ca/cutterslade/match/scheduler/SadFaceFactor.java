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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public enum SadFaceFactor {
  GYM(2) {

    @Override
    int getSadFaces(Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit) {
//      if (allByes(match)) return 0;
      int sadFaces = 0;
      for (Match m : existingMatches) {
        int commonTeams = Sets.intersection(match, m.getTeams()).size();
        if (0 != commonTeams && m.getSlot().getGym().equals(slot.getGym())) sadFaces += commonTeams;
        if (sadFaces >= limit) break;
      }
      return sadFaces;
    }
  },
  TIME(1) {

    @Override
    int getSadFaces(Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit) {
//      if (allByes(match)) return 0;
      int sadFaces = 0;
      for (Match m : existingMatches) {
        int commonTeams = Sets.intersection(match, m.getTeams()).size();
        if (0 != commonTeams && m.getSlot().getTime().equals(slot.getTime())) sadFaces += commonTeams;
        if (sadFaces >= limit) break;
      }
      return sadFaces;
    }
  },
  MATCH_UP(4) {

    @Override
    int getSadFaces(Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit) {
//      if (allByes(match)) return 0;
      Multimap<Team, Team> playCount = ArrayListMultimap.create();
      for (Team t : match) {
        Set<Team> others = ImmutableSet.copyOf(Sets.difference(match, ImmutableSet.of(t)));
        for (Match m : existingMatches) {
          if (m.getTeams().contains(t)) playCount.putAll(t, Sets.intersection(m.getTeams(), others));
        }
      }
      int sadFaces = 0;
      for (Map.Entry<Team, Collection<Team>> e : playCount.asMap().entrySet()) {
        int alreadyPlayedCount = e.getValue().size();
        sadFaces += alreadyPlayedCount * alreadyPlayedCount;
        if (sadFaces >= limit) return sadFaces;
      }
      return sadFaces;
    }
  },
  BYE_MATCH(0) {

    @Override
    int getSadFaces(Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit) {
      int byes = Iterables.size(Iterables.filter(match, Team.BYE_PREDICATE));
      return byes == match.size() || byes == 0 ? 0 : 1;
    }
  };

  private final int defaultValue;

  private SadFaceFactor(int defaultValue) {
    this.defaultValue = defaultValue;
  }

  abstract int getSadFaces(Slot slot, ImmutableSet<Team> match, Iterable<Match> existingMatches, int limit);

  public int getDefaultValue() {
    return defaultValue;
  }

  boolean allByes(ImmutableSet<Team> match) {
    return Iterables.size(Iterables.filter(match, Team.BYE_PREDICATE)) == match.size();
  }
}