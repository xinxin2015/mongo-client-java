package cn.admin.cmd;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Process p = null;
        ProcessBuilder pb;
        String jsPath =
                Objects.requireNonNull(Main.class.getClassLoader().getResource("test.js")).getPath().substring(1);
        pb = new ProcessBuilder("mongo","--port","27017","test1111","--quiet",jsPath);
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (p != null) {
            new Thread(new ErrorThread(p.getErrorStream())).start();
            new Thread(new InputThread(p.getInputStream())).start();
        }

        Scanner s = new Scanner(System.in);
        OutputStreamWriter writer = new OutputStreamWriter(p.getOutputStream());
        try {
            while (true) {
                String line = s.nextLine();
                System.out.println(line + "\n");
                writer.write(line + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static class ErrorThread implements Runnable {

        BufferedReader reader;

        ErrorThread(InputStream in) {
            reader = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public void run() {
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class InputThread implements Runnable {

        BufferedReader reader;
        InputStream in;

        InputThread(InputStream in) {
            this.in = in;
            reader = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public void run() {
            String line;
            System.out.println(in.getClass());
            try {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("read complete");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
