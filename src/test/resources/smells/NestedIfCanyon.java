public class NestedIfCanyon {

    public String decideLoan(int creditScore, boolean employed, boolean hasCollateral, double income) {
        if (employed) {
            if (creditScore > 650) {
                if (hasCollateral) {
                    if (income > 30000) {
                        return "approved";
                    }
                }
            }
        }
        return "denied";
    }
}
