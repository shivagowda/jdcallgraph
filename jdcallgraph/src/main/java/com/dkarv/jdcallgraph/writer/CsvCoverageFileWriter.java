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
package com.dkarv.jdcallgraph.writer;

import com.dkarv.jdcallgraph.util.StackItem;

import java.io.IOException;
import java.util.*;

public class CsvCoverageFileWriter implements GraphWriter {
  FileWriter writer;

  private final Map<StackItem, Set<StackItem>> usedIn = new HashMap<>();
  private StackItem currentItem;

  public CsvCoverageFileWriter() throws IOException {
      if (writer == null) {
        writer = new FileWriter("/cg/coverage.csv");
      }
  }

  @Override
  public void start(String identifier) throws IOException {

  }

  @Override
  public void node(StackItem method) throws IOException {
    currentItem = method.isTestMethod() ? method : null;
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
    if(currentItem == null && from.isTestMethod()) {
      currentItem = from;
    }

    usedIn.putIfAbsent(to, new HashSet<StackItem>());
    usedIn.get(to).add(currentItem);
  }

  @Override
  public void edge(StackItem from, StackItem to, String label) throws IOException {
    this.edge(from, to);
  }

  @Override
  public void end() throws IOException {
  }

  @Override
  public void close() throws IOException {
    for (Map.Entry<StackItem, Set<StackItem>> entry : usedIn.entrySet()) {
      if(entry.getValue().size() == 0) continue;
      String key = entry.getKey().toString();
      boolean sourceAdded = false;
      for (StackItem item : entry.getValue()) {
        if(item != null) {
          if (!sourceAdded) {
            writer.append(key);
            sourceAdded = true;
          }
          writer.append(';');
          writer.append(item.toString());
        }
      }
      if(sourceAdded) {
        writer.append('\n');
      }
    }
    writer.close();
  }
}
