package airportflow.m1;

import java.util.*;

/** Module 1 (CO1): KMP, Z-function, Rabin-Karp. */
public final class StringAlgos {
    private StringAlgos() {}

    public static int[] prefixFunction(String p) {
        int[] pi = new int[p.length()];
        for (int i = 1, k = 0; i < p.length(); i++) {
            while (k > 0 && p.charAt(i) != p.charAt(k)) k = pi[k - 1];
            if (p.charAt(i) == p.charAt(k)) k++;
            pi[i] = k;
        }
        return pi;
    }

    /** All start indices of pat in text, O(n+m). */
    public static List<Integer> kmp(String text, String pat) {
        List<Integer> res = new ArrayList<>();
        if (pat.isEmpty()) return res;
        int[] pi = prefixFunction(pat);
        for (int i = 0, k = 0; i < text.length(); i++) {
            while (k > 0 && text.charAt(i) != pat.charAt(k)) k = pi[k - 1];
            if (text.charAt(i) == pat.charAt(k)) k++;
            if (k == pat.length()) { res.add(i - k + 1); k = pi[k - 1]; }
        }
        return res;
    }

    public static int[] zFunction(String s) {
        int n = s.length();
        int[] z = new int[n];
        for (int i = 1, l = 0, r = 0; i < n; i++) {
            if (i < r) z[i] = Math.min(r - i, z[i - l]);
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
            if (i + z[i] > r) { l = i; r = i + z[i]; }
        }
        if (n > 0) z[0] = n;
        return z;
    }

    public static List<Integer> zSearch(String text, String pat) {
        List<Integer> res = new ArrayList<>();
        if (pat.isEmpty()) return res;
        int[] z = zFunction(pat + '\u0000' + text);
        int m = pat.length();
        for (int i = m + 1; i < z.length; i++) if (z[i] >= m) res.add(i - m - 1);
        return res;
    }

    /** Positions where the message restarts with its own prefix. */
    public static int repeatedPrefixCount(String msg) {
        int c = 0;
        int[] z = zFunction(msg);
        for (int i = 1; i < z.length; i++) if (z[i] > 0) c++;
        return c;
    }

    /** Rolling hash, expected O(n+m); every hash hit is verified. */
    public static List<Integer> rabinKarp(String text, String pat) {
        final long MOD = 1_000_000_007L, B = 256;
        List<Integer> res = new ArrayList<>();
        int n = text.length(), m = pat.length();
        if (m == 0 || m > n) return res;
        long h = 1;
        for (int i = 1; i < m; i++) h = h * B % MOD;
        long hp = 0, ht = 0;
        for (int i = 0; i < m; i++) { hp = (hp * B + pat.charAt(i)) % MOD; ht = (ht * B + text.charAt(i)) % MOD; }
        for (int i = 0; i + m <= n; i++) {
            if (hp == ht && text.startsWith(pat, i)) res.add(i);
            if (i + m < n) ht = ((ht - text.charAt(i) * h % MOD + MOD) % MOD * B + text.charAt(i + m)) % MOD;
        }
        return res;
    }

    // Aho-Corasick removed to simplify Module 1; multi-pattern matching no longer available.
}
