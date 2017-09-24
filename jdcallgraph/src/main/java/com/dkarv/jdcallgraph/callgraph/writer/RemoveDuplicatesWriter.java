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
package com.dkarv.jdcallgraph.callgraph.writer;

import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.log.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A writer that can wrap another writer and forwards nodes and edges
 * only if they did not happen before.
 */
public class RemoveDuplicatesWriter implements GraphWriter {
  private final static Logger LOG = new Logger(RemoveDuplicatesWriter.class);

  private final GraphWriter parentWriter;
  private final HashMap<StackItem, HashSet<StackItem>> edges = new HashMap<>();

  public RemoveDuplicatesWriter(GraphWriter parentWriter) {
    this.parentWriter = parentWriter;
  }

  @Override
  public void start(String identifier) throws IOException {
    parentWriter.start(identifier);
  }

  @Override
  public void node(StackItem method, boolean isTest) throws IOException {
    parentWriter.node(method, isTest);
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
    boolean duplicate = false;
    HashSet<StackItem> set = edges.get(from);
    if (set != null) {
      if (set.contains(to)) {
        duplicate = true;
      } else {
        set.add(to);
      }
    } else {
      set = new HashSet<>();
      set.add(to);
      edges.put(from, set);
    }

    if (!duplicate) {
      parentWriter.edge(from, to);
    }
  }

  @Override
  public void end() throws IOException {
    parentWriter.end();
    edges.clear();
  }

  @Override
  public void close() throws IOException {
    parentWriter.close();
  }
}
