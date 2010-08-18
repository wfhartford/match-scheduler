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

final class Time {
  private final String name;

  static ImmutableSet<Time> forNames(Set<String> names) {
    ImmutableSet.Builder<Time> b = ImmutableSet.builder();
    for (String name : names)
      b.add(new Time(name));
    return b.build();
  }

  Time(String name) {
    if (null == name) throw new IllegalArgumentException("name may not be null");
    this.name = name;
  }

  String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Time other = (Time) obj;
    if (!name.equals(other.name)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Time [name=" + name + "]";
  }
}
