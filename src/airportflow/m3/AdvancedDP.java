package airportflow.m3;

import java.util.*;

/** Module 3 (CO3): Levenshtein/Damerau, bitmask DP, matrix-chain, optimal BST. */
public final class AdvancedDP {
    private AdvancedDP() {}

    public static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int[] cur = new int[b.length() + 1];
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++)
                cur[j] = Math.min(Math.min(prev[j] + 1, cur[j - 1] + 1), prev[j - 1] + (a.charAt(i - 1) != b.charAt(j - 1) ? 1 : 0));
            prev = cur;
        }
        return prev[b.length()];
    }

    /** True Damerau-Levenshtein (adjacent transpositions cost 1), O(nm). */
    public static int damerau(String a, String b) {
        int n = a.length(), m = b.length(), INF = n + m;
        int[][] d = new int[n + 2][m + 2];
        Map<Character, Integer> da = new HashMap<>();
        d[0][0] = INF;
        for (int i = 0; i <= n; i++) { d[i + 1][0] = INF; d[i + 1][1] = i; }
        for (int j = 0; j <= m; j++) { d[0][j + 1] = INF; d[1][j + 1] = j; }
        for (int i = 1; i <= n; i++) {
            int db = 0;
            for (int j = 1; j <= m; j++) {
                int i1 = da.getOrDefault(b.charAt(j - 1), 0), j1 = db;
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                if (cost == 0) db = j;
                d[i + 1][j + 1] = Math.min(Math.min(d[i][j] + cost, d[i + 1][j] + 1),
                        Math.min(d[i][j + 1] + 1, d[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1)));
            }
            da.put(a.charAt(i - 1), i);
        }
        return d[n + 1][m + 1];
    }

    public record Suggestion(String word, int distance) {}

    public static List<Suggestion> suggest(String word, List<String> dict, int k) {
        List<Suggestion> l = new ArrayList<>();
        for (String w : dict) l.add(new Suggestion(w, damerau(word.toUpperCase(), w.toUpperCase())));
        l.sort(Comparator.comparingInt(Suggestion::distance).thenComparing(Suggestion::word));
        return l.subList(0, Math.min(k, l.size()));
    }

    public record Assignment(long cost, int[] gateOfFlight) {}

    /** Min-cost assignment of n flights to n gates via DP over gate subsets, O(2^n * n). */
    public static Assignment bitmaskAssign(int[][] cost) {
        int n = cost.length;
        long INF = Long.MAX_VALUE / 4;
        long[] dp = new long[1 << n];
        int[] parMask = new int[1 << n], parGate = new int[1 << n];
        Arrays.fill(dp, INF);
        dp[0] = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            if (dp[mask] >= INF) continue;
            int i = Integer.bitCount(mask);
            if (i >= n) continue;
            for (int g = 0; g < n; g++) {
                if ((mask >> g & 1) != 0) continue;
                int nm = mask | 1 << g;
                if (dp[mask] + cost[i][g] < dp[nm]) { dp[nm] = dp[mask] + cost[i][g]; parMask[nm] = mask; parGate[nm] = g; }
            }
        }
        int full = (1 << n) - 1;
        int[] res = new int[n];
        for (int m = full; m != 0; m = parMask[m]) res[Integer.bitCount(parMask[m])] = parGate[m];
        return new Assignment(dp[full], res);
    }

    public record Chain(long cost, String order) {}

    /** dims has n+1 entries for n matrices; O(n^3). */
    public static Chain matrixChain(int[] dims) {
        int n = dims.length - 1;
        long[][] m = new long[n][n];
        int[][] s = new int[n][n];
        for (int L = 2; L <= n; L++)
            for (int i = 0; i + L - 1 < n; i++) {
                int j = i + L - 1;
                m[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long c = m[i][k] + m[k + 1][j] + (long) dims[i] * dims[k + 1] * dims[j + 1];
                    if (c < m[i][j]) { m[i][j] = c; s[i][j] = k; }
                }
            }
        return new Chain(m[0][n - 1], paren(s, 0, n - 1));
    }

    private static String paren(int[][] s, int i, int j) {
        return i == j ? "M" + (i + 1) : "(" + paren(s, i, s[i][j]) + " x " + paren(s, s[i][j] + 1, j) + ")";
    }

    public record Obst(long cost, int[][] root) {}

    /** Keys sorted; cost = sum freq * depth (root depth 1); O(n^3). */
    public static Obst optimalBst(long[] freq) {
        int n = freq.length;
        long[][] cost = new long[n + 2][n + 2];
        int[][] root = new int[n][n];
        long[] pre = new long[n + 1];
        for (int i = 0; i < n; i++) pre[i + 1] = pre[i] + freq[i];
        for (int i = 0; i < n; i++) { cost[i][i] = freq[i]; root[i][i] = i; }
        for (int L = 2; L <= n; L++)
            for (int i = 0; i + L - 1 < n; i++) {
                int j = i + L - 1;
                cost[i][j] = Long.MAX_VALUE;
                for (int r = i; r <= j; r++) {
                    long c = (r > i ? cost[i][r - 1] : 0) + (r < j ? cost[r + 1][j] : 0) + pre[j + 1] - pre[i];
                    if (c < cost[i][j]) { cost[i][j] = c; root[i][j] = r; }
                }
            }
        return new Obst(cost[0][n - 1], root);
    }

    public record Node(int depth, int keyIndex) {}

    public static List<Node> bstShape(int[][] root, int i, int j, int depth) {
        List<Node> res = new ArrayList<>();
        if (i > j) return res;
        int r = root[i][j];
        res.add(new Node(depth, r));
        res.addAll(bstShape(root, i, r - 1, depth + 1));
        res.addAll(bstShape(root, r + 1, j, depth + 1));
        return res;
    }
}
