package airportflow.m4;

import java.util.*;

/** Module 4 (CO4): Ford-Fulkerson, Edmonds-Karp, Dinic, min-cut, bipartite matching, Konig's theorem. */
public final class NetworkFlow {
    private NetworkFlow() {}

    public record Cut(String from, String to, int capacity) {}

    public static final class FlowNetwork {
        private final Map<String, Integer> idx = new HashMap<>();
        private final List<String> names = new ArrayList<>();
        private final List<List<Integer>> g = new ArrayList<>();
        private final List<int[]> edges = new ArrayList<>();   // {to, residual, original}

        public int nodeCount() { return names.size(); }

        private int node(String name) {
            Integer i = idx.get(name);
            if (i != null) return i;
            idx.put(name, names.size()); names.add(name); g.add(new ArrayList<>());
            return names.size() - 1;
        }

        public void addEdge(String from, String to, int cap) {
            int u = node(from), v = node(to);
            g.get(u).add(edges.size()); edges.add(new int[]{v, cap, cap});
            g.get(v).add(edges.size()); edges.add(new int[]{u, 0, 0});
        }

        private void reset() { for (int[] e : edges) e[1] = e[2]; }

        /** DFS augmenting paths, O(E * maxflow). */
        public int fordFulkerson(String S, String T) {
            reset();
            int s = idx.get(S), t = idx.get(T), flow = 0;
            while (true) {
                int f = dfs(s, t, Integer.MAX_VALUE, new boolean[names.size()]);
                if (f == 0) return flow;
                flow += f;
            }
        }

        private int dfs(int u, int t, int f, boolean[] seen) {
            if (u == t) return f;
            seen[u] = true;
            for (int ei : g.get(u)) {
                int[] e = edges.get(ei);
                if (e[1] > 0 && !seen[e[0]]) {
                    int d = dfs(e[0], t, Math.min(f, e[1]), seen);
                    if (d > 0) { e[1] -= d; edges.get(ei ^ 1)[1] += d; return d; }
                }
            }
            return 0;
        }

        /** BFS augmenting paths, O(V E^2). */
        public int edmondsKarp(String S, String T) {
            reset();
            int s = idx.get(S), t = idx.get(T), flow = 0, n = names.size();
            while (true) {
                int[] par = new int[n];
                Arrays.fill(par, -2);
                par[s] = -1;
                Deque<Integer> q = new ArrayDeque<>();
                q.add(s);
                while (!q.isEmpty() && par[t] == -2) {
                    int u = q.poll();
                    for (int ei : g.get(u)) {
                        int[] e = edges.get(ei);
                        if (e[1] > 0 && par[e[0]] == -2) { par[e[0]] = ei; q.add(e[0]); }
                    }
                }
                if (par[t] == -2) return flow;
                int b = Integer.MAX_VALUE;
                for (int v = t; par[v] != -1; v = edges.get(par[v] ^ 1)[0]) b = Math.min(b, edges.get(par[v])[1]);
                for (int v = t; par[v] != -1; v = edges.get(par[v] ^ 1)[0]) { edges.get(par[v])[1] -= b; edges.get(par[v] ^ 1)[1] += b; }
                flow += b;
            }
        }

        /** Level graph + blocking flow, O(V^2 E). */
        public int dinic(String S, String T) {
            reset();
            int s = idx.get(S), t = idx.get(T), flow = 0, n = names.size();
            while (true) {
                int[] level = new int[n];
                Arrays.fill(level, -1);
                level[s] = 0;
                Deque<Integer> q = new ArrayDeque<>();
                q.add(s);
                while (!q.isEmpty()) {
                    int u = q.poll();
                    for (int ei : g.get(u)) {
                        int[] e = edges.get(ei);
                        if (e[1] > 0 && level[e[0]] < 0) { level[e[0]] = level[u] + 1; q.add(e[0]); }
                    }
                }
                if (level[t] < 0) return flow;
                int[] it = new int[n];
                int f;
                while ((f = blocking(s, t, Integer.MAX_VALUE, level, it)) > 0) flow += f;
            }
        }

        private int blocking(int u, int t, int f, int[] level, int[] it) {
            if (u == t) return f;
            for (; it[u] < g.get(u).size(); it[u]++) {
                int ei = g.get(u).get(it[u]);
                int[] e = edges.get(ei);
                if (e[1] > 0 && level[e[0]] == level[u] + 1) {
                    int d = blocking(e[0], t, Math.min(f, e[1]), level, it);
                    if (d > 0) { e[1] -= d; edges.get(ei ^ 1)[1] += d; return d; }
                }
            }
            return 0;
        }

        /** Call after a max-flow run: saturated edges leaving the source side are the critical connections. */
        public List<Cut> minCut(String S) {
            int s = idx.get(S);
            boolean[] seen = new boolean[names.size()];
            Deque<Integer> q = new ArrayDeque<>();
            seen[s] = true; q.add(s);
            while (!q.isEmpty()) {
                int u = q.poll();
                for (int ei : g.get(u)) {
                    int[] e = edges.get(ei);
                    if (e[1] > 0 && !seen[e[0]]) { seen[e[0]] = true; q.add(e[0]); }
                }
            }
            List<Cut> cut = new ArrayList<>();
            for (int u = 0; u < seen.length; u++) {
                if (!seen[u]) continue;
                for (int ei : g.get(u)) {
                    int[] e = edges.get(ei);
                    if (ei % 2 == 0 && !seen[e[0]]) cut.add(new Cut(names.get(u), names.get(e[0]), e[2]));
                }
            }
            return cut;
        }
    }

    /** Kuhn's augmenting-path matching, O(VE). Returns right -> left. */
    public static Map<String, String> bipartiteMatching(List<String> left, Map<String, List<String>> adj) {
        Map<String, String> match = new LinkedHashMap<>();
        for (String u : left) tryKuhn(u, adj, match, new HashSet<>());
        return match;
    }

    private static boolean tryKuhn(String u, Map<String, List<String>> adj, Map<String, String> match, Set<String> seen) {
        for (String v : adj.getOrDefault(u, List.of())) {
            if (!seen.add(v)) continue;
            if (!match.containsKey(v) || tryKuhn(match.get(v), adj, match, seen)) { match.put(v, u); return true; }
        }
        return false;
    }

    public record Cover(Set<String> left, Set<String> right) {
        public int size() { return left.size() + right.size(); }
    }

    /** Konig's theorem: min vertex cover has the same size as the maximum matching. */
    public static Cover konigCover(List<String> left, Map<String, List<String>> adj, Map<String, String> match) {
        Map<String, String> mateL = new HashMap<>();
        for (Map.Entry<String, String> e : match.entrySet()) mateL.put(e.getValue(), e.getKey());
        Set<String> leftSet = new HashSet<>(left), Z = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        for (String u : left) if (!mateL.containsKey(u)) { Z.add(u); stack.push(u); }
        while (!stack.isEmpty()) {
            String u = stack.pop();
            for (String v : adj.getOrDefault(u, List.of())) {
                if (Z.contains(v) || v.equals(mateL.get(u))) continue;
                Z.add(v);
                String w = match.get(v);
                if (w != null && Z.add(w)) stack.push(w);
            }
        }
        Set<String> cl = new TreeSet<>(), cr = new TreeSet<>();
        for (String u : left) if (!Z.contains(u)) cl.add(u);
        for (String v : Z) if (!leftSet.contains(v)) cr.add(v);
        return new Cover(cl, cr);
    }
}
