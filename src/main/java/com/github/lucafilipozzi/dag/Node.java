// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.util.Objects;

public class Node {
  public enum STATUS {
    UNTRIED,
    SUCCESS,
    FAILURE
  }

  private String id;
  private STATUS status;

  public static Node of(String id) {
    Node node = new Node();
    node.id = id;
    node.status = STATUS.UNTRIED;
    return node;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Node)) {
      return false;
    }
    return Objects.equals(((Node)other).id, this.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public String getId() {
    return id;
  }

  public void setStatus(STATUS status) {
    this.status = status;
  }

  public STATUS getStatus() {
    return status;
  }

  public String getChallenge() {
    return id.split("-")[0];
  }
}
