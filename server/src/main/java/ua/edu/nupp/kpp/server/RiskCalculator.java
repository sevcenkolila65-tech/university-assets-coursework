package ua.edu.nupp.kpp.server;
public final class RiskCalculator {
    private RiskCalculator() {}
    public static double calculate(double f1, double f2, double f3) {
        check(f1); check(f2); check(f3);
        return Math.round((0.40*f1 + 0.35*f2 + 0.25*f3)*100.0)/100.0;
    }
    public static double scarcity(int total, int available) {
        if (total < 0 || available < 0 || available > total) throw new IllegalArgumentException("Некоректна кількість одиниць");
        if (total == 0) return 100.0;
        return Math.round((100.0*(total-available)/total)*100.0)/100.0;
    }
    private static void check(double x) {
        if (!Double.isFinite(x) || x < 0 || x > 100) throw new IllegalArgumentException("Фактор має бути в межах 0–100");
    }
}
