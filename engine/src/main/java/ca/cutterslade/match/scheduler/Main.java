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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.google.common.collect.ImmutableMap;

/**
 * @author W.F. Hartford
 * 
 */
public class Main {

  private static final Options OPTIONS;
  static {
    final Options o = new Options();
    o.addOption(OptionBuilder.withLongOpt("teams").hasArg().withArgName("count").withDescription("The total number of teams").isRequired().create('t'));
    o.addOption(OptionBuilder.withLongOpt("tiers").hasArg().withArgName("count").withDescription("The number of tiers that the teams are divided into").isRequired().create('r'));
    o.addOption(OptionBuilder.withLongOpt("gyms").hasArg().withArgName("count").withDescription("The number of gyms in which matches are played").isRequired().create('g'));
    o.addOption(OptionBuilder.withLongOpt("courts").hasArg().withArgName("count").withDescription("The number of courts in each gym").isRequired().create('c'));
    o.addOption(OptionBuilder.withLongOpt("times").hasArg().withArgName("count").withDescription("The number of time slots per day").isRequired().create('m'));
    o.addOption(OptionBuilder.withLongOpt("days").hasArg().withArgName("count").withDescription("The number of play days in the season").isRequired().create('d'));
    o.addOption(OptionBuilder.withLongOpt("randomMatches").withDescription("Randomize the order of teams within each match").create());
    o.addOption(OptionBuilder.withLongOpt("randomSlots").withDescription("Randomize the order of slots within each day").create());
    o.addOption(OptionBuilder.withLongOpt("randomDays").withDescription("Randomize the order of days within each season").create());
    o.addOption(OptionBuilder.withLongOpt("random").withDescription("Randomize all of the above").create());
    OPTIONS = o;
  }

  public static void main(final String[] args) throws InterruptedException {
    final CommandLineParser parser = new PosixParser();
    try {
      final CommandLine line = parser.parse(OPTIONS, args);
      final int teams = Integer.parseInt(line.getOptionValue('t'));
      final int tiers = Integer.parseInt(line.getOptionValue('r'));
      final int gyms = Integer.parseInt(line.getOptionValue('g'));
      final int courts = Integer.parseInt(line.getOptionValue('c'));
      final int times = Integer.parseInt(line.getOptionValue('m'));
      final int days = Integer.parseInt(line.getOptionValue('d'));
      final Configuration config;
      if (line.hasOption("random")) config = Configuration.RANDOM_CONFIGURATION;
      else config = new Configuration(ImmutableMap.<SadFaceFactor, Integer> of(), line.hasOption("randomMatches"), line.hasOption("randomSlots"), line.hasOption("randomDays"));
      final Scheduler s = new Scheduler(config, teams, tiers, gyms, courts, times, days);
      System.out.println(summary(s));
    }
    catch (final ParseException e) {
      final HelpFormatter f = new HelpFormatter();
      final PrintWriter pw = new PrintWriter(System.err);
      f.printHelp(pw, 80, "match-scheduler", null, OPTIONS, 2, 2, null, true);
      pw.close();
      System.err.println("For example: match-scheduler -t 48 -r 3 -d 12 -m 2 -g 3 -c 2 --randomMatches --randomSlots");
    }
  }

  private static String summary(final Scheduler scheduler) {
    final StringWriter w = new StringWriter();
    final PrintWriter p = new PrintWriter(w);
    final String columnFormat = " %12s";
    p.printf(columnFormat, "Day");
    p.printf(columnFormat, "Time");
    for (final Court c : scheduler.getCourts()) {
      p.printf(columnFormat, c.getGym().getName() + '-' + c.getName());
    }
    p.println();
    for (final Day d : scheduler.getDays())
      for (final Time t : scheduler.getTimes()) {
        p.printf(columnFormat, d.getName());
        p.printf(columnFormat, t.getName());
        for (final Court c : scheduler.getCourts()) {
          final Match m = scheduler.getMatch(d, t, c);
          p.printf(columnFormat, teamsString(m));
        }
        p.println();
      }
    p.close();
    return w.toString();
  }

  private static String teamsString(final Match m) {
    final StringBuilder b = new StringBuilder();
    for (final Team t : m.getTeams())
      b.append(t.getName()).append(',');
    b.setLength(b.length() - 1);
    return b.toString();
  }
}
