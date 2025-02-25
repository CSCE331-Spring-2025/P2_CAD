import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class CheckoutPage extends JFrame {
    private ArrayList<String> selectedItems;
    private ArrayList<Double> selectedPrices;
    private JLabel totalLabel;
    private JRadioButton creditDebitButton;
    private JRadioButton cashButton;

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

        // Payment Method Selection
        JPanel paymentPanel = new JPanel(new FlowLayout());
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Select Payment Method"));

        creditDebitButton = new JRadioButton("Credit/Debit Card");
        cashButton = new JRadioButton("Cash");

        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(creditDebitButton);
        paymentGroup.add(cashButton);

        paymentPanel.add(creditDebitButton);
        paymentPanel.add(cashButton);

        // Default to Credit/Debit Card selected
        creditDebitButton.setSelected(true);

        add(paymentPanel, BorderLayout.SOUTH);

        // Proceed to Payment Button
        JButton proceedButton = new JButton("Proceed to Payment");
        proceedButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        proceedButton.addActionListener(e -> proceedToPayment());
        add(proceedButton, BorderLayout.PAGE_END);
    }

    private double calculateTotal() {
        return selectedPrices.stream().mapToDouble(Double::doubleValue).sum();
    }

    private void proceedToPayment() {
        String paymentMethod = creditDebitButton.isSelected() ? "Credit/Debit Card" : "Cash";
        String message = "Proceeding with payment via " + paymentMethod;

        // Here you can add logic to handle the payment process or redirect to another stage.
        JOptionPane.showMessageDialog(this, message);
        this.dispose(); // Close the checkout page after payment action
    }
}
