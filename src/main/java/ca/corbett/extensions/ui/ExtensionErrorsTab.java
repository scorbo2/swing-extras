package ca.corbett.extensions.ui;

import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.ListPanel;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;

/**
 * Shows a list of StartupErrors reported by ExtensionManager.
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ExtensionErrorsTab extends JPanel {

    private final ListPanel<ExtensionManager.StartupError> errorList;
    private final Window ownerWindow;
    private final JPanel contentPanel;

    public ExtensionErrorsTab(Window owner, ExtensionManager<?> extManager) {
        super(new BorderLayout());
        this.ownerWindow = owner;
        errorList = new ListPanel<>("Startup errors", null);
        errorList.addItems(extManager.getStartupErrors());
        errorList.addListSelectionListener(e -> listSelectionChanged());

        add(errorList, BorderLayout.WEST);
        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    private void listSelectionChanged() {
        contentPanel.removeAll();
        ExtensionManager.StartupError error = errorList.getSelected();
        if (error != null) {
            contentPanel.add(buildErrorPanel(error), BorderLayout.CENTER);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void removeJarFile(File jarFile) {
        if (jarFile == null || !jarFile.exists()) {
            JOptionPane.showMessageDialog(ownerWindow, "Jar file not found. It may have already been removed.");
            return;
        }

        if (JOptionPane.showConfirmDialog(ownerWindow,
                                          "Are you sure you wish to remove this jar file?",
                                          "Confirm",
                                          JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
            return;
        }

        for (int i = errorList.getItemCount() - 1; i >= 0; i--) {
            if (errorList.getItemAt(i).getJarFile().getAbsolutePath().equals(jarFile.getAbsolutePath())) {
                errorList.removeItemAt(i);
            }
        }
        errorList.selectItem(-1);
        errorList.revalidate();
        errorList.repaint();

        jarFile.delete();
        JOptionPane.showMessageDialog(ownerWindow, "Jar file removed.");


    }

    private FormPanel buildErrorPanel(ExtensionManager.StartupError error) {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        formPanel.add(LabelField.createBoldHeaderLabel(error.getJarFile().getName()));
        formPanel.add(new LabelField("Jar file:", error.getJarFile().getAbsolutePath()));
        LongTextField errorField = LongTextField.ofDynamicSizingMultiLine("Error message:", 5);
        errorField.setEditable(false);
        errorField.setText(error.getErrorMessage());
        errorField.setAllowPopoutEditing(true);
        formPanel.add(errorField);

        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("Remove jar file");
        button.addActionListener(e -> removeJarFile(error.getJarFile()));
        panelField.getPanel().add(button);
        formPanel.add(panelField);

        return formPanel;
    }
}
