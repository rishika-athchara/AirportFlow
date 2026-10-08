package airportflow.m3;

import airportflow.Data;
import airportflow.UI;
import java.util.*;

/** Menu for Module 3 - Advanced Dynamic Programming (CO3). */
public final class M3Menu {
    private M3Menu() {}

    public static void run() {
        UI.menu("M3 ADVANCED DP (CO3)", "Back",
                new String[]{"Levenshtein / Damerau: correct entered data", "Bitmask DP: gate/resource combinations",
                        "Matrix-Chain: order data-processing steps", "Optimal BST: frequently accessed flights"},
                new UI.Action[]{M3Menu::spell, M3Menu::bitmask, M3Menu::matrixChain, M3Menu::obst});
    }

    public static void spell() {
        List<String> vocab = List.of("DELAYED", "CANCELLED", "BOARDING", "GATE CHANGE", "LANDED", "ON TIME", "DEL", "BOM", "BLR", "HYD", "VGA", "DXB");
        String w = UI.ask("Type a (mis-spelled) status or airport code", "DELYAED").toUpperCase();
        System.out.println("  Levenshtein to first 3 words:");
        for (int i = 0; i < 3; i++) System.out.printf("   %-10s %d%n", vocab.get(i), AdvancedDP.levenshtein(w, vocab.get(i)));
        System.out.println("  Damerau-Levenshtein (a swap is 1 edit) best suggestions:");
        for (AdvancedDP.Suggestion s : AdvancedDP.suggest(w, vocab, 3)) System.out.printf("   %-10s %d%n", s.word(), s.distance());
    }

    public static void bitmask() {
        int n = UI.askInt("Number of flights/gates to assign (2-10)", 6, 2, 10);
        List<Data.Flight> fl = Data.FLIGHTS.subList(0, n);
        char[] sizes = new char[n];
        String[] names = new String[n];
        for (int j = 0; j < n; j++) {
            if (j < Data.GATES.size()) { sizes[j] = Data.GATES.get(j).size(); names[j] = Data.GATES.get(j).name(); }
            else { sizes[j] = 'L'; names[j] = "D" + j; }
        }
        int[][] cost = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                cost[i][j] = Math.abs(i - j) * 3 + fl.get(i).pax() / 40 + ("SML".indexOf(sizes[j]) < "SML".indexOf(fl.get(i).size()) ? 1000 : 0);
        AdvancedDP.Assignment a = AdvancedDP.bitmaskAssign(cost);
        System.out.println("  Minimum total cost = " + a.cost() + "  (DP over gate subsets, O(2^n * n))");
        System.out.println("  cost = walking distance + passenger load; +1000 penalty when the gate is too small");
        int bad = 0;
        for (int i = 0; i < n; i++) {
            int g = a.gateOfFlight()[i];
            if (cost[i][g] >= 1000) bad++;
            System.out.printf("   %-7s (%c) -> %-3s (%c)  cost %d%n", fl.get(i).code(), fl.get(i).size(), names[g], sizes[g], cost[i][g]);
        }
        System.out.println(bad == 0 ? "  All size constraints satisfied." : "  Size violations forced by this gate subset: " + bad);
    }

    public static void matrixChain() {
        String[] t = UI.ask("Matrix dimensions p0,p1,...,pn", "10,30,5,60,20").split(",");
        int[] dims = new int[t.length];
        for (int i = 0; i < t.length; i++) dims[i] = Integer.parseInt(t[i].trim());
        AdvancedDP.Chain c = AdvancedDP.matrixChain(dims);
        long left = 0;
        for (int i = 1; i < dims.length - 1; i++) left += (long) dims[0] * dims[i] * dims[i + 1];
        System.out.println("  Best evaluation order: " + c.order() + "\n  Scalar multiplications: " + c.cost()
                + "\n  (naive left-to-right would cost " + left + ")");
    }

    public static void obst() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 8; i++) codes.add(Data.FLIGHTS.get(i).code());
        Collections.sort(codes);
        long[] f = new long[8];
        System.out.println("  flight   access-frequency");
        for (int i = 0; i < 8; i++) { f[i] = new Random(i).nextInt(30) + 1; System.out.printf("  %-8s %d%n", codes.get(i), f[i]); }
        AdvancedDP.Obst o = AdvancedDP.optimalBst(f);
        System.out.println("  Optimal expected cost (weighted): " + o.cost());
        for (AdvancedDP.Node nd : AdvancedDP.bstShape(o.root(), 0, 7, 0))
            System.out.println("   " + "    ".repeat(nd.depth()) + (nd.depth() == 0 ? "root " : "|- ") + codes.get(nd.keyIndex()));
    }
}
