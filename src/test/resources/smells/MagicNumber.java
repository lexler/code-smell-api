public class MagicNumber {

    public String shippingTier(double orderTotal) {
        if (orderTotal > 500) {
            return "free";
        }
        return "standard";
    }
}
