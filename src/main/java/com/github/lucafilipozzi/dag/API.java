// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class API {
  String registry;

  private API() { /* hide constructor */ }

  public static API of(Reader reader, User user) {
    API api = new API();
    api.saveToRegistry(MyGraph.of(reader, user));
    return api;
  }

  private MyGraph loadFromRegistry() {
    return MyGraph.load(new StringReader(registry));
  }

  private void saveToRegistry(MyGraph myGraph) {
    StringWriter stringWriter = new StringWriter();
    myGraph.dump(stringWriter);
    registry = stringWriter.toString();
  }

  public String get() {
    MyGraph myGraph = loadFromRegistry();
    try {
      return myGraph.getChallenge(); // return 2xx
    } catch (RuntimeException ignored) {
      return null;                   // return 4xx
    } finally {
      saveToRegistry(myGraph);
    }
  }

  public String post(String status) {
    if (!List.of("success", "failure").contains(status)) {
      return "invalid";      // return 4xx
    }
    MyGraph myGraph = loadFromRegistry();
    try {
      myGraph.setStatus(status);
      myGraph.getChallenge();
      return "continue";     // return 2xx
    } catch (MyGraph.GetChallengeFailure ignored) {
      return "failure";      // return 2xx
    } catch (MyGraph.GetChallengeSuccess ignored) {
      return "success";      // return 2xx
    } catch (MyGraph.SetStatusException ignored) {
      return "invalid";      // return 4xx
    } finally {
      saveToRegistry(myGraph);
    }
  }
}
