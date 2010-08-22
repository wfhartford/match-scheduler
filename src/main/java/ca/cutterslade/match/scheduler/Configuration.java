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

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Configuration {
  static final Configuration DEFAULT_CONFIGURATION = new Configuration(ImmutableMap.<SadFaceFactor, Integer> of());
  private final ImmutableMap<SadFaceFactor, Integer> factors;

  Configuration(Map<SadFaceFactor, Integer> factors) {
    this.factors = ImmutableMap.copyOf(factors);
  }

  int getFactor(SadFaceFactor factor) {
    Integer value = factors.get(factor);
    return null == value ? 1 : value.intValue();
  }
}