package ca.corbett.forms.fields;

import ca.corbett.extras.LookAndFeelManager;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultCaret;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

/**
 * The HtmlLabelField can be used to convert part of a label into a hyperlink,
 * or which can be used to embed multiple separate hyperlinks within the same label.
 * This is a more advanced version of the standard LabelField, which only supports a single
 * hyperlink for the entire label.
 * <p>
 * <b>USAGE:</b> To use the HtmlLabelField, you supply an html-formatted label text,
 * and a custom Action which will be triggered when the hyperlink is clicked. the command
 * which is supplied to the Action will match the href value specified in the html label text.
 * For example:
 * </p>
 * <pre>
 * final String html = "&lt;html&gt;Would you like to "
 *                     + "&lt;a href='proceed'&gt;proceed&lt;/a&gt;"
 *                     + " or &lt;a href='cancel'&gt;cancel&lt;/a&gt;?&lt;/html&gt;";
 * HtmlLabelField labelField = new HtmlLabelField(html, myCustomAction);
 * </pre>
 * <p>
 * The Action that you supply will receive an actionPerformed() call when the link is clicked,
 * and the action command will be "proceed" or "cancel" in this example.
 * Here's what the Action might look like:
 * </p>
 * <pre>
 *     Action myCustomAction = new AbstractAction() {
 *         &#64;Override
 *         public void actionPerformed(ActionEvent e) {
 *             String command = e.getActionCommand();
 *             if ("proceed".equals(command)) {
 *                 // Do whatever you need to do when the proceed link is clicked
 *             }
 *             else if ("cancel".equals(command)) {
 *                 // Do whatever you need to do when the cancel link is clicked
 *             }
 *         }
 *     };
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class HtmlLabelField extends FormField {

    private final JEditorPane label;
    private Action linkAction;
    private HyperlinkListener linkListener;

    /**
     * For creating a HtmlLabelField with no field label.
     * The supplied linkAction will receive an actionPerformed() when any link is clicked,
     * and the action command will be the href value of the clicked link.
     * If the supplied linkAction is null, no action will be performed when links are clicked.
     */
    public HtmlLabelField(String labelText, Action linkAction) {
        this("", labelText, linkAction);
    }

    /**
     * For creating a HtmlLabelField with a field label.
     * The supplied linkAction will receive an actionPerformed() when any link is clicked,
     * and the action command will be the href value of the clicked link.
     * If the supplied linkAction is null, no action will be performed when links are clicked.
     */
    public HtmlLabelField(String fieldLabelText, String labelText, Action linkAction) {
        label = new JEditorPane("text/html", labelText == null ? "" : labelText);
        label.setEditable(false);
        label.setOpaque(false); // Make it look like a JLabel!
        label.setBorder(null);  // Make it look like a JLabel!
        label.setCaret(new DoNothingCaret()); // Prevent the caret from showing
        label.setHighlighter(null);  // Also remove the highlighter to prevent selection
        label.setFont(getDefaultFont());
        label.setForeground(LookAndFeelManager.getLafColor("Label.foreground", Color.BLACK));
        fieldLabel.setText(fieldLabelText == null ? "" : fieldLabelText);
        fieldComponent = label;
        registerLinkListener(linkAction);
    }

    /**
     * Overridden here as we generally don't want to show a validation label on a label.
     * Will return true only if one or more FieldValidators have been explicitly assigned.
     * Yes! You can assign a FieldValidator to a HtmlLabelField if you really want to, and it
     * will perform validation if so. This generally makes no sense, as HtmlLabelFields do not
     * allow user input, and so validation is disabled by default.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }

    /**
     * Returns the current label text in html format.
     *
     * @return The text of the label in html format.
     */
    public String getText() {
        return label.getText();
    }

    /**
     * Returns the Action that is triggered when links are clicked.
     */
    public Action getLinkAction() {
        return linkAction;
    }

    /**
     * Sets new label text in html format, and wires it up with the given Action.
     * This will replace any previously assigned Action and label text.
     *
     * @param text The new label text in html format.
     * @param action The Action to trigger when links are clicked.
     */
    public HtmlLabelField setText(String text, Action action) {
        label.setText(text == null ? "" : text);
        registerLinkListener(action);
        return this;
    }

    /**
     * Sets the font to use for the label text.
     * This is shorthand for ((JEditorPane)getFieldComponent()).setFont()
     *
     * @param font The new Font to use.
     */
    public HtmlLabelField setFont(Font font) {
        if (font == null) {
            return this;
        }
        label.setFont(font);
        return this;
    }

    /**
     * Returns the current Font used for the label text.
     */
    public Font getFont() {
        return label.getFont();
    }

    /**
     * Invoked internally to create and return a HyperlinkListener that triggers
     * the given Action when links are clicked.
     * If any listener was previously created, it is unregistered from our editor pane.
     */
    private void registerLinkListener(Action action) {
        this.linkAction = action;

        // Unregister any existing listener:
        if (linkListener != null) {
            label.removeHyperlinkListener(linkListener);
            linkListener = null;
        }

        // If we have no action, nothing to do:
        if (action == null) {
            return;
        }

        // Create a new listener with this action:
        linkListener = e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String href = e.getDescription();
                // Pass the href as the action command
                linkAction.actionPerformed(new ActionEvent(label, 0, href));
            }
        };

        // Register it:
        label.addHyperlinkListener(linkListener);
    }

    /**
     * A caret implementation that does nothing, to prevent the caret from showing
     * in the JEditorPane used for our label.
     */
    private static class DoNothingCaret extends DefaultCaret {
        @Override
        public void setVisible(boolean visible) {
            // Do nothing
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public void setSelectionVisible(boolean visible) {
            // Do nothing
        }
    }
}
