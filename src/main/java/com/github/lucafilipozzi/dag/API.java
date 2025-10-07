// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class API {
  String processManager;

  private API() { /* hide constructor */ }

  public static API of(Reader reader, User user) {
    API api = new API();
    api.saveToProcessManager(MyGraph.of(reader, user));
    return api;
  }

  private MyGraph loadFromProcessManager() {
    return MyGraph.load(new StringReader(processManager));
  }

  private void saveToProcessManager(MyGraph myGraph) {
    StringWriter stringWriter = new StringWriter();
    myGraph.dump(stringWriter);
    processManager = stringWriter.toString();
  }

  public String get() {
    MyGraph myGraph = loadFromProcessManager();
    try {
      return myGraph.getChallenge(); // return 2xx
    } catch (RuntimeException ignored) {
      return null;                   // return 4xx
    }
  }

  public String post(String status) {
    if (!List.of("success", "failure").contains(status)) {
      return "invalid";      // return 4xx
    }
    MyGraph myGraph = loadFromProcessManager();
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
      saveToProcessManager(myGraph);
    }
  }
}
