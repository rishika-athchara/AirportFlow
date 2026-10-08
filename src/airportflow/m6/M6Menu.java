package airportflow.m6;

import airportflow.Data;
import airportflow.UI;
import java.math.BigInteger;
import java.util.*;

/** Menu for Module 6 - Randomized & Parallel Algorithms (CO6). */
public final class M6Menu {
    private M6Menu() {}

    public static void run() {
        UI.menu("M6 RANDOMIZED & PARALLEL (CO6)", "Back",
                new String[]{"Randomized QuickSort: rank flights", "Reservoir sampling: event stream",
                        "Miller-Rabin: large-number primality", "Blelloch scan: cumulative passenger counts",
                        "Parallel reduce: transport statistics", "Brent's theorem: processor/time bounds"},
                new UI.Action[]{M6Menu::quicksort, M6Menu::reservoir, M6Menu::millerRabin, M6Menu::scan, M6Menu::reduce, M6Menu::brent});
    }

    public static void quicksort() {
        int[] pax = Data.FLIGHTS.stream().mapToInt(Data.Flight::pax).toArray();
        long c = RandPar.quicksort(pax, true, new Random(1));
        StringBuilder sb = new StringBuilder();
        for (int i = pax.length - 1; i >= pax.length - 8; i--) sb.append(pax[i]).append(' ');
        System.out.println("  Busiest flights by passengers: " + sb + "(comparisons = " + c + ")");
        int n = UI.askInt("Adversarial (already sorted) input size", 1000, 10, 20000);
        int[] a = new int[n], b = new int[n];
        for (int i = 0; i < n; i++) a[i] = b[i] = i;
        long det = RandPar.quicksort(a, false, null), rnd = RandPar.quicksort(b, true, new Random(2));
        System.out.println("  n = " + n + ": first-pivot comparisons = " + det + ", randomized = " + rnd + "   (O(n^2) vs expected O(n log n))");
    }

    public static void reservoir() {
        int k = UI.askInt("Sample size k", 5, 1, 50), N = UI.askInt("Stream length", 100000, k, 5_000_000);
        Iterator<String> stream = new Iterator<>() {
            int i = 0;
            public boolean hasNext() { return i < N; }
            public String next() { return String.format("EVT%07d", i++); }
        };
        System.out.println("  " + k + " uniform samples from a stream of " + N + " events (O(k) memory): " + RandPar.reservoir(stream, k, new Random()));
    }

    public static void millerRabin() {
        BigInteger n = new BigInteger(UI.ask("Number to test (booking id / key)", "2305843009213693951"));
        System.out.println("  Miller-Rabin (20 rounds): " + n + " is " + (RandPar.millerRabin(n, 20, new Random()) ? "PROBABLY PRIME" : "COMPOSITE"));
        System.out.println("  Carmichael 561 -> " + RandPar.millerRabin(561) + " | 1000000007 -> " + RandPar.millerRabin(1000000007L));
    }

    public static void scan() {
        int n = UI.askInt("Number of terminals / hours", 8, 1, 4096);
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = new Random(i).nextInt(90) + 10;
        RandPar.Scan s = RandPar.blelloch(a);
        int show = Math.min(n, 16);
        System.out.println("  arrivals      : " + Arrays.toString(Arrays.copyOf(a, show)));
        System.out.println("  exclusive scan: " + Arrays.toString(Arrays.copyOf(s.exclusive(), show)));
        System.out.println("  work = " + s.work() + " (O(n)), span = " + s.span() + " (O(log n)); sequential scan work = " + (n - 1));
    }

    public static void reduce() throws Exception {
        long[] pax = new long[1_000_000];
        for (int i = 0; i < pax.length; i++) pax[i] = Data.FLIGHTS.get(i % Data.FLIGHTS.size()).pax();
        long t0 = System.nanoTime(); long seq = Arrays.stream(pax).sum();
        long t1 = System.nanoTime(); long par = RandPar.parallelReduce(pax, 4, Long::sum, 0);
        long t2 = System.nanoTime(); long fj = RandPar.forkJoinSum(pax);
        long t3 = System.nanoTime();
        System.out.printf("  sequential sum     : %d  (%.2f ms)%n", seq, (t1 - t0) / 1e6);
        System.out.printf("  thread-pool (4)    : %d  (%.2f ms)%n", par, (t2 - t1) / 1e6);
        System.out.printf("  fork/join          : %d  (%.2f ms)%n", fj, (t3 - t2) / 1e6);
        long[] tr = RandPar.treeReduce(pax);
        System.out.println("  tree reduction     : value " + tr[0] + ", depth " + tr[1] + " = ceil(log2 n), work " + (pax.length - 1));
        System.out.println("  parallel max       : " + RandPar.parallelReduce(pax, 4, Math::max, Long.MIN_VALUE));
    }

    public static void brent() {
        int W = UI.askInt("Work W (total operations)", 1_000_000, 1, Integer.MAX_VALUE), D = UI.askInt("Depth D (critical path)", 20, 1, 1_000_000);
        System.out.printf("  %5s %20s %18s %12s%n", "p", "T_p <= W/p + D", "lower max(W/p,D)", "speed-up <=");
        for (int p : new int[]{1, 2, 4, 8, 16, 64, 1024}) {
            double[] b = RandPar.brent(W, D, p);
            System.out.printf("  %5d %20.1f %18.1f %12.1f%n", p, b[0], b[1], W / b[1]);
        }
    }
}
