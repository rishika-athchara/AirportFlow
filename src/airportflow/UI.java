package airportflow;

import java.util.Scanner;

/** Console helpers shared by every module menu. */
public final class UI {
    private UI() {}
    public static final Scanner IN = new Scanner(System.in);
    /** When true every prompt takes its default value (used by the "full demo" option). */
    public static boolean demo = false;

    @FunctionalInterface public interface Action { void run() throws Exception; }

    public static String ask(String prompt, String def) {
        if (demo) { System.out.println(prompt + " -> " + def); return def; }
        System.out.print(prompt + (def.isEmpty() ? "" : " [" + def + "]") + ": ");
        if (!IN.hasNextLine()) { System.out.println(); System.exit(0); }
        String s = IN.nextLine().trim();
        return s.isEmpty() ? def : s;
    }

    public static int askInt(String prompt, int def, int lo, int hi) {
        while (true) {
            try {
                int v = Integer.parseInt(ask(prompt, String.valueOf(def)));
                if (v >= lo && v <= hi) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("  invalid number (" + lo + ".." + hi + "), try again");
        }
    }

    public static void header(String s) { System.out.println("\n>>> " + s); }

    public static void menu(String title, String back, String[] labels, Action[] acts) {
        while (true) {
            System.out.println("\n===== " + title + " =====");
            for (int i = 0; i < labels.length; i++) System.out.printf("  %d. %s%n", i + 1, labels[i]);
            System.out.println("  0. " + back);
            String c = ask("Choice", "0");
            if (c.equals("0")) return;
            int k = -1;
            try { k = Integer.parseInt(c); } catch (NumberFormatException ignored) { }
            if (k >= 1 && k <= labels.length) {
                try { acts[k - 1].run(); } catch (Exception e) { System.out.println("  ! error: " + e); }
                continue;
            }
            System.out.println("  invalid choice");
        }
    }
}
