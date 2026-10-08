package airportflow.m2;

import java.util.*;

/** Module 2 (CO2): suffix array (doubling + SA-IS), LCP (Kasai), suffix automaton. */
public final class SuffixStructures {
    private SuffixStructures() {}

    /** Prefix doubling, O(n log^2 n); used as baseline / test oracle. */
    public static int[] saDoubling(String s) {
        int n = s.length();
        if (n == 0) return new int[0];
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) { sa[i] = i; rank[i] = s.charAt(i); }
        for (int k = 1; ; k <<= 1) {
            final int kk = k;
            final int[] rk = rank;
            Comparator<Integer> cmp = (a, b) -> {
                if (rk[a] != rk[b]) return Integer.compare(rk[a], rk[b]);
                int ra = a + kk < n ? rk[a + kk] : -1, rb = b + kk < n ? rk[b + kk] : -1;
                return Integer.compare(ra, rb);
            };
            Arrays.sort(sa, cmp);
            int[] tmp = new int[n];
            for (int i = 1; i < n; i++) tmp[sa[i]] = tmp[sa[i - 1]] + (cmp.compare(sa[i - 1], sa[i]) < 0 ? 1 : 0);
            rank = tmp;
            if (rank[sa[n - 1]] == n - 1) break;
        }
        int[] r = new int[n];
        for (int i = 0; i < n; i++) r[i] = sa[i];
        return r;
    }

    /** Linear-time suffix array (SA-IS). */
    public static int[] saIs(String text) {
        if (text.isEmpty()) return new int[0];
        TreeSet<Character> set = new TreeSet<>();
        for (char c : text.toCharArray()) set.add(c);
        Map<Character, Integer> id = new HashMap<>();
        for (char c : set) id.put(c, id.size());
        int[] s = new int[text.length()];
        for (int i = 0; i < s.length; i++) s[i] = id.get(text.charAt(i));
        return saIs(s, set.size() - 1);
    }

    private static void induce(int[] lms, int[] s, boolean[] ls, int[] sumS, int[] sumL, int[] sa) {
        int n = s.length;
        Arrays.fill(sa, -1);
        int[] buf = sumS.clone();
        for (int d : lms) { if (d == n) continue; sa[buf[s[d]]++] = d; }
        buf = sumL.clone();
        sa[buf[s[n - 1]]++] = n - 1;
        for (int i = 0; i < n; i++) {
            int v = sa[i];
            if (v >= 1 && !ls[v - 1]) sa[buf[s[v - 1]]++] = v - 1;
        }
        buf = sumL.clone();
        for (int i = n - 1; i >= 0; i--) {
            int v = sa[i];
            if (v >= 1 && ls[v - 1]) sa[--buf[s[v - 1] + 1]] = v - 1;
        }
    }

    private static int[] saIs(int[] s, int upper) {
        int n = s.length;
        if (n == 0) return new int[0];
        if (n == 1) return new int[]{0};
        if (n == 2) return s[0] < s[1] ? new int[]{0, 1} : new int[]{1, 0};
        int[] sa = new int[n];
        boolean[] ls = new boolean[n];
        for (int i = n - 2; i >= 0; i--) ls[i] = (s[i] == s[i + 1]) ? ls[i + 1] : (s[i] < s[i + 1]);
        int[] sumL = new int[upper + 1], sumS = new int[upper + 1];
        for (int i = 0; i < n; i++) { if (!ls[i]) sumS[s[i]]++; else sumL[s[i] + 1]++; }
        for (int i = 0; i <= upper; i++) { sumS[i] += sumL[i]; if (i < upper) sumL[i + 1] += sumS[i]; }
        int[] lmsMap = new int[n + 1];
        Arrays.fill(lmsMap, -1);
        int m = 0;
        for (int i = 1; i < n; i++) if (!ls[i - 1] && ls[i]) lmsMap[i] = m++;
        int[] lms = new int[m];
        for (int i = 1, k = 0; i < n; i++) if (!ls[i - 1] && ls[i]) lms[k++] = i;
        induce(lms, s, ls, sumS, sumL, sa);
        if (m > 0) {
            int[] sorted = new int[m];
            for (int i = 0, k = 0; i < n; i++) { int v = sa[i]; if (v >= 0 && lmsMap[v] != -1) sorted[k++] = v; }
            int[] recS = new int[m];
            int recUpper = 0;
            for (int i = 1; i < m; i++) {
                int l = sorted[i - 1], r = sorted[i];
                int endL = (lmsMap[l] + 1 < m) ? lms[lmsMap[l] + 1] : n;
                int endR = (lmsMap[r] + 1 < m) ? lms[lmsMap[r] + 1] : n;
                boolean same = true;
                if (endL - l != endR - r) same = false;
                else {
                    while (l < endL) { if (s[l] != s[r]) break; l++; r++; }
                    if (l == n || s[l] != s[r]) same = false;
                }
                if (!same) recUpper++;
                recS[lmsMap[sorted[i]]] = recUpper;
            }
            int[] recSa = saIs(recS, recUpper);
            for (int i = 0; i < m; i++) sorted[i] = lms[recSa[i]];
            induce(sorted, s, ls, sumS, sumL, sa);
        }
        return sa;
    }

    /** lcp[i] = LCP(suffix sa[i], suffix sa[i+1]); O(n). */
    public static int[] kasai(String s, int[] sa) {
        int n = s.length();
        int[] rank = new int[n], lcp = new int[Math.max(0, n - 1)];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;
        for (int i = 0, h = 0; i < n; i++) {
            if (rank[i] + 1 < n) {
                int j = sa[rank[i] + 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h)) h++;
                lcp[rank[i]] = h;
                if (h > 0) h--;
            } else h = 0;
        }
        return lcp;
    }

    public static String longestRepeated(String s) {
        int[] sa = saIs(s), lcp = kasai(s, sa);
        int best = -1;
        for (int i = 0; i < lcp.length; i++) if (best < 0 || lcp[i] > lcp[best]) best = i;
        return best < 0 || lcp[best] == 0 ? "" : s.substring(sa[best], sa[best] + lcp[best]);
    }

    private static int cmp(String s, int from, String pat) {
        return s.substring(from, Math.min(s.length(), from + pat.length())).compareTo(pat);
    }

    /** Occurrences via binary search on the SA, O(m log n). Sorted positions. */
    public static List<Integer> saSearch(String s, int[] sa, String pat) {
        int lo = 0, hi = sa.length;
        while (lo < hi) { int mid = (lo + hi) >>> 1; if (cmp(s, sa[mid], pat) < 0) lo = mid + 1; else hi = mid; }
        int start = lo;
        hi = sa.length;
        while (lo < hi) { int mid = (lo + hi) >>> 1; if (cmp(s, sa[mid], pat) <= 0) lo = mid + 1; else hi = mid; }
        List<Integer> res = new ArrayList<>();
        for (int i = start; i < lo; i++) res.add(sa[i]);
        Collections.sort(res);
        return res;
    }

    /** Suffix automaton, O(n) construction. */
    public static final class SuffixAutomaton {
        private final List<Map<Character, Integer>> next = new ArrayList<>();
        private final List<Integer> link = new ArrayList<>(), len = new ArrayList<>();
        private int last = 0;

        public SuffixAutomaton(String s) {
            next.add(new HashMap<>()); link.add(-1); len.add(0);
            for (char c : s.toCharArray()) extend(c);
        }

        private void extend(char c) {
            int cur = len.size();
            len.add(len.get(last) + 1); link.add(0); next.add(new HashMap<>());
            int p = last;
            while (p != -1 && !next.get(p).containsKey(c)) { next.get(p).put(c, cur); p = link.get(p); }
            if (p == -1) link.set(cur, 0);
            else {
                int q = next.get(p).get(c);
                if (len.get(p) + 1 == len.get(q)) link.set(cur, q);
                else {
                    int cl = len.size();
                    len.add(len.get(p) + 1); next.add(new HashMap<>(next.get(q))); link.add(link.get(q));
                    while (p != -1 && Objects.equals(next.get(p).get(c), q)) { next.get(p).put(c, cl); p = link.get(p); }
                    link.set(q, cl); link.set(cur, cl);
                }
            }
            last = cur;
        }

        public boolean contains(String t) {
            int u = 0;
            for (char c : t.toCharArray()) {
                Integer v = next.get(u).get(c);
                if (v == null) return false;
                u = v;
            }
            return true;
        }

        public int states() { return len.size(); }

        public long distinctSubstrings() {
            long total = 0;
            for (int i = 1; i < len.size(); i++) total += len.get(i) - len.get(link.get(i));
            return total;
        }
    }
}
