public class DeadCode {

    public double orderTotal(double price, int quantity) {
        double discount = price * 0.1;
        return price * quantity;
    }
}
