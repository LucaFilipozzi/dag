// © 2025 Luca Filipozzi. Some rights reserved. See LICENSE.
package com.github.lucafilipozzi.dag;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class API {
  String processManager;

  private API() { /* hide constructor */ }

  /**
   * Creates a new instance of {@code API} by processing data from the given {@code Reader}
   * and {@code User}. This method initializes an {@code API} object and saves the processed
   * {@code MyGraph} data to the process manager.
   *
   * @param reader the source of data to construct the {@code MyGraph} in GraphML format
   * @param user the user providing challenges for the {@code MyGraph}
   * @return a new instance of {@code API} initialized with the given data
   */
  public static API of(Reader reader, User user) {
    API api = new API();
    api.saveToProcessManager(MyGraph.of(reader, user));
    return api;
  }

  /**
   * Loads and returns a {@code MyGraph} object from the data stored in the process manager.
   * The method reads the serialized {@code MyGraph} data from the {@code processManager}
   * string in JSON format and reconstructs a {@code MyGraph} instance.
   *
   * @return a {@code MyGraph} object reconstructed from the process manager data
   */
  private MyGraph loadFromProcessManager() {
    return MyGraph.load(new StringReader(processManager));
  }

  /**
   * Serializes to JSON the given {@code MyGraph} object and stores its data in the process manager.
   *
   * @param myGraph the {@code MyGraph} instance to serialize and save into the process manager
   */
  private void saveToProcessManager(MyGraph myGraph) {
    StringWriter stringWriter = new StringWriter();
    myGraph.dump(stringWriter);
    processManager = stringWriter.toString();
  }

  /**
   * Retrieves the challenge string from the {@code MyGraph} instance loaded from the process manager.
   * If the operation fails due to a runtime exception, the method returns {@code null}.
   *
   * @return a string representing the challenge from the {@code MyGraph} on success,
   *         or {@code null} if an exception occurs during retrieval
   */
  public String get() {
    MyGraph myGraph = loadFromProcessManager();
    try {
      return myGraph.getChallenge(); // return 2xx
    } catch (RuntimeException ignored) {
      return null;                   // return 4xx
    }
  }

  /**
   * Updates the status of the {@code MyGraph} object.
   * The status must be either "success" or "failure". If an invalid status is provided,
   * an error code is returned. Based on the execution of the {@code MyGraph} methods,
   * this method may return different result codes.
   *
   * @param status the new status to be set for the {@code MyGraph} instance, allowed values
   *               are "success" or "failure"
   * @return a string indicating the result of the operation:
   *         - "invalid" for an invalid status or an exception in setting the status,
   *         - "continue" if setting the status succeeded and the challenge is processed,
   *         - "failure" if verification has failed (no shortest path exists).
   *         - "success" if verification has succeeded (no further untried challenges).
   */
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
