package airportflow;

import java.util.*;

/** Deterministic synthetic airport data. */
public final class Data {
    private Data() {}
    public record Flight(int id, String code, String dest, int depMin, int pax, char size) {}
    public record Gate(String name, char size) {}

    private static final String[] AIRLINES = {"AI", "6E", "UK", "EK", "QR", "SQ", "BA", "LH"};
    private static final String[] CITIES = {"DEL", "BOM", "BLR", "HYD", "MAA", "CCU", "DXB", "SIN", "LHR", "DOH", "FRA", "VGA"};
    public static final String[] STATUS = {"ON TIME", "DELAYED", "BOARDING", "CANCELLED", "GATE CHANGE", "LANDED"};
    private static final String[] NOTES = {"fuel truck arrived", "baggage belt 3 jammed", "crew change pending",
            "weather advisory runway 09", "catering loaded", "security alert zone B"};

    public static List<Flight> makeFlights(int n, long seed) {
        Random r = new Random(seed);
        List<Flight> l = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String code = AIRLINES[r.nextInt(AIRLINES.length)] + (100 + r.nextInt(900));
            l.add(new Flight(i, code, CITIES[r.nextInt(CITIES.length)], 360 + 15 * i, 60 + r.nextInt(261), "SML".charAt(r.nextInt(3))));
        }
        return l;
    }

    public static List<String> makeLog(int n, long seed) {
        Random r = new Random(seed);
        List<Flight> fl = makeFlights(12, seed);
        List<String> log = new ArrayList<>();
        for (int i = 0; i < n; i++)
            log.add(fl.get(r.nextInt(fl.size())).code() + " " + STATUS[r.nextInt(STATUS.length)] + " - " + NOTES[r.nextInt(NOTES.length)]);
        return log;
    }

    public static final List<Flight> FLIGHTS = makeFlights(20, 7);
    public static final List<String> LOG = makeLog(40, 11);
    public static final List<Gate> GATES = List.of(new Gate("A1", 'S'), new Gate("A2", 'M'), new Gate("B1", 'M'),
            new Gate("B2", 'L'), new Gate("C1", 'L'), new Gate("C2", 'S'));

    private static int rank(char c) { return "SML".indexOf(c); }

    /** flight id -> gates whose size >= flight size. */
    public static Map<Integer, List<String>> eligibility(List<Flight> fl, List<Gate> gates) {
        Map<Integer, List<String>> m = new LinkedHashMap<>();
        for (Flight f : fl) {
            List<String> ok = new ArrayList<>();
            for (Gate g : gates) if (rank(g.size()) >= rank(f.size())) ok.add(g.name());
            m.put(f.id(), ok);
        }
        return m;
    }

    /** Passenger-flow capacities (persons / 10 min): {from, to, capacity}. */
    public static final String[][] NETWORK = {
            {"Entrance", "CheckIn", "300"}, {"Entrance", "SelfKiosk", "120"}, {"CheckIn", "Security", "220"},
            {"SelfKiosk", "Security", "120"}, {"Security", "Immigration", "180"}, {"Security", "DomesticGates", "160"},
            {"Immigration", "IntlGates", "150"}, {"DomesticGates", "Aircraft", "140"}, {"IntlGates", "Aircraft", "130"}};

    public static final int[][] TERMINAL_GRAPH = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {1, 5}, {5, 6}, {2, 6}, {6, 7}};
}
