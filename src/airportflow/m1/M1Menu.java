package airportflow.m1;

import airportflow.Data;
import airportflow.UI;
import java.util.*;

/** Menu for Module 1 - String Algorithms (CO1). */
public final class M1Menu {
    private M1Menu() {}

    public static void run() {
        UI.menu("M1 STRING ALGORITHMS (CO1)", "Back",
            new String[]{"KMP: search flight numbers / alerts", "Z-function: repeated operational messages",
                "Rabin-Karp: booking & baggage codes"},
            new UI.Action[]{M1Menu::kmp, M1Menu::z, M1Menu::rabinKarp});
    }

    public static void kmp() {
        System.out.println("Searching the operational log with KMP. Sample lines:");
        for (int i = 0; i < 4; i++) System.out.printf("  [%02d] %s%n", i, Data.LOG.get(i));
        String pat = UI.ask("Pattern", "DELAYED").toUpperCase();
        String text = String.join("\n", Data.LOG).toUpperCase();
        List<Integer> pos = StringAlgos.kmp(text, pat);
        System.out.println("  " + pos.size() + " match(es) at offsets " + pos.subList(0, Math.min(15, pos.size())));
        SortedSet<Integer> lines = new TreeSet<>();
        for (int p : pos) { int c = 0; for (int i = 0; i < p; i++) if (text.charAt(i) == '\n') c++; lines.add(c); }
        int shown = 0;
        for (int l : lines) { if (shown++ == 8) break; System.out.printf("   line %02d: %s%n", l, Data.LOG.get(l)); }
    }

    public static void z() {
        String s = UI.ask("Message for Z-function", "AI101 AI101 AI101 DELAYED");
        System.out.println("  Z-array: " + Arrays.toString(StringAlgos.zFunction(s)));
        System.out.println("  positions restarting with own prefix: " + StringAlgos.repeatedPrefixCount(s));
        String tok = s.split("\\s+")[0];
        System.out.println("  repeated token '" + tok + "' found at: " + StringAlgos.zSearch(s, tok));
    }

    public static void rabinKarp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(String.format("BK%05d-BAG%03d ", new Random(i).nextInt(90000) + 10000, i));
        String blob = sb.toString().trim();
        System.out.println("  booking/baggage codes: " + blob.substring(0, 110) + " ...");
        String pat = UI.ask("Code fragment to find", "BAG007");
        System.out.println("  Rabin-Karp: " + StringAlgos.rabinKarp(blob, pat) + "  | KMP cross-check: " + StringAlgos.kmp(blob, pat));
    }

    // Aho-Corasick removed: multiple-pattern search was deprecated for simplicity.
}
