import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ListSubsetField;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.List;

/**
 * A simple test program to verify the drag and drop functionality
 * of ListSubsetField works correctly.
 */
public class ListSubsetDragDropTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ListSubsetField Drag and Drop Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            FormPanel formPanel = new FormPanel();
            
            // Test 1: With auto-sorting disabled (should allow reordering)
            ListSubsetField<String> field1 = new ListSubsetField<>("Test without auto-sort:",
                    List.of("Zebra", "Apple", "Mango", "Banana", "Cherry"),
                    List.of("Apple", "Cherry"))
                    .setAutoSortingEnabled(false)  // Allow reordering
                    .setFixedCellWidth(120);
            formPanel.add(field1);
            
            // Test 2: With auto-sorting enabled (should NOT allow reordering)
            ListSubsetField<String> field2 = new ListSubsetField<>("Test with auto-sort:",
                    List.of("Zebra", "Apple", "Mango", "Banana", "Cherry"),
                    List.of("Apple", "Cherry"))
                    .setAutoSortingEnabled(true)  // Disable reordering
                    .setFixedCellWidth(120);
            formPanel.add(field2);
            
            frame.add(formPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("Test Instructions:");
            System.out.println("1. Top field (auto-sort OFF): You SHOULD be able to drag items within each list to reorder them");
            System.out.println("2. Top field: You SHOULD be able to drag items between the two lists");
            System.out.println("3. Bottom field (auto-sort ON): You SHOULD NOT be able to drag items within each list");
            System.out.println("4. Bottom field: You SHOULD be able to drag items between lists (but they will be sorted)");
        });
    }
}
