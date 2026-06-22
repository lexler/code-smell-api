import java.util.ArrayList;
import java.util.List;

public class GlobalMutableState {

    public static int nextInvoiceNumber = 1000;
    public static List<String> issuedInvoices = new ArrayList<>();

    public String issue(String customer) {
        String number = "INV-" + nextInvoiceNumber++;
        issuedInvoices.add(number);
        return number;
    }
}
