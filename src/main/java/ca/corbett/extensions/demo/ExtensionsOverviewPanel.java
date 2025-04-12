package ca.corbett.extensions.demo;

import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Color;

public class ExtensionsOverviewPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Extensions: overview";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        formPanel.setStandardLeftMargin(24);

        LabelField label = LabelField.createBoldHeaderLabel("Welcome to app-extensions!", 24);
        label.setColor(Color.BLUE);
        label.setBottomMargin(16);
        formPanel.addFormField(label);

        formPanel.addFormField(LabelField.createPlainHeaderLabel("TODO", 14));

        formPanel.render();
        return formPanel;
    }
}
