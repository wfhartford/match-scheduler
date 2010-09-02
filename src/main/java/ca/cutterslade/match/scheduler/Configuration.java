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
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Configuration implements Serializable {

  /**
   * 1
   */
  private static final long serialVersionUID = 1L;

  static final Configuration DEFAULT_CONFIGURATION = new Configuration(false);

  static final Configuration RANDOM_CONFIGURATION = new Configuration(true);

  private final ImmutableMap<SadFaceFactor, Integer> factors;

  private final boolean randomizeMatchOrder;

  private final boolean randomizeDayOrder;

  private final boolean randomizeSlotOrder;

  public Configuration(boolean random) {
    this(null, random, random, random);
  }

  public Configuration(Map<SadFaceFactor, Integer> factors, boolean random) {
    this(factors, random, random, random);
  }

  public Configuration(Map<SadFaceFactor, Integer> factors, boolean randomizeMatchOrder, boolean randomizeDayOrder, boolean randomizeSlotOrder) {
    this.factors = null == factors ? ImmutableMap.<SadFaceFactor, Integer> of() : ImmutableMap.copyOf(factors);
    this.randomizeMatchOrder = randomizeMatchOrder;
    this.randomizeDayOrder = randomizeDayOrder;
    this.randomizeSlotOrder = randomizeSlotOrder;
  }

  int getFactor(SadFaceFactor factor) {
    Integer value = factors.get(factor);
    return null == value ? factor.getDefaultValue() : value.intValue();
  }

  boolean isRandomizeMatchOrder() {
    return randomizeMatchOrder;
  }

  boolean isRandomizeDayOrder() {
    return randomizeDayOrder;
  }

  boolean isRandomizeSlotOrder() {
    return randomizeSlotOrder;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + factors.hashCode();
    result = prime * result + (randomizeDayOrder ? 1231 : 1237);
    result = prime * result + (randomizeMatchOrder ? 1231 : 1237);
    result = prime * result + (randomizeSlotOrder ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Configuration other = (Configuration) obj;
    if (!factors.equals(other.factors)) return false;
    if (randomizeDayOrder != other.randomizeDayOrder) return false;
    if (randomizeMatchOrder != other.randomizeMatchOrder) return false;
    if (randomizeSlotOrder != other.randomizeSlotOrder) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Configuration [factors=" + factors + ", randomizeMatchOrder=" + randomizeMatchOrder + ", randomizeDayOrder=" + randomizeDayOrder + ", randomizeSlotOrder=" + randomizeSlotOrder + "]";
  }

}