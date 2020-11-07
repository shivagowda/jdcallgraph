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
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JSONCoverageFileWriter implements GraphWriter {
  FileWriter writer;
  final int BUFFER_SIZE = 10;
  private Map<StackItem, Set<StackItem>> usedIn = new ConcurrentHashMap<>();
  private StackItem currentItem;

  public JSONCoverageFileWriter(long threadId) throws IOException {
      if (writer == null) {
        writer = new FileWriter("/cg/coverage-" + threadId +".json");
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

    if(currentItem != null) {
      writeToFile(to, currentItem);
    }
  }

  private void writeToFile(StackItem source, StackItem test) throws IOException {

    JSONObject json = new JSONObject();
    Map<String, Object> node = getStringObjectMap(source);
    json.put("source", node);

    node = getStringObjectMap(test);
    json.put("test", node);
    writer.append(json.toString()+'\n');
  }

  @Override
  public void edge(StackItem from, StackItem to, String label) throws IOException {
    this.edge(from, to);
  }

  //Same as close() without writer.close();
  @Override
  public void end() throws IOException {
    writeToFile();
  }

  @Override
  public void close() throws IOException {
    writeToFile();
    writer.close();
  }

  synchronized private void writeToFile() throws IOException {
    for (Map.Entry<StackItem, Set<StackItem>> entry : usedIn.entrySet()) {
      if (entry.getValue().size() == 0) continue;
      StackItem key = entry.getKey();
      JSONObject json = new JSONObject();
      List<Map<String, Object>> tests = new ArrayList<>();
      boolean sourceAdded = false;
      for (StackItem item : entry.getValue()) {
        if (item != null) {
          if (!sourceAdded) {
            Map<String, Object> source = getStringObjectMap(key);
            json.put("source", source);
            sourceAdded = true;
          }
          tests.add(getStringObjectMap(item));
        }
      }
      //remove map entry
      usedIn.remove(key);
      if (tests.size() > 0) {
        json.put("tests", tests);
      }
      if (json.length() > 0) {
        writer.append(json.toString()+'\n');
      }
//      if (sourceAdded) {
//        writer.append('\n');
//      }
    }
    usedIn = new HashMap<>();
  }

  private Map<String, Object> getStringObjectMap(StackItem key) {
    String parameters = key.getMethodParameters();
    if(parameters.isEmpty()) {
      parameters = "void";
    }
    Map<String, Object> source = new HashMap<>();
    source.put("package",key.getPackageName());
    source.put("class", key.getShortClassName());
    source.put("method", key.getShortMethodName());
    source.put("params", parameters);
    int hashCode = source.hashCode();
    source.put("id", hashCode);
    return source;
  }

}
