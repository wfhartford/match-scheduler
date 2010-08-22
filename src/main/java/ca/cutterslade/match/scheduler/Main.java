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

import com.google.common.collect.ImmutableSet;

/**
 * @author W.F. Hartford
 * 
 */
public class Main {
  public static void main(String[] args) {
    final Scheduler s = new Scheduler(48, 3, 3, 2, 2, 12);
    ImmutableSet<Match> matches = s.getMatches();
    printMatches(matches);
  }

  /**
   * @param matches
   */
  private static void printMatches(ImmutableSet<Match> matches) {
    for (Match m : matches)
      System.out.println(m);
  }
}
