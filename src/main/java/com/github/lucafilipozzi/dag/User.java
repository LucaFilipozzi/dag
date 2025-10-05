// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class User {
  private Set<String> challenges;
}
