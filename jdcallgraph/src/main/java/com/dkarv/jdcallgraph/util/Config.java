package com.dkarv.jdcallgraph.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class Config {
    public static String OUT_DIR = null;

    /**
     * The available log levels are: <br/>
     * 0 | Off <br/>
     * 1 | Fatal <br/>
     * 2 | Error <br/>
     * 3 | Warn <br/>
     * 4 | Info <br/>
     * 5 | Debug <br/>
     * 6 | Trace <br/>
     */
    public static int LOG_LEVEL = 7;
    public static boolean LOG_STDOUT = false;

    public static void load(String path) throws IOException {
        final File file = new File(path);

        for (String line : Files.readAllLines(file.toPath(), Charset.defaultCharset())) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] args = line.split(":", 2);
            if (args.length < 2) {
                throw new IllegalArgumentException("Invalid line in config file: " + line);
            }

            if ("outDir".equals(args[0])) {
                OUT_DIR = args[1].trim();
                if (!OUT_DIR.endsWith(File.separator)) {
                    OUT_DIR = OUT_DIR + File.separator;
                }
            } else if ("logLevel".equals(args[0])) {
                LOG_LEVEL = Integer.parseInt(args[1].trim());
                if (LOG_LEVEL < 0 || LOG_LEVEL > 6) {
                    throw new IllegalArgumentException("Invalid log level: " + LOG_LEVEL);
                }
            } else if ("logStdout".equals(args[0])) {
                LOG_STDOUT = Boolean.parseBoolean(args[1].trim());
            } else {
                throw new IllegalArgumentException("Unknown config option: " + line);
            }
        }

        checkValues();
    }

    private static void checkValues() {
        if (OUT_DIR == null) {
            throw new IllegalArgumentException("Please specify an output directory in your config");
        }
    }
}