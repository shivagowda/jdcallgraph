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
package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.util.options.Target;
import com.dkarv.jdcallgraph.writer.*;
import com.dkarv.jdcallgraph.util.*;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;

import java.io.*;
import java.util.*;

public class CallGraph {
  private static final Logger LOG = new Logger(CallGraph.class);
  private final long threadId;
  final Stack<StackItem> calls = new Stack<>();

  final List<GraphWriter> writers = new ArrayList<>();

  public CallGraph(long threadId) throws IOException {
    this.threadId = threadId;
    Target[] targets = Config.getInst().writeTo();
    for (Target target : targets) {
        writers.add(createWriter(target, false));
    }
  }

  public GraphWriter createWriter(Target t, boolean multiGraph) throws IOException {
    // TODO redo this with new remove duplicate strategies
    switch (t) {
      case COVERAGE:
        return new CsvCoverageFileWriter(this.threadId);
      case COVERAGE_JSON:
        return new JSONCoverageFileWriter(this.threadId);
      case TRACE:
        return new CsvTraceFileWriter();
      case GRAPH_DB:
        return new GraphDBTraceFileWriter();
      case GRAPH_DB_CSV:
        return new GraphDBCSVFileWriter(this.threadId);
      default:
        throw new IllegalArgumentException("Unknown writeTo: " + t);
    }
  }

  public void called(StackItem method) throws IOException {

    if (calls.isEmpty() && method.isTestMethod()) { //first item pushed to stack should be test

      // First node
      calls.push(method);
      for (GraphWriter w : writers) {
        w.node(method);
      }

    } else { //it's not a first node in the stack
      StackItem top = calls.peek();

      for (GraphWriter w : writers) {
        w.edge(top, method);
      }
      calls.push(method);
    }
  }

  public void returned(StackItem method) throws IOException {

    boolean found = false;
    while (!calls.isEmpty()) {
      StackItem topItem = calls.pop();
      if (topItem.equals(method)) {
        break;
      }
    }

    if (calls.isEmpty()) {
      for (GraphWriter w : writers) {
        w.end();
      }
    }
  }

  public void finish() throws IOException {
    for (GraphWriter w : writers) {
      w.close();
    }
  }
}
