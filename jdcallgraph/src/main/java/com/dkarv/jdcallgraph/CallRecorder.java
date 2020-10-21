/*
 * MIT License
 * <p>
 * Copyright (c) 2017 David Krebs
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dkarv.jdcallgraph;

import com.dkarv.jdcallgraph.callgraph.CallGraph;
import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.StackItem;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class CallRecorder {
  private static final Logger LOG = new Logger(CallRecorder.class);

  /**
   * Collect the call graph per thread.
   */
  static final Map<Long, CallGraph> GRAPHS = new HashMap<>();
  public static void beforeMethod(StackItem item) {
    try {
      LOG.trace(">> {}{}", item, item.isReturnSafe() ? "" : " (return unsafe)");
      Long threadId = Thread.currentThread().getId();
      synchronized (GRAPHS) {
        if (!GRAPHS.containsKey(threadId)) {
          System.out.println("shiv: thread-id: " + threadId + "  name: " + Thread.currentThread().getName() + " Time: " + System.currentTimeMillis() + " map size: " + GRAPHS.size());

          GRAPHS.put(threadId, new CallGraph(threadId));
        }
      }
      GRAPHS.get(threadId).called(item);
    } catch (Throwable e) {
      LOG.error("Error in beforeMethod", e);
    }
  }


  public static void afterMethod(StackItem item) {
    try {
      LOG.trace("<< {}", item);
      long threadId = Thread.currentThread().getId();
      CallGraph graph = GRAPHS.get(threadId);
      if (graph == null) {
        // not interesting
        return;
      }
      graph.returned(item);
    } catch (Throwable e) {
      LOG.error("Error in afterMethod", e);
    }
  }

  public static void shutdown() {
    for (CallGraph g : GRAPHS.values()) {
      try {
        g.finish();
      } catch (IOException e) {
        LOG.error("Error finishing call graph {}", g, e);
      }
    }
  }
}
