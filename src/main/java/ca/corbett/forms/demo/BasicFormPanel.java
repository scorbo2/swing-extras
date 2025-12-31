package ca.corbett.forms.demo;

import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.demo.SnippetAction;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.HtmlLabelField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PasswordField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Builds a FormPanel that contains examples of "basic" FormFields. These are the
 * ones that are most commonly used, like text input, checkboxes, comboboxes,
 * static labels, and such.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class BasicFormPanel extends PanelBuilder {

    private FormPanel formPanel;

    @Override
    public String getTitle() {
        return "Forms: basic fields";
    }

    @Override
    public JPanel build() {
        formPanel = buildFormPanel("Basic form components");

        // Let's add some basic text input fields with various options:
        formPanel.add(LabelField.createBoldHeaderLabel("Text input"));
        formPanel.add(new ShortTextField("Single-line text:", 16).setText("Hello!"));
        formPanel.add(new PasswordField("Password entry:", 12).setPassword("password"));
        formPanel.add(LongTextField.ofFixedSizeMultiLine("Multi-line text:", 3, 21)
                                   .setText("Text fields are great for long text entry."));
        LongTextField textField = LongTextField.ofFixedSizeMultiLine("With pop-out edit:", 3, 21);
        textField.setAllowPopoutEditing(true);
        textField.setText("You can hit the \"Pop out\" button to edit this text in a resizable popup window.");
        formPanel.add(textField);
        LabelField snippetLabel = createSnippetLabel(new TextFieldSnippetAction());
        snippetLabel.getMargins().setTop(0);
        formPanel.add(snippetLabel);

        // Now we can show miscellaneous stuff that every form can use:
        formPanel.add(LabelField.createBoldHeaderLabel("General input components"));
        formPanel.add(new CheckBoxField("Checkboxes", true));
        formPanel.add(buildComboField());
        formPanel.add(new LabelField("Hyperlink:", "Yes, you can add hyperlinks to your forms!")
                              .setHyperlink(new ExampleHyperlinkAction()));
        formPanel.add(buildHtmlLabelField()); // New in swing-extras 2.6!
        formPanel.add(buildButtonField()); // New in swing-extras 2.6!
        formPanel.add(new NumberField("Number chooser:", 0, 0, 100, 1));
        formPanel.add(createSnippetLabel(new GeneralFieldSnippetAction()));

        // And finally, we can show what's possible with static labels:
        formPanel.add(LabelField.createBoldHeaderLabel("Static labels"));
        formPanel.add(new LabelField(
                "<html>Static labels are great for showing information to the user<br>"
                        + "or for providing context around form input.<br><br>"
                        + "For example, note that this form panel uses header labels to divide<br>"
                        + "the form into sections, which is visually helpful.<br><br>"
                        + "Also note that static labels can be multi-line when needed!</html>"));
        LabelField styledLabel = new LabelField("LabelFields can have custom styling!");
        styledLabel.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 14));
        styledLabel.setColor(Color.MAGENTA);
        formPanel.add(styledLabel);

        return formPanel;
    }

    private ComboField<String> buildComboField() {
        List<String> options = List.of(
                "Option 1",
                "Option 2",
                "Option 3");
        return new ComboField<>("Comboboxes:", options, 0, false);
    }

    private ButtonField buildButtonField() {
        ButtonField buttonField = new ButtonField();
        buttonField.addButton(buildExampleAction("Button1"));
        buttonField.addButton(buildExampleAction("Button2"));
        buttonField.setButtonPreferredSize(new Dimension(90, 25));
        buttonField.getFieldLabel().setText("Button field:");
        return buttonField;
    }

    private Action buildExampleAction(String name) {
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "You clicked " + name + "!");
            }
        };
    }

    private HtmlLabelField buildHtmlLabelField() {
        final String html = "<html>Would you like to "
                + "<a href='link1'>proceed</a> or "
                + "<a href='link2'>cancel</a>?</html>";
        return new HtmlLabelField("Multiple links:", html, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                String message;
                if ("link1".equals(command)) {
                    message = "You clicked the proceed link!";
                }
                else if ("link2".equals(command)) {
                    message = "You clicked the cancel link!";
                }
                else {
                    message = "Unknown link clicked: " + command;
                }
                JOptionPane.showMessageDialog(DemoApp.getInstance(), message);
            }
        });
    }

    /**
     * A label hyperlink can be wired up to any AbstractAction to do whatever you need it to do.
     * For this demo app, let's just show a message dialog when the link is clicked.
     */
    private static class ExampleHyperlinkAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(DemoApp.getInstance(), "You clicked the link! Hooray!");
        }
    }

    /**
     * Shows a code snippet for creating simple text input fields.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class TextFieldSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    // Start with a blank FormPanel:
                    FormPanel formPanel = new FormPanel();
                    
                    // Add a simple one-line text entry field:
                    formPanel.add(new ShortTextField("Single-line text:", 16).setText("Hello!"));
                    
                    // Add a password entry field:
                    formPanel.add(new PasswordField("Password entry:", 12).setPassword("password"));
                    
                    // Add a multi-line text entry field:
                    formPanel.add(LongTextField.ofFixedSizeMultiLine("Multi-line text:", 3, 21)
                                               .setText("Text fields are great for long text entry."));
                    
                    // Add a multi-line text entry field with popout editing enabled:
                    LongTextField textField = LongTextField.ofFixedSizeMultiLine("With pop-out edit:", 3, 21);
                    textField.setAllowPopoutEditing(true);
                    textField.setText("You can hit the \\"Pop out\\" button to edit this text in a resizable popup window.");
                    """;
        }
    }

    /**
     * Shows a code snippet for creating general form components.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class GeneralFieldSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    // Start with a blank FormPanel:
                    FormPanel formPanel = new FormPanel();
                    
                    // Checkboxes are very easy:
                    formPanel.add(new CheckBoxField("Checkboxes", true));
                    
                    // Comboboxes require a list of stuff to display:
                    List<String> options = List.of(
                        "Option 1",
                        "Option 2",
                        "Option 3");
                    formPanel.add(new ComboField<>("Comboboxes:", options, 0, false));
                    
                    // Hyperlinks can be added by setting a hyperlink action on a regular LabelField:
                    formPanel.add(new LabelField("Hyperlink:", "Yes, you can add hyperlinks to your forms!")
                                                  .setHyperlink(new ExampleHyperlinkAction()));
                    
                    // Or you can use HtmlLabelField for multiple links in one label:
                    final String html = "<html>Would you like to "
                                        + "<a href='link1'>proceed</a> or "
                                        + "<a href='link2'>cancel</a>?</html>";
                    formPanel.add(new HtmlLabelField("Multiple links:", html, new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String command = e.getActionCommand();
                            // Handle link clicks here... (command will be 'link1' or 'link2' in this example)
                        }
                    }));
                    
                    // NumberFields require an acceptable range and some initial value:
                    formPanel.add(new NumberField("Number chooser:", 0, 0, 100, 1));
                    """;
        }
    }
}
