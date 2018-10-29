package cn.admin.script;

import com.eclipsesource.v8.*;

import java.io.*;
import java.util.Objects;

public class V8Test {

    static class Printer {

        public void print(final String message) {
            System.out.println(message);
        }

    }


    private V8 runtime = V8.createV8Runtime();

    static class Console {

        public void log(final String message) {
            System.out.println("[info] " + message);
        }

        public void error(final String message) {
            System.out.println("[error] " + message);
        }

    }


    void start() throws IOException {

        runtime.registerJavaMethod((receiver, parameters) -> {
            if (parameters.length() > 0) {
                Object arg1 = parameters.get(0);
                System.out.println(arg1);
                if (arg1 instanceof Releasable) {
                    ((Releasable) arg1).release();
                }
            }
        }, "print");
        String path = Objects.requireNonNull(V8Test.class.getClassLoader().getResource("shell")).getPath();
        File file = new File(path);
        File[] files = file.listFiles();
        StringBuilder sb = new StringBuilder();
        if (files != null) {
            for (File f : files) {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\r\n");
                }
            }
        }
        runtime.executeScript(sb.toString());
        runtime.executeScript("var db = new DB();" +
                "db.help()");
        while (true) {

        }
    }

    public static void main(String[] args) {
        try {
            new V8Test().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
