import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * The CheckoutPage class represents the checkout interface for processing payments.
 * It displays the selected items, total price, and payment method options.
 *
 * @author Sareem Mominkhoja, Rayan Ali, Chloe Lee, Chase Varghese
 */
public class CheckoutPage extends JFrame {
    private ArrayList<String> selectedItems;
    private ArrayList<Double> selectedPrices;
    private JLabel totalLabel;

    /**
     * Constructs a CheckoutPage with the selected items and their prices.
     *
     * @param items  The list of selected item names.
     * @param prices The list of corresponding item prices.
     */
    public CheckoutPage(ArrayList<String> items, ArrayList<Double> prices) {
        this.selectedItems = items;
        this.selectedPrices = prices;

        setTitle("Checkout");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Display Order Summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < selectedItems.size(); i++) {
            String item = selectedItems.get(i);
            double price = selectedPrices.get(i);
            summaryPanel.add(new JLabel(item + " - $" + price));
        }

        JScrollPane scrollPane = new JScrollPane(summaryPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Total Price Label
        totalLabel = new JLabel("Total: $" + calculateTotal(), SwingConstants.CENTER);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(totalLabel, BorderLayout.NORTH);

        // Payment Method Buttons
        JPanel paymentPanel = new JPanel(new FlowLayout());
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Select Payment Method"));

        // Pay with Credit/Debit Card Button
        JButton creditDebitButton = new JButton("Pay with Credit/Debit Card");
        creditDebitButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        creditDebitButton.addActionListener(e -> proceedToPayment("Credit/Debit Card"));

        // Pay with Cash Button
        JButton cashButton = new JButton("Pay with Cash");
        cashButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        cashButton.addActionListener(e -> proceedToPayment("Cash"));

        paymentPanel.add(creditDebitButton);
        paymentPanel.add(cashButton);

        add(paymentPanel, BorderLayout.SOUTH);
    }

    /**
     * Calculates the total price of all selected items.
     *
     * @return The total amount to be paid.
     */
    private double calculateTotal() {
        return selectedPrices.stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * Handles the payment process based on the selected payment method.
     * Displays a message confirming the selected payment method.
     *
     * @param paymentMethod The chosen method of payment (Credit/Debit Card or Cash).
     */
    private void proceedToPayment(String paymentMethod) {
        String message = "Proceeding with payment via " + paymentMethod;

        // Here you can add logic to handle the payment process or redirect to another stage.
        JOptionPane.showMessageDialog(this, message);
        this.dispose(); // Close the checkout page after payment action
    }
}
