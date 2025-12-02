package ca.corbett.forms.demo;

import ca.corbett.extras.demo.SnippetAction;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.SliderField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.JPanel;
import java.awt.Color;
import java.util.List;

/**
 * A demo panel for showing off some "advanced", or less-commonly-used, FormField implementations.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class AdvancedFormPanel extends PanelBuilder {
    private FormPanel formPanel;

    @Override
    public String getTitle() {
        return "Forms: advanced fields";
    }

    @Override
    public JPanel build() {
        formPanel = buildFormPanel("Advanced form fields");

        // Show some file and directory choosers with various options:
        formPanel.add(LabelField.createBoldHeaderLabel("File and directory choosers"));
        formPanel.add(new CheckBoxField("Show hidden files", false)
                              .addValueChangedListener(new HiddenFileChangeListener()));
        formPanel.add(new FileField("File chooser:", null, 15, FileField.SelectionType.AnyFile));
        formPanel.add(new FileField("With image preview:", null, 15, FileField.SelectionType.AnyFile)
                              .setFileFilter(new ImageUtil.ImageFileFilter()) // limit to image files
                              .setAccessory(new FileField.ImagePreviewAccessory())); // include image preview
        formPanel.add(new FileField("Dir chooser:", null, 15, FileField.SelectionType.ExistingDirectory));
        formPanel.add(createSnippetLabel(new FileFieldSnippetAction()));

        // Sliders are pretty neat, especially with customizations offered by swing-forms:
        formPanel.add(LabelField.createBoldHeaderLabel("Sliders"));
        formPanel.add(new LabelField(
                "<html>Sliders are sometimes more visually interesting than number spinners.<br>"
                        + "And with custom labels, they can even replace comboboxes for simple selections!</html>"));
        formPanel.add(new SliderField("Standard slider:", 0, 100, 50).setShowValueLabel(false));
        formPanel.add(new SliderField("With value label:", 0, 100, 50).setShowValueLabel(true));
        formPanel.add(new SliderField("Custom colors:", 0, 100, 50)
                              .setColorStops(List.of(Color.BLACK, Color.BLUE, Color.CYAN, Color.WHITE)));
        formPanel.add(new SliderField("Custom labels:", 0, 100, 50)
                              .setColorStops(List.of(Color.RED, Color.YELLOW, Color.GREEN))
                              .setLabels(List.of("Very low", "Low", "Medium", "High", "Very high"), false));
        formPanel.add(createSnippetLabel(new SliderSnippetAction()));

        // Color and color gradient choosers are also available:
        formPanel.add(LabelField.createBoldHeaderLabel("Color choosers"));
        formPanel.add(new LabelField(
                "<html>Both solid colors and color gradients are supported.<br>" +
                        "Color gradients can be used with ImageTextUtil and DesktopPane.</html>"));
        formPanel.add(new ColorField("Solid color:", ColorSelectionType.SOLID).setColor(Color.BLUE));
        formPanel.add(new ColorField("Gradients:", ColorSelectionType.GRADIENT).setGradient(buildDefaultGradient()));
        formPanel.add(new ColorField("Either:", ColorSelectionType.EITHER).setColor(Color.RED));
        formPanel.add(createSnippetLabel(new ColorFieldSnippetAction(), 0));

        return formPanel;
    }

    /**
     * Invoked internally to build some gradient for initial display purposes:
     */
    private Gradient buildDefaultGradient() {
        return new Gradient(GradientType.DIAGONAL2, Color.GREEN, Color.BLACK);
    }

    /**
     * A simple ValueChangeListener that we can attach to our "show hidden files" checkbox
     * to update all FileFields on the form to behave accordingly when launched.
     */
    private class HiddenFileChangeListener implements ValueChangedListener {
        @Override
        public void formFieldValueChanged(FormField field) {
            // Get the checked state of the checkbox that caused this event:
            boolean isChecked = ((CheckBoxField)field).isChecked();

            // Now stream through all fields on our form panel looking for FileFields:
            formPanel.getFormFields()
                     .stream()
                     .filter(f -> f instanceof FileField)
                     .forEach(f -> ((FileField)f).setFileHidingEnabled(!isChecked));
        }
    }

    /**
     * Shows a snippet for how to create FileFields.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class FileFieldSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    // Start with a blank FormPanel:
                    FormPanel formPanel = new FormPanel();
                    
                    // Add a simple FileField for selecting any file:
                    formPanel.add(new FileField("File chooser:", null, 15, FileField.SelectionType.AnyFile));
                    
                    // Let's limit the selection to image files and add an image preview:
                    formPanel.add(new FileField("With image preview:", null, 15, FileField.SelectionType.AnyFile)
                                          .setFileFilter(new ImageUtil.ImageFileFilter()) // limit to image files
                                          .setAccessory(new FileField.ImagePreviewAccessory())); // include image preview
                    
                    // We can also limit the selection to directories instead of files:
                    formPanel.add(new FileField("Dir chooser:", null, 15, FileField.SelectionType.ExistingDirectory));
                    """;
        }
    }

    /**
     * Shows a code snippet for creating SliderFields.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class SliderSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    // Start with a blank FormPanel:
                    FormPanel formPanel = new FormPanel();
                    
                    // Create a boring standard SliderField with no value label:
                    formPanel.add(new SliderField("Standard slider:", 0, 100, 50).setShowValueLabel(false));
                    
                    // Create a boring standard SliderField with a simple numeric value label:
                    formPanel.add(new SliderField("With value label:", 0, 100, 50).setShowValueLabel(true));
                    
                    // We can add custom color stops to make the SliderField look more interesting:
                    formPanel.add(new SliderField("Custom colors:", 0, 100, 50)
                                    .setColorStops(List.of(Color.BLACK, Color.BLUE, Color.CYAN, Color.WHITE)));
                    
                    // And we can add custom labels to represent our colors:
                    formPanel.add(new SliderField("Custom labels:", 0, 100, 50)
                                    .setColorStops(List.of(Color.RED, Color.YELLOW, Color.GREEN))
                                    .setLabels(List.of("Very low", "Low", "Medium", "High", "Very high"), false));
                    
                    // Note that the count of custom labels doesn't have to match the count of color stops!
                    // SliderField is smart enough to interpolate accordingly.
                    """;
        }
    }

    /**
     * Shows a code snippet for creating ColorFields.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class ColorFieldSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    ColorField solidOnly = new ColorField("Solid color:", ColorSelectionType.SOLID);
                    solidOnly.setColor(Color.BLUE); // or whatever initial value we want
                    
                    ColorField gradientOnly = new ColorField("Gradients:", ColorSelectionType.GRADIENT);
                    gradientOnly.setGradient(buildDefaultGradient()); // set our initial gradient
                    
                    ColorField either = new ColorField("Either:", ColorSelectionType.EITHER);
                    either.setColor(Color.RED); // set a solid color initially, but user can also choose gradients.
                    """;
        }
    }
}
