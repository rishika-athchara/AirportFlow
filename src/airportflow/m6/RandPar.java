package airportflow.m6;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

/** Module 6 (CO6): randomized and parallel algorithms. */
public final class RandPar {
    private RandPar() {}

    /** In-place Lomuto QuickSort; returns number of comparisons. Expected O(n log n) when randomized. */
    public static long quicksort(int[] a, boolean randomized, Random rnd) {
        long cmp = 0;
        Deque<int[]> st = new ArrayDeque<>();
        st.push(new int[]{0, a.length - 1});
        while (!st.isEmpty()) {
            int[] r = st.pop();
            int lo = r[0], hi = r[1];
            if (lo >= hi) continue;
            if (randomized) { int p = lo + rnd.nextInt(hi - lo + 1); int t = a[p]; a[p] = a[hi]; a[hi] = t; }
            int pv = a[hi], i = lo;
            for (int j = lo; j < hi; j++) {
                cmp++;
                if (a[j] <= pv) { int t = a[i]; a[i] = a[j]; a[j] = t; i++; }
            }
            int t = a[i]; a[i] = a[hi]; a[hi] = t;
            st.push(new int[]{lo, i - 1});
            st.push(new int[]{i + 1, hi});
        }
        return cmp;
    }

    /** Algorithm R: uniform k-sample from a stream of unknown length, O(k) memory. */
    public static <T> List<T> reservoir(Iterator<T> stream, int k, Random rnd) {
        List<T> res = new ArrayList<>();
        for (long i = 0; stream.hasNext(); i++) {
            T x = stream.next();
            if (i < k) res.add(x);
            else {
                long j = (long) (rnd.nextDouble() * (i + 1));
                if (j < k) res.set((int) j, x);
            }
        }
        return res;
    }

    private static final int[] SMALL = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};

    /** Probabilistic primality test, error <= 4^-rounds. */
    public static boolean millerRabin(BigInteger n, int rounds, Random rnd) {
        BigInteger two = BigInteger.TWO, nm1 = n.subtract(BigInteger.ONE);
        if (n.compareTo(two) < 0) return false;
        for (int p : SMALL) {
            BigInteger bp = BigInteger.valueOf(p);
            if (n.equals(bp)) return true;
            if (n.mod(bp).signum() == 0) return false;
        }
        BigInteger d = nm1;
        int r = 0;
        while (!d.testBit(0)) { d = d.shiftRight(1); r++; }
        for (int i = 0; i < rounds; i++) {
            BigInteger a = new BigInteger(n.bitLength(), rnd).mod(n.subtract(BigInteger.valueOf(3))).add(two);
            BigInteger x = a.modPow(d, n);
            if (x.equals(BigInteger.ONE) || x.equals(nm1)) continue;
            boolean composite = true;
            for (int j = 0; j < r - 1; j++) {
                x = x.multiply(x).mod(n);
                if (x.equals(nm1)) { composite = false; break; }
            }
            if (composite) return false;
        }
        return true;
    }

    public static boolean millerRabin(long n) { return millerRabin(BigInteger.valueOf(n), 20, new Random()); }

    public record Scan(long[] exclusive, long work, int span) {}

    /** Blelloch work-efficient exclusive prefix sum (simulated level by level): work O(n), span O(log n). */
    public static Scan blelloch(long[] a) {
        int n = a.length, size = 1;
        while (size < Math.max(n, 1)) size <<= 1;
        long[] t = Arrays.copyOf(a, size);
        long work = 0;
        int span = 0;
        for (int d = 1; d < size; d <<= 1) {                 // up-sweep
            for (int i = 2 * d - 1; i < size; i += 2 * d) { t[i] = t[i - d] + t[i]; work++; }
            span++;
        }
        t[size - 1] = 0;
        for (int d = size / 2; d >= 1; d >>= 1) {            // down-sweep
            for (int i = 2 * d - 1; i < size; i += 2 * d) { long tmp = t[i - d]; t[i - d] = t[i]; t[i] = tmp + t[i]; work++; }
            span++;
        }
        return new Scan(Arrays.copyOf(t, n), work, span);
    }

    /** Chunked reduction on a real thread pool; combine step is sequential over `workers` partial results. */
    public static long parallelReduce(long[] a, int workers, java.util.function.LongBinaryOperator op, long identity) throws Exception {
        if (a.length == 0) return identity;
        ExecutorService ex = Executors.newFixedThreadPool(workers);
        try {
            int chunk = (a.length + workers - 1) / workers;
            List<Future<Long>> fs = new ArrayList<>();
            for (int lo = 0; lo < a.length; lo += chunk) {
                final int from = lo, to = Math.min(a.length, lo + chunk);
                fs.add(ex.submit(() -> { long r = identity; for (int i = from; i < to; i++) r = op.applyAsLong(r, a[i]); return r; }));
            }
            long r = identity;
            for (Future<Long> f : fs) r = op.applyAsLong(r, f.get());
            return r;
        } finally { ex.shutdown(); }
    }

    /** Fork/Join divide-and-conquer sum: work n-1, depth ~log n. */
    public static long forkJoinSum(long[] a) {
        return ForkJoinPool.commonPool().invoke(new SumTask(a, 0, a.length));
    }

    private static final class SumTask extends RecursiveTask<Long> {
        final long[] a; final int lo, hi;
        SumTask(long[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }
        @Override protected Long compute() {
            if (hi - lo <= 4096) { long s = 0; for (int i = lo; i < hi; i++) s += a[i]; return s; }
            int mid = (lo + hi) >>> 1;
            SumTask l = new SumTask(a, lo, mid), r = new SumTask(a, mid, hi);
            l.fork();
            return r.compute() + l.join();
        }
    }

    /** Pairwise tree reduction: returns {value, depth} with depth = ceil(log2 n). */
    public static long[] treeReduce(long[] a) {
        long[] cur = a.clone();
        int depth = 0;
        while (cur.length > 1) {
            long[] nx = new long[(cur.length + 1) / 2];
            for (int i = 0; i < nx.length; i++) nx[i] = 2 * i + 1 < cur.length ? cur[2 * i] + cur[2 * i + 1] : cur[2 * i];
            cur = nx;
            depth++;
        }
        return new long[]{cur[0], depth};
    }

    /** Brent's theorem: returns {upper bound W/p + D, lower bound max(W/p, D)}. */
    public static double[] brent(double work, double depth, int p) {
        return new double[]{work / p + depth, Math.max(work / p, depth)};
    }
}
