package com.appresizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class ShellUtils {

    public static final class Result {

        public final int code;
        public final String output;

        Result(int code, String output) {
            this.code = code;
            this.output = output;
        }

        public boolean ok() {
            return code == 0;
        }
    }

    private ShellUtils() {
    }

    public static boolean hasRoot() {
        Result result = execRoot("id");
        return result.ok() && result.output.contains("uid=0");
    }

    public static Result execRoot(String command) {
        return exec(new String[]{"su", "-c", command});
    }

    public static Result exec(String[] command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            StringBuilder builder = new StringBuilder();
            read(process.getInputStream(), builder);
            read(process.getErrorStream(), builder);
            int code = process.waitFor();
            return new Result(code, builder.toString());
        } catch (Throwable throwable) {
            return new Result(-1, String.valueOf(throwable));
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static void read(InputStream stream, StringBuilder builder) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (Throwable ignored) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
