public class SwallowedException {

    public void recordPayment(String amount) {
        try {
            double value = Double.parseDouble(amount);
            postToLedger(value);
        } catch (NumberFormatException e) {
        }
    }

    private void postToLedger(double value) {
        if (value <= 0) {
            throw new NumberFormatException();
        }
    }
}
