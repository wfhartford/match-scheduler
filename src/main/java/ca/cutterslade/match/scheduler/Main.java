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

/**
 * @author W.F. Hartford
 * 
 */
public class Main {
  public static void main(String[] args) {
    final Scheduler s = new Scheduler(Configuration.DEFAULT_CONFIGURATION, 48, 3, 3, 2, 2, 12);
    printMatches(s);
  }

  /**
   * @param s
   *          .getMatches()
   */
  private static void printMatches(Scheduler s) {
    for (Match m : s.getMatches())
      System.out.println(m);
    System.out.println(summary(s));
  }

  private static String summary(Scheduler scheduler) {
    StringWriter w = new StringWriter();
    PrintWriter p = new PrintWriter(w);
    String columnFormat = " %12s";
    p.printf(columnFormat, "Day");
    p.printf(columnFormat, "Time");
    for (Court c : scheduler.getCourts()) {
      p.printf(columnFormat, c.getGym().getName() + '-' + c.getName());
    }
    p.println();
    for (Day d : scheduler.getDays())
      for (Time t : scheduler.getTimes()) {
        p.printf(columnFormat, d.getName());
        p.printf(columnFormat, t.getName());
        for (Court c : scheduler.getCourts()) {
          Match m = scheduler.getMatch(d, t, c);
          p.printf(columnFormat, teamsString(m));
        }
        p.println();
      }
    p.close();
    return w.toString();
  }

  private static String teamsString(Match m) {
    StringBuilder b = new StringBuilder();
    for (Team t : m.getTeams())
      b.append(t.getName()).append(',');
    b.setLength(b.length() - 1);
    return b.toString();
  }
}
