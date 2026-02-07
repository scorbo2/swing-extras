package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.HtmlLabelField;

import javax.swing.Action;

/**
 * Represents an html label, which can have multiple hyperlinks with custom actions.
 * Labels are static form fields, so they do not save or load anything
 * to or from properties.
 * <p>
 * <b>USAGE:</b> Refer to the HtmlLabelField class for detailed usage instructions.
 * Basically, you must supply the html-formatted label text, and an Action
 * which will be triggered when any of the hyperlinks are clicked.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class HtmlLabelProperty extends AbstractProperty {

    private Action linkAction;
    private String html;

    public HtmlLabelProperty(String name, String labelHtml, Action linkAction) {
        this(name, "", labelHtml, linkAction);
    }

    public HtmlLabelProperty(String name, String fieldLabel, String labelHtml, Action linkAction) {
        super(name, fieldLabel);
        this.html = labelHtml;
        this.linkAction = linkAction;

        // Most properties generate FormField instances that allow user input, but we do not:
        allowsUserInput = false;
    }

    public Action getLinkAction() {
        return linkAction;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html, Action linkAction) {
        this.html = html;
        this.linkAction = linkAction;
    }

    @Override
    public void saveToProps(Properties props) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

    @Override
    public void loadFromProps(Properties props) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return new HtmlLabelField(propertyLabel, html, linkAction);
    }

    @Override
    public void loadFromFormField(FormField field) {
        // Labels are static form fields, so there's literally nothing to do here.
    }
}
