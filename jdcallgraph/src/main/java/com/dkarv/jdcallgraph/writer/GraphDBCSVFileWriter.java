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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public class GraphDBCSVFileWriter implements GraphWriter {
  private FileWriter writer;

  private final Set<StackItem> trace = new HashSet<>();

  public GraphDBCSVFileWriter(long threadId) throws IOException {
      if (writer == null) {
        writer = new FileWriter("/cg/graphdb-" + threadId +".csv");
        //TODO: below line is getting printed multiple times within a thread.
//        writer.append("from_id|from_label|from_package|from_class|from_method|from_params|to_id|to_label|to_package|to_class|to_method|to_params\n");
      }
  }
  @Override
  public void start(String identifier) throws IOException {
  }

  @Override
  public void node(StackItem method) throws IOException {
//    writer.append(method.toString());
//    trace.clear();
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
    StringBuilder line = new StringBuilder();
    String fromString = from.toGraphDBCSV();
    String toString = to.toGraphDBCSV();

    line.append(fromString.hashCode()).append('|');
    line.append(fromString).append('|');
    line.append(toString.hashCode()).append('|');
    line.append(toString).append('\n');
    //There should be a single call to writer.append(), otherwise multiple threads will jumble up the data
    writer.append(line.toString());
  }

  @Override
  public void edge(StackItem from, StackItem to, String label) throws IOException {
    this.edge(from, to);
  }

  @Override
  public void end() throws IOException {
//    writer.append('\n');
  }

  @Override
  public void close() throws IOException {
    if(writer == null) {
      //TODO: log error
    } else {
      writer.close();
    }
  }
}
