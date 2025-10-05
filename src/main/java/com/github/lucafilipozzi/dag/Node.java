// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@Data
public class Node {
  public enum STATUS {
    UNTRIED,
    SUCCESS,
    FAILURE
  }

  private String id;

  @EqualsAndHashCode.Exclude
  @Builder.Default
  private STATUS status = STATUS.UNTRIED;

  public String getChallenge() {
    return id.split("-")[0];
  }
}
