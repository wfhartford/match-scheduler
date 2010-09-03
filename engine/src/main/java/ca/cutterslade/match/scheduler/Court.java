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

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class Court implements Serializable {

  /**
   * 1
   */
  private static final long serialVersionUID = 1L;

  private final String name;

  private final Gym gym;

  static ImmutableSet<Court> forNames(Set<String> names, Set<Gym> gyms) {
    ImmutableSet.Builder<Court> b = ImmutableSet.builder();
    for (Gym gym : gyms)
      for (String name : names)
        b.add(new Court(name, gym));
    return b.build();
  }

  Court(String name, Gym gym) {
    if (null == name) throw new IllegalArgumentException("name may not be null");
    if (null == gym) throw new IllegalArgumentException("gym may not be null");
    this.name = name;
    this.gym = gym;
  }

  public String getName() {
    return name;
  }

  public Gym getGym() {
    return gym;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + gym.hashCode();
    result = prime * result + name.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Court other = (Court) obj;
    if (!gym.equals(other.gym)) return false;
    if (!name.equals(other.name)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Court [gym=" + gym + ", name=" + name + "]";
  }

}
