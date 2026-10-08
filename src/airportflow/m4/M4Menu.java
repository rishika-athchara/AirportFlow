package airportflow.m4;

import airportflow.Data;
import airportflow.UI;
import java.util.*;
import java.util.function.ToIntBiFunction;

/** Menu for Module 4 - Network Flow (CO4). */
public final class M4Menu {
    private M4Menu() {}

    public static void run() {
        UI.menu("M4 NETWORK FLOW (CO4)", "Back",
                new String[]{"Max passenger flow (Ford-Fulkerson / Edmonds-Karp / Dinic)", "Min-Cut: critical connections",
                        "Bipartite matching: flights -> gates", "Konig's theorem: gate-flight conflicts"},
                new UI.Action[]{M4Menu::flow, M4Menu::cut, M4Menu::matching, M4Menu::konig});
    }

    private static NetworkFlow.FlowNetwork airport() {
        NetworkFlow.FlowNetwork g = new NetworkFlow.FlowNetwork();
        for (String[] e : Data.NETWORK) g.addEdge(e[0], e[1], Integer.parseInt(e[2]));
        return g;
    }

    private static Map<String, List<String>> adjacency() {
        Map<String, List<String>> adj = new LinkedHashMap<>();
        Map<Integer, List<String>> el = Data.eligibility(Data.FLIGHTS.subList(0, 10), Data.GATES);
        for (Map.Entry<Integer, List<String>> e : el.entrySet()) adj.put(Data.FLIGHTS.get(e.getKey()).code() + "#" + e.getKey(), e.getValue());
        return adj;
    }

    public static void flow() {
        NetworkFlow.FlowNetwork g = airport();
        System.out.println("  Passenger-flow network (persons / 10 min):");
        for (String[] e : Data.NETWORK) System.out.printf("   %14s -> %-14s cap %s%n", e[0], e[1], e[2]);
        long t0 = System.nanoTime();
        int ff = g.fordFulkerson("Entrance", "Aircraft"); long t1 = System.nanoTime();
        int ek = g.edmondsKarp("Entrance", "Aircraft"); long t2 = System.nanoTime();
        int di = g.dinic("Entrance", "Aircraft"); long t3 = System.nanoTime();
        System.out.printf("  Ford-Fulkerson  max flow = %d  (%.3f ms)%n", ff, (t1 - t0) / 1e6);
        System.out.printf("  Edmonds-Karp    max flow = %d  (%.3f ms)%n", ek, (t2 - t1) / 1e6);
        System.out.printf("  Dinic           max flow = %d  (%.3f ms)%n", di, (t3 - t2) / 1e6);
    }

    public static void cut() {
        NetworkFlow.FlowNetwork g = airport();
        int f = g.edmondsKarp("Entrance", "Aircraft");
        List<NetworkFlow.Cut> cut = g.minCut("Entrance");
        int total = cut.stream().mapToInt(NetworkFlow.Cut::capacity).sum();
        System.out.println("  Max flow = " + f + ". Critical connections (min-cut, total capacity " + total + "):");
        for (NetworkFlow.Cut c : cut) System.out.printf("   %s -> %s (cap %d)  <- upgrading this raises throughput%n", c.from(), c.to(), c.capacity());
    }

    public static void matching() {
        Map<String, List<String>> adj = adjacency();
        List<String> left = new ArrayList<>(adj.keySet());
        Map<String, String> m = NetworkFlow.bipartiteMatching(left, adj);
        System.out.println("  Flights: " + left.size() + ", gates: " + Data.GATES.size() + ". Max simultaneous assignments: " + m.size());
        new TreeMap<>(m).forEach((gate, fl) -> System.out.println("   " + gate + " <- " + fl));
        List<String> un = new ArrayList<>(left);
        un.removeAll(m.values());
        System.out.println("  Unassigned flights: " + un);
    }

    public static void konig() {
        Map<String, List<String>> adj = adjacency();
        List<String> left = new ArrayList<>(adj.keySet());
        Map<String, String> m = NetworkFlow.bipartiteMatching(left, adj);
        NetworkFlow.Cover c = NetworkFlow.konigCover(left, adj, m);
        System.out.println("  |max matching| = " + m.size() + "   |min vertex cover| = " + c.size() + "   (equal by Konig's theorem)");
        System.out.println("   cover flights: " + c.left() + "\n   cover gates  : " + c.right());
        System.out.println("  Every flight-gate compatibility is touched by this small set: the cover gates are the scarce resource.");
    }
}
