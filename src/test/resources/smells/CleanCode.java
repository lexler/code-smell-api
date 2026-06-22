public class CleanCode {

    private static final double NIGHTLY_RATE = 99.95;
    private static final double LOYALTY_THRESHOLD = 500.0;
    private static final double LOYALTY_DISCOUNT = 0.9;

    public String confirm(Guest guest, int nights) {
        double price = price(nights);
        return summary(guest, price);
    }

    private double price(int nights) {
        double total = nights * NIGHTLY_RATE;
        return qualifiesForLoyalty(total) ? total * LOYALTY_DISCOUNT : total;
    }

    private boolean qualifiesForLoyalty(double total) {
        return total > LOYALTY_THRESHOLD;
    }

    private String summary(Guest guest, double price) {
        return guest.name() + " pays " + price;
    }

    record Guest(String name, String email) {}
}
