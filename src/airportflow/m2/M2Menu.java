package airportflow.m2;

import airportflow.Data;
import airportflow.UI;
import java.util.*;

/** Menu for Module 2 - Suffix Structures / Document Similarity (CO2). */
public final class M2Menu {
    private M2Menu() {}

    public static void run() {
        UI.menu("M2 SUFFIX STRUCTURES (CO2)", "Back",
                new String[]{"Suffix Array (SA-IS) document index + substring query", "LCP (Kasai): repeated descriptions",
                        "Suffix Automaton: substring queries"},
                new UI.Action[]{M2Menu::index, M2Menu::lcp, M2Menu::automaton});
    }

    private static String doc() { return String.join("#", Data.LOG); }

    public static void index() {
        String doc = doc();
        int[] sa = SuffixStructures.saIs(doc);
        boolean same = Arrays.equals(sa, SuffixStructures.saDoubling(doc));
        System.out.println("  Indexed " + doc.length() + " characters with SA-IS (equals doubling SA: " + same + ")");
        String q = UI.ask("Substring query", "gate").toLowerCase();
        String low = doc.toLowerCase();
        List<Integer> pos = SuffixStructures.saSearch(low, SuffixStructures.saIs(low), q);
        System.out.println("  '" + q + "' occurs " + pos.size() + " time(s); first offsets: " + pos.subList(0, Math.min(10, pos.size())));
    }

    public static void lcp() {
        String doc = doc();
        int[] sa = SuffixStructures.saIs(doc), lcp = SuffixStructures.kasai(doc, sa);
        System.out.println("  Longest repeated operational text: '" + SuffixStructures.longestRepeated(doc) + "'");
        System.out.println("  Max LCP (Kasai): " + Arrays.stream(lcp).max().orElse(0)
                + " | average LCP: " + String.format("%.2f", Arrays.stream(lcp).average().orElse(0)));
    }

    public static void automaton() {
        String doc = doc();
        SuffixStructures.SuffixAutomaton sam = new SuffixStructures.SuffixAutomaton(doc);
        System.out.println("  States: " + sam.states() + " for " + doc.length() + " characters");
        System.out.println("  Distinct substrings: " + sam.distinctSubstrings());
        String q = UI.ask("Does the log contain this substring?", "runway 09");
        System.out.println("  -> " + (sam.contains(q) ? "YES" : "NO"));
    }
}
