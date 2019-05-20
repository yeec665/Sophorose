package indi.hiro.pagoda;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Hiro on 2019/4/3.
 */
public class Terminal implements Runnable {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static final ArrayList<Terminal> TERMINALS = new ArrayList<>();
    static final SqlHelper SQL_HELPER = new SqlHelper();
    static final IdsWorker IDS_WORKER = new IdsWorker(SQL_HELPER);

    static volatile boolean SYSTEM_RUNNING = true;

    public static void main(String[] args) {
        connectToDatabase(args);
        IDS_WORKER.buildSrcAddrWhiteList(SQL_HELPER.getHost());
        synchronized (TERMINALS) {
            TERMINALS.add(create());
        }
        System.out.println("Terminal ready");
    }

    static void connectToDatabase(String[] args) {
        switch (args.length) {
            case 1:
                SQL_HELPER.setHost(args[0]);
                break;
            case 3:
                SQL_HELPER.setHost(args[0]);
                SQL_HELPER.setUsername(args[1]);
                SQL_HELPER.setPassword(args[2]);
                break;
            case 4:
                SQL_HELPER.setHost(args[0]);
                SQL_HELPER.setDatabase(args[1]);
                SQL_HELPER.setUsername(args[2]);
                SQL_HELPER.setPassword(args[3]);
                break;
        }
        SQL_HELPER.testConnect();
    }

    static void handleMessage(String msg) {
        if (msg == null) {
            return;
        }
        int atSign = msg.lastIndexOf('@');
        if (atSign == -1) {
            return;
        }
        String content = msg.substring(0, atSign);
        String destination = msg.substring(atSign + 1).trim();
        if (destination.equalsIgnoreCase("IDS")) {
            IDS_WORKER.receive(content);
        } else if (destination.equalsIgnoreCase("SQL")) {
            SQL_HELPER.enqueue(content);
        }else if (destination.equalsIgnoreCase("CMD")) {
            executeCommand(content);
        }  else {
            try {
                int index = Integer.parseInt(destination);
                synchronized (TERMINALS) {
                    BufferedWriter terminalWriter = TERMINALS.get(index).stdInWriter;
                    terminalWriter.write(content);
                    terminalWriter.newLine();
                    terminalWriter.flush();
                }
            } catch (IndexOutOfBoundsException e1) {
                System.err.println("Terminal index out of bounds");
            } catch (NumberFormatException e2) {
                System.err.println("No such terminal");
            } catch (Exception e3) {
                // ignored
            }
        }
    }

    static void executeCommand(String msg) {
        if (msg.trim().equalsIgnoreCase("exit")) {
            exit();
        }
        try {
            int index;
            Terminal terminal = create(Runtime.getRuntime().exec(msg));
            synchronized (TERMINALS) {
                index = TERMINALS.size();
                TERMINALS.add(terminal);
            }
            System.out.println("New process added as terminal " + index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void exit() {
        SYSTEM_RUNNING = false;
        System.out.println("Bye");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            //ignored
        }
        System.exit(0);
    }

    static Terminal create() {
        return create(System.in, System.out);
    }

    static Terminal create(Process process) {
        return create(process.getInputStream(), process.getOutputStream());
    }

    static Terminal create(InputStream stdOut, OutputStream stdIn) {
        BufferedReader stdOutReader = null;
        if (stdOut != null) {
            stdOutReader = new BufferedReader(new InputStreamReader(stdOut));
        }
        BufferedWriter stdInWriter = null;
        if (stdIn != null) {
            stdInWriter = new BufferedWriter(new OutputStreamWriter(stdIn));
        }
        return new Terminal(stdOutReader, stdInWriter);
    }

    final BufferedReader stdOutReader;
    final BufferedWriter stdInWriter;

    Terminal(BufferedReader stdOutReader, BufferedWriter stdInWriter) {
        this.stdOutReader = stdOutReader;
        this.stdInWriter = stdInWriter;
        if (stdOutReader != null) {
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        try {
            while (SYSTEM_RUNNING) {
                handleMessage(stdOutReader.readLine());
            }
        } catch (IOException e) {
            // ignored
        }
    }
}
