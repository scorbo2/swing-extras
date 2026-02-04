package ca.corbett.extras.demo.panels;

import ca.corbett.extras.ActionPanel;
import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.SwingFormsResources;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.CollapsiblePanelField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

/**
 * To show off the new ActionPanel component.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class ActionPanelDemoPanel extends PanelBuilder {

    private enum BorderOption {
        NONE("None", null),
        THIN_BLACK_LINE("Thin black line border", BorderFactory.createLineBorder(Color.BLACK, 1)),
        THICK_BLACK_LINE("Thick black line border", BorderFactory.createLineBorder(Color.BLACK, 3)),
        THIN_WHITE_LINE("Thin white line border", BorderFactory.createLineBorder(Color.WHITE, 1)),
        THICK_WHITE_LINE("Thick white line border", BorderFactory.createLineBorder(Color.WHITE, 3)),
        ETCHED("Etched border", BorderFactory.createEtchedBorder()),
        RAISED_BEVELED("Raised beveled border", BorderFactory.createRaisedBevelBorder()),
        LOWERED_BEVELED("Lowered beveled border", BorderFactory.createLoweredBevelBorder());

        private final String label;
        private final Border border;

        BorderOption(String label, Border border) {
            this.label = label;
            this.border = border;
        }

        @Override
        public String toString() {
            return label;
        }

        public Border getBorder() {
            return border;
        }
    }

    private ComboField<BorderOption> actionPanelBorderField;
    private ComboField<String> useLabelsField;
    private CheckBoxField sortGroupsByNameField;
    private CheckBoxField sortActionsByNameField;
    private CheckBoxField showGroupHeaderIconsField;
    private CheckBoxField showActionIconsField;
    private ComboField<BorderOption> groupBorderField;
    private ComboField<BorderOption> headerBorderField;
    private NumberField externalPaddingField;
    private NumberField internalPaddingField;
    private NumberField actionIndentField;
    private ComboField<String> stylingField;
    private ColorField actionForegroundField;
    private ColorField actionBackgroundField;
    private ColorField groupHeaderForegroundField;
    private ColorField groupHeaderBackgroundField;
    private ActionPanel actionPanel;
    private JPanel exampleContentPanel;

    @Override
    public String getTitle() {
        return "ActionPanel";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("ActionPanel demo");

        String sb = "<html>The <b>ActionPanel</b> component provides a way to very easily<br>" +
                "group actions into customizable groups within a vertically-stacked list.<br>" +
                "ActionPanel is highly customizable! Try it out below.</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(16);
        formPanel.add(labelField);

        // Put the ActionPanel above the control panel because the control panel will be quite tall:
        actionPanel = new ActionPanel();
        exampleContentPanel = new JPanel(new BorderLayout());
        populateActionPanel();
        PanelField exampleContainer = new PanelField(new BorderLayout());
        exampleContainer.setShouldExpand(true);
        exampleContainer.getPanel().add(actionPanel, BorderLayout.WEST);
        exampleContainer.getPanel().add(exampleContentPanel, BorderLayout.CENTER);
        formPanel.add(exampleContainer);

        // Display the control panel in a collapsible panel, and start it collapsed:
        CollapsiblePanelField containerField = new CollapsiblePanelField("Configuration options",
                                                                         false,
                                                                         new BorderLayout());
        containerField.getPanel().add(buildControlPanel(), BorderLayout.CENTER);
        containerField.setShouldExpandHorizontally(true);
        containerField.getMargins().setTop(24); // Move it away from the example panel a bit
        formPanel.add(containerField);

        return formPanel;
    }

    private FormPanel buildControlPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);

        actionPanelBorderField = new ComboField<>("ActionPanel border:", Arrays.asList(BorderOption.values()), 0);
        actionPanelBorderField.addValueChangedListener(
                f -> actionPanel.setBorder(actionPanelBorderField.getSelectedItem().getBorder()));
        formPanel.add(actionPanelBorderField);

        headerBorderField = new ComboField<>("Header border:", Arrays.asList(BorderOption.values()), 0);
        headerBorderField.addValueChangedListener(
                f -> actionPanel.setGroupHeaderBorder(headerBorderField.getSelectedItem().getBorder()));
        formPanel.add(headerBorderField);

        groupBorderField = new ComboField<>("Group border:", Arrays.asList(BorderOption.values()), 0);
        groupBorderField.addValueChangedListener(
                f -> actionPanel.setGroupBorder(groupBorderField.getSelectedItem().getBorder()));
        formPanel.add(groupBorderField);

        sortGroupsByNameField = new CheckBoxField("Sort groups by name", false);
        sortGroupsByNameField.addValueChangedListener(f -> setGroupComparator());
        formPanel.add(sortGroupsByNameField);

        sortActionsByNameField = new CheckBoxField("Sort actions by name", false);
        sortActionsByNameField.addValueChangedListener(f -> setActionComparator());
        formPanel.add(sortActionsByNameField);

        showActionIconsField = new CheckBoxField("Show action icons", true);
        showActionIconsField.addValueChangedListener(
                f -> actionPanel.setShowActionIcons(showActionIconsField.isChecked()));
        formPanel.add(showActionIconsField);

        showGroupHeaderIconsField = new CheckBoxField("Show group header icons", true);
        showGroupHeaderIconsField.addValueChangedListener(
                f -> actionPanel.setShowGroupIcons(showGroupHeaderIconsField.isChecked()));
        formPanel.add(showGroupHeaderIconsField);

        externalPaddingField = new NumberField("External padding:", ActionPanel.DEFAULT_EXTERNAL_PADDING, 0, 32, 1);
        externalPaddingField.addValueChangedListener(
                f -> actionPanel.setExternalPadding(externalPaddingField.getCurrentValue().intValue()));
        formPanel.add(externalPaddingField);

        internalPaddingField = new NumberField("Internal padding:", ActionPanel.DEFAULT_INTERNAL_PADDING, 0, 32, 1);
        internalPaddingField.addValueChangedListener(
                f -> actionPanel.setInternalPadding(internalPaddingField.getCurrentValue().intValue()));
        formPanel.add(internalPaddingField);

        actionIndentField = new NumberField("Action left indent:", 0, 0, 32, 1);
        actionIndentField.addValueChangedListener(
                f -> actionPanel.setActionIndent(actionIndentField.getCurrentValue().intValue()));
        formPanel.add(actionIndentField);

        List<String> options = List.of("Show actions as clickable labels", "Show actions as buttons");
        useLabelsField = new ComboField<>("Action style:", options, 0);
        useLabelsField.addValueChangedListener(f -> {
            if (useLabelsField.getSelectedIndex() == 0) {
                actionPanel.setUseLabels();
            }
            else {
                actionPanel.setUseButtons();
            }
        });
        formPanel.add(useLabelsField);

        options = List.of("Use Look and Feel defaults", "Override with custom styling");
        stylingField = new ComboField<>("Styling:", options, 0);
        stylingField.addValueChangedListener(f -> styleFieldChanged());
        formPanel.add(stylingField);

        actionForegroundField = new ColorField("Action foreground:", ColorSelectionType.SOLID);
        actionForegroundField.setEnabled(false);
        actionForegroundField.setColor(Color.BLACK);
        actionForegroundField.getMargins().setLeft(16); // indent a bit to show that these are styling options
        actionForegroundField.addValueChangedListener(f -> styleFieldChanged());
        formPanel.add(actionForegroundField);

        actionBackgroundField = new ColorField("Action background:", ColorSelectionType.SOLID);
        actionBackgroundField.setEnabled(false);
        actionBackgroundField.setColor(Color.LIGHT_GRAY);
        actionBackgroundField.getMargins().setLeft(16);
        actionBackgroundField.addValueChangedListener(f -> styleFieldChanged());
        formPanel.add(actionBackgroundField);

        groupHeaderForegroundField = new ColorField("Group header foreground:", ColorSelectionType.SOLID);
        groupHeaderForegroundField.setEnabled(false);
        groupHeaderForegroundField.setColor(Color.WHITE);
        groupHeaderForegroundField.getMargins().setLeft(16);
        groupHeaderForegroundField.addValueChangedListener(f -> styleFieldChanged());
        formPanel.add(groupHeaderForegroundField);

        groupHeaderBackgroundField = new ColorField("Group header background:", ColorSelectionType.SOLID);
        groupHeaderBackgroundField.setEnabled(false);
        groupHeaderBackgroundField.setColor(new Color(70, 130, 180)); // steel blue
        groupHeaderBackgroundField.getMargins().setLeft(16);
        groupHeaderBackgroundField.addValueChangedListener(f -> styleFieldChanged());
        formPanel.add(groupHeaderBackgroundField);

        return formPanel;
    }

    /**
     * Enable or disable the custom styling fields based on selection.
     * Also applies the selected colors to the ActionPanel.
     */
    private void styleFieldChanged() {
        boolean isCustom = stylingField.getSelectedIndex() == 1;
        actionForegroundField.setEnabled(isCustom);
        actionBackgroundField.setEnabled(isCustom);
        groupHeaderForegroundField.setEnabled(isCustom);
        groupHeaderBackgroundField.setEnabled(isCustom);
        actionPanel.setActionForeground(isCustom ? actionForegroundField.getColor() : null);
        actionPanel.setActionBackground(isCustom ? actionBackgroundField.getColor() : null);
        actionPanel.setGroupHeaderForeground(isCustom ? groupHeaderForegroundField.getColor() : null);
        actionPanel.setGroupHeaderBackground(isCustom ? groupHeaderBackgroundField.getColor() : null);
    }

    /**
     * Invoked internally to alphabetize the action groups by name if the sort
     * checkbox is selected (otherwise will sort by add order, which is deliberately not alphabetical).
     */
    private void setGroupComparator() {
        if (sortGroupsByNameField.isChecked()) {
            actionPanel.setGroupComparator(String::compareToIgnoreCase);
        }
        else {
            actionPanel.setGroupComparator(null);
        }
    }

    /**
     * Invoked internally to alphabetize the actions by name if the sort
     * checkbox is selected (otherwise will sort by add order, which is deliberately not alphabetical).
     * Note that you can sort actions by any of their properties! This example
     * sorts them by name, but you could choose to sort them based on whether they have an
     * icon, or by some other criteria within your application.
     */
    private void setActionComparator() {
        if (sortActionsByNameField.isChecked()) {
            actionPanel.setActionComparator((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        }
        else {
            actionPanel.setActionComparator(null);
        }
    }

    /**
     * Adds some example actions to our action panel.
     */
    private void populateActionPanel() {
        Icon icon = SwingFormsResources.getCopyIcon(16);
        actionPanel.addAll("First group", List.of(
                new ExampleAction("Intro to ActionPanel", icon,
                                  "Intro to ActionPanel",
                                  "<html>The supplied actions can do anything your application wants!<br>"
                                          + "In this example, clicking the actions on the left will update<br>"
                                          + "this panel with example text. This can be used as a navigation menu.<br><br>"
                                          + "But in a real application, these actions could launch dialogs,<br>"
                                          + "start long-running processes, or do anything you need.</html>"),
                new ExampleAction("Styling options", icon,
                                  "Styling options",
                                  "<html>Use the configuration options panel below to customize<br>"
                                          + "the appearance and behavior of the ActionPanel on the left.<br>"
                                          + "You can change borders, padding, sorting, colors, and more!</html>"),
                new ExampleAction("Reduce clutter!", icon,
                                  "Reduce clutter!",
                                  "<html>ActionPanel is great for reducing UI clutter, because each<br>"
                                          + "action group can be collapsed or expanded as needed.<br>"
                                          + "Try it out!</html>")
        ));
        actionPanel.setGroupIcon("First group", SwingFormsResources.getValidIcon(16));

        actionPanel.addAll("Second group", List.of(
                new ExampleAction("Example action", icon, "Actions can be sorted",
                                  "<html>By default, actions are shown in the order they are added.<br>"
                                          + "but you can choose to have them sorted by name instead.<br>"
                                          + "Try it out with the controls above, and you'll see that<br>"
                                          + "this action (added first) will be sorted after the others (alphabetically).</html>"),
                new ExampleAction("Action A", icon, "Action A", "You clicked Action A!"),
                new ExampleAction("Action B", icon, "Action B", "You clicked Action B!")
        ));
        actionPanel.setGroupIcon("Second group", SwingFormsResources.getValidIcon(16));

        actionPanel.addAll("Another group", List.of(
                new ExampleAction("Last action", icon, "Last action", "You clicked the last action!")
        ));
        actionPanel.setGroupIcon("Another group", SwingFormsResources.getValidIcon(16));

        populateExamplePanel("ActionPanel demo",
                             "<html>Click an action in the ActionPanel to the left to see it in action!<br>"
                                     + "Use the configuration options panel below to customize it.</html>");
    }

    /**
     * Throws some static text into our example content panel.
     */
    private void populateExamplePanel(String title, String text) {
        exampleContentPanel.removeAll();
        FormPanel panel = new FormPanel(Alignment.TOP_LEFT);
        panel.setBorderMargin(12);
        LabelField headerLabel = LabelField.createBoldHeaderLabel(title, 16, 0, 6);
        headerLabel.getMargins().setTop(0);
        panel.add(headerLabel);
        panel.add(new LabelField(text));
        exampleContentPanel.add(panel, BorderLayout.CENTER);
        exampleContentPanel.revalidate();
        exampleContentPanel.repaint();
    }

    /**
     * A very simple action implementation that just populates the example panel
     * with some static text when invoked.
     */
    private class ExampleAction extends EnhancedAction {

        private final String name;
        private final Icon icon;
        private final String exampleTitle;
        private final String exampleText;

        public ExampleAction(String name, Icon icon, String exampleTitle, String exampleText) {
            super(name);
            this.name = name;
            this.icon = icon;
            this.exampleTitle = exampleTitle;
            this.exampleText = exampleText;
            setIcon(icon);
            // No tooltip for this demo
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            populateExamplePanel(exampleTitle, exampleText);
        }
    }
}
