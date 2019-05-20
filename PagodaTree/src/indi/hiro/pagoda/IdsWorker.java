package indi.hiro.pagoda;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hiro on 2019/4/6.
 */
public class IdsWorker implements Runnable {

    static final int SYSTEM_SYNCHRONIZE = 5 * 1000; // 5s
    static final int STORAGE_WINDOW = 3 * 60 * 1000; // 3min
    @SuppressWarnings("PointlessArithmeticExpression")
    static final int MEMORY_TIMEOUT = 1 * 60 * 1000; // 1min
    static final int MEMORY_USED_TO_TIMEOUT = 6 * 60 * 60 * 1000; // 6h

    static final int RECOGNIZE_THRESHOLD = 50;

    static final Pattern NCAP_PATTERN = Pattern.compile("^\\{([\\d,]+)}$");
    static final Pattern NCAP_SPLIT = Pattern.compile(",");

    final SqlHelper sqlHelper;

    final ArrayList<Integer> srcAddrWhiteList = new ArrayList<>();
    final LinkedList<PacketRecord> records = new LinkedList<>();
    final ConcurrentLinkedQueue<PacketRecord> newRecords = new ConcurrentLinkedQueue<>();
    final ArrayList<RecognizedIntrusion> intrusions = new ArrayList<>();

    public IdsWorker(SqlHelper sqlHelper) {
        this.sqlHelper = sqlHelper;
        Thread thread = new Thread(this, "IdsSynchronize");
        thread.start();
    }

    public void buildSrcAddrWhiteList(String databaseHost) {
        synchronized (srcAddrWhiteList) {
            hostToWhiteList(databaseHost);
            //hostToWhiteList("100.100.30.25");
            hostToWhiteList("localhost");
            System.out.println("SrcAddrWhiteList :");
            for (int a : srcAddrWhiteList) {
                System.out.print(addrToString(a));
                System.out.print("; ");
            }
        }
        System.out.println();
    }

    private void hostToWhiteList(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress a : addresses) {
                int ia = bytesToAddr(a.getAddress());
                if (!srcAddrWhiteList.contains(ia)) {
                    srcAddrWhiteList.add(ia);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int bytesToAddr(byte[] a) {
        if (a != null && a.length == 4) {
            return 0xFF000000 & (a[0] << 24) | 0x00FF0000 & (a[1] << 16) | 0x0000FF00 & (a[2] << 8) | 0x000000FF & a[3];
        }
        return 0;
    }

    private String addrToString(int a) {
        return (0xFF & (a >> 24)) + "." + (0xFF & (a >> 16)) + "." + (0xFF & (a >> 8)) + "." + (0xFF & a);
    }

    public void receive(String ncap) {
        //System.out.println("IDS : " + ncap);
        try {
            Matcher matcher = NCAP_PATTERN.matcher(ncap);
            if (matcher.matches()) {
                String[] fields = NCAP_SPLIT.split(matcher.group(1));
                addRecord(new PacketRecord(
                        Long.parseLong(fields[0]),
                        Integer.parseInt(fields[1]),
                        Integer.parseInt(fields[2]),
                        Long.parseLong(fields[3]),
                        Long.parseLong(fields[4]),
                        Integer.parseInt(fields[5]),
                        Integer.parseInt(fields[6]),
                        Integer.parseInt(fields[7])
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addRecord(PacketRecord pr) {
        newRecords.add(pr);
    }

    @Override
    public void run() {
        while (Terminal.SYSTEM_RUNNING) {
            try {
                Thread.sleep(SYSTEM_SYNCHRONIZE);
            } catch (InterruptedException e) {
                continue;
            }
            workLoop();
        }
    }

    private void workLoop() {
        removeRecords();
        collectRecords();
        recognizeIntrusions();
    }

    private void removeRecords() {
        long time = System.currentTimeMillis();
        while (!records.isEmpty()) {
            if (time - records.getFirst().time > STORAGE_WINDOW) {
                records.removeFirst();
            } else {
                break;
            }
        }
        for (int i = intrusions.size() - 1; i >= 0; i--) {
            RecognizedIntrusion ri = intrusions.get(i);
            ri.intoDatabase(sqlHelper);
            if (time - ri.stopTime() > MEMORY_TIMEOUT) {
                intrusions.remove(i);
            } else if (time - ri.startTime() > MEMORY_USED_TO_TIMEOUT) {
                intrusions.remove(i);
                srcAddrWhiteList.add(ri.srcAddr);
            }
        }
    }

    private void collectRecords() {
        NEXT_PR:
        while (true) {
            PacketRecord pr = newRecords.poll();
            if (pr == null) {
                break;
            }
            synchronized (srcAddrWhiteList) {
                for (int sa : srcAddrWhiteList) {
                    if (pr.srcAddr == sa) {
                        continue NEXT_PR;
                    }
                }
            }
            pr.intoDatabase(sqlHelper);
            for (RecognizedIntrusion ri : intrusions) {
                if (pr.srcAddr == ri.srcAddr) {
                    ri.add(pr);
                    continue NEXT_PR;
                }
            }
            records.addLast(pr);
        }
    }

    private void recognizeIntrusions() {
        IntegerCounter ic = new IntegerCounter();
        for (PacketRecord pr : records) {
            ic.addOne(pr.srcAddr);
        }
        //System.out.println("IdsWorker.recognizeIntrusions : " + ic.toString());
        ic.removeIfV(v -> v < RECOGNIZE_THRESHOLD);
        ic.iterateK(this::collectRecords);
    }

    private void collectRecords(int srcAddr) {
        //System.out.println("IdsWorker.collectRecords : " + ipAddr(srcAddr));
        RecognizedIntrusion ri = new RecognizedIntrusion(srcAddr);
        records.removeIf(pr -> pr.srcAddr == srcAddr && ri.add(pr));
        if (ri.notEmpty()) {
            intrusions.add(ri);
        }
    }
}
