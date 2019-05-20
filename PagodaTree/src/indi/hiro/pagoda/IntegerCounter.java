package indi.hiro.pagoda;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public class IntegerCounter {

    private static final int MIN_CAPACITY = 4;
    private static final int DEFAULT_CAPACITY = 6;

    /**
     * Index 0, 2, 4, ... : integer
     * Index 1, 3, 5, ... : occurrence count
     * All counts are positive
     */
    private int[] ii;

    private int dSize;
    private int iSum;

    public IntegerCounter(int initialCapacity) {
        initialCapacity = Math.max(MIN_CAPACITY, initialCapacity);
        ii = new int[2 * initialCapacity];
    }

    public IntegerCounter() {
        this(DEFAULT_CAPACITY);
    }

    public int size() {
        return dSize >>> 1;
    }

    public void iterateK(IntConsumer ic) {
        for (int p = 0; p < dSize; p += 2) {
            ic.accept(ii[p]);
        }
    }

    public void iterateKV(IntIntConsumer iic) {
        for (int p = 0; p < dSize; p += 2) {
            iic.accept(ii[p], ii[p + 1]);
        }
    }

    public void iterateIP(Consumer<IntPair> ipc) {
        for (int p = 0; p < dSize; p += 2) {
            ipc.accept(new IntPair(ii[p], ii[p + 1]));
        }
    }

    @Deprecated
    private int indexWithLinearSearch(int x) {
        int p = 0;
        while (p < dSize) {
            if (ii[p] < x) {
                p += 2;
            } else {
                break;
            }
        }
        return p;
    }

    private int indexWithBinarySearch(int x) {
        int pL = 0, pR = dSize;
        while (pL < pR) {
            int pM = ((pL + pR) >>> 1) & ~1;
            int mx = ii[pM];
            if (mx < x) {
                pL = pM + 2;
            } else if (mx > x) {
                pR = pM;
            } else {
                return pM;
            }
        }
        return pL;
    }

    public int count(int x) {
        int p = indexWithBinarySearch(x);
        if (p < dSize && ii[p] == x) {
            return ii[p + 1];
        }
        return 0;
    }

    public int countAll() {
        return iSum;
    }

    private void addSum(int n) {
        int nSum = iSum + n;
        if (nSum < 0) {
            throw new RuntimeException();
        }
        iSum = nSum;
    }

    private int addP(int x, int n) {
        addSum(n);
        int p = indexWithBinarySearch(x);
        if (p < dSize && ii[p] == x) {
            return ii[p + 1] += n;
        }
        if (dSize + 2 > ii.length) {
            ii = Arrays.copyOf(ii, ii.length << 1);
        }
        System.arraycopy(ii, p, ii, p + 2, dSize - p);
        dSize += 2;
        ii[p] = x;
        return ii[p + 1] = n;
    }

    private int minusP(int x, int n) {
        int p = indexWithBinarySearch(x);
        if (p < dSize && ii[p] == x) {
            int pn = ii[p + 1];
            if (pn > n) {
                addSum(-n);
                return ii[p + 1] = pn - n;
            } else {
                addSum(-pn);
                dSize -= 2;
                System.arraycopy(ii, p + 2, ii, p, dSize - p);
            }
        }
        return 0;
    }

    public void addOne(int x) {
        addP(x, 1);
    }

    public void minusOne(int x) {
        minusP(x, 1);
    }

    public void add(int[] xx) {
        for (int x : xx) {
            addP(x, 1);
        }
    }

    public void add(int[] xx, int off, int len) {
        for (int lim = off + len; off < lim; off++) {
            addP(xx[off], 1);
        }
    }

    public void add(Integer[] xx) {
        for (int x : xx) {
            addP(x, 1);
        }
    }

    public void add(int x, int n) {
        if (n > 0) {
            addP(x, n);
        } else if (n < 0) {
            minusP(x, -n);
        }
    }

    public int addAndCount(int x, int n) {
        if (n > 0) {
            return addP(x, n);
        } else if (n < 0) {
            return minusP(x, -n);
        } else {
            return count(x);
        }
    }

    public void removeIfV(IntPredicate predicate) {
        int p1 = 0, p2 = 0;
        int niSum = 0;
        while (p2 < dSize) {
            int pn = ii[p2 + 1];
            if (!predicate.test(pn)) {
                niSum += pn;
                if (p1 < p2) {
                    ii[p1] = ii[p2];
                    ii[p1 + 1] = pn;
                }
                p1 += 2;
            }
            p2 += 2;
        }
        dSize = p1;
        iSum = niSum;
    }

    public void removeIfKV(IntIntPredicate predicate) {
        int p1 = 0, p2 = 0;
        int niSum = 0;
        while (p2 < dSize) {
            int pn = ii[p2 + 1];
            if (!predicate.test(ii[p2], pn)) {
                niSum += pn;
                if (p1 < p2) {
                    ii[p1] = ii[p2];
                    ii[p1 + 1] = pn;
                }
                p1 += 2;
            }
            p2 += 2;
        }
        dSize = p1;
        iSum = niSum;
    }

    public IntPair maxV() {
        int maxP = -1;
        int maxV = 0;
        for (int p = 0; p < dSize; p += 2) {
            int pn = ii[p + 1];
            if (pn > maxV) {
                maxP = p;
                maxV = pn;
            }
        }
        if (maxP >= 0) {
            return new IntPair(ii[maxP], ii[maxP + 1]);
        } else {
            return null;
        }
    }

    public IntPair maxV(int lowerBound) {
        int maxP = -1;
        int maxV = 0;
        for (int p = 0; p < dSize; p += 2) {
            if (ii[p] > lowerBound && ii[p + 1] > maxV) {
                maxP = p;
                maxV = ii[p + 1];
            }
        }
        if (maxP >= 0) {
            return new IntPair(ii[maxP], ii[maxP + 1]);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int p = 0; p < dSize; p += 2) {
            if (p > 0) {
                sb.append(", ");
            }
            sb.append(ii[p]).append('=').append(ii[p + 1]);
        }
        return sb.append("}").toString();
    }

    public static class IntPair {

        public int a;
        public int b;

        public IntPair(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    @FunctionalInterface
    public interface IntIntConsumer {

        void accept(int a, int b);
    }

    @FunctionalInterface
    public interface IntIntPredicate {

        boolean test(int a, int b);
    }
}
