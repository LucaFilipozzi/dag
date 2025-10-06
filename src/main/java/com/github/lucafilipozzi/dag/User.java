// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.util.HashSet;
import java.util.Set;

public class User {
  private Set<String> challenges;

  private User() {
    challenges = new HashSet<>();
  }

  public static User of(Set<String> challenges) {
    User user = new User();
    user.challenges = Set.copyOf(challenges);
    return user;
  }

  public Set<String> getChallenges() {
    return Set.copyOf(challenges);
  }
}
