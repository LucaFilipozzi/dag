// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Singular;

@Builder
public class User {
  @Singular
  private Set<String> challenges;

  public Set<String> getChallenges() {
    return Set.copyOf(challenges);
  }
}
