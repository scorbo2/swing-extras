package ca.corbett.extras.demo.panels;

import ca.corbett.extras.Version;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageTextUtil;
import ca.corbett.extras.properties.Properties;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A quick demo panel to show off the capabilities of ImageTextUtil.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2025-03-15
 */
public class ImageTextUtilDemoPanel extends PanelBuilder {
    private final BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
    private ImagePanel imagePanel;

    private LongTextField textField;
    private FontField fontField;
    private ColorField bgColorField;
    private ColorField textFillColorField;
    private ColorField textOutlineColorField;
    private ComboField<String> outlineWidthField;
    private ComboField<ImageTextUtil.TextAlign> textAlignField;
    private NumberField lineWrapField;

    private String text;
    private String fontFamily;
    private boolean isBold;
    private boolean isItalic;
    private Gradient bgGradient;
    private Color fillColor;
    private Color outlineColor;
    private int outlineWidth;
    private ImageTextUtil.TextAlign textAlign;
    private int lineWrapAt;

    public ImageTextUtilDemoPanel() {
        text = "This is the ImageTextUtil class in swing-extras... let's generate some text!";
        fontFamily = Font.SANS_SERIF;
        isBold = false;
        isItalic = false;
        bgGradient = new Gradient(GradientType.HORIZONTAL_STRIPE, Color.BLUE, Color.BLACK);
        fillColor = Color.CYAN;
        outlineColor = Color.DARK_GRAY;
        outlineWidth = 20;
        textAlign = ImageTextUtil.TextAlign.CENTER;
        lineWrapAt = 25;
    }

    @Override
    public String getTitle() {
        return "ImageTextUtil";
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        imagePanel = new ImagePanel(image, ImagePanelConfig.createDefaultProperties());
        imagePanel.setBorder(BorderFactory.createEtchedBorder());

        panel.add(imagePanel, BorderLayout.CENTER);

        FormPanel formPanel = buildFormPanel("ImageTextUtil", 12);

        LabelField labelField = new LabelField("<html>The ImageTextUtil class gives you a way<br>" +
                                            "to write multiple lines of text to an image<br>" +
                                            "with optional fill and outline properties.<br>" +
                                            "Line wrapping can be handled automatically!</html>");
        labelField.getMargins().setBottom(10);
        formPanel.add(labelField);

        // Let's create a small-ish multi-line text field for entering text:
        textField = LongTextField.ofFixedSizeMultiLine("Text:", 4, 16);
        textField.setText(text);
        textField.addValueChangedListener(changeListener);
        formPanel.add(textField);

        // We can add a FontField for picking the desired font:
        fontField = new FontField("Font:");
        fontField.addValueChangedListener(changeListener);
        fontField.setShowSizeField(false);
        formPanel.add(fontField);

        // Solid-color backgrounds are boring, so let's force a gradient background for our image:
        bgColorField = new ColorField("Background:", ColorSelectionType.GRADIENT).setGradient(bgGradient);
        bgColorField.addValueChangedListener(changeListener);
        formPanel.add(bgColorField);

        // For text generation we can limit the color options to solid colors:
        textFillColorField = new ColorField("Text fill:", ColorSelectionType.SOLID).setColor(fillColor);
        textFillColorField.addValueChangedListener(changeListener);
        formPanel.add(textFillColorField);

        // Likewise for text outline color selection, let's limit the options to solid colors:
        textOutlineColorField = new ColorField("Text outline:", ColorSelectionType.SOLID).setColor(outlineColor);
        textOutlineColorField.addValueChangedListener(changeListener);
        formPanel.add(textOutlineColorField);

        // We can build our text alignment options directly from the TextAlign enum:
        textAlignField = new ComboField<>("Text align:",
                                          List.of(ImageTextUtil.TextAlign.values()),
                                          4,
                                          false);
        textAlignField.addValueChangedListener(changeListener);
        formPanel.add(textAlignField);

        // We can add a combo for picking an outline width:
        List<String> options = new ArrayList<>();
        options.add("None");
        options.add("Thin");
        options.add("Medium");
        options.add("Thick");
        outlineWidthField = new ComboField<>("Outline width:", options, 1, false);
        outlineWidthField.addValueChangedListener(changeListener);
        formPanel.add(outlineWidthField);

        // And for line wrap, we can just let the user pick the character count at which wrap will occur:
        lineWrapField = new NumberField("Line wrap:", 25, 5, 100, 1);
        lineWrapField.addValueChangedListener(changeListener);
        formPanel.add(lineWrapField);

        panel.add(formPanel, BorderLayout.WEST);

        render();

        return panel;
    }

    /**
     * Invoked internally to render the image with current settings and display it:
     */
    private void render() {
        Graphics2D graphics = image.createGraphics();
        try {
            // If our gradient makes sense, use it to fill the image:
            if (bgGradient != null) {
                GradientUtil.fill(bgGradient, graphics, 0, 0, image.getWidth(), image.getHeight());
            }

            // Otherwise, something has gone wrong, so default to something else:
            else {
                graphics.setColor(Color.BLACK);
                graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            }
        }
        finally {
            graphics.dispose();
        }

        // If our text makes no sense, default it to something safe:
        if (text.isBlank()) {
            text = Version.NAME;
        }

        // ImageTextUtil has many options for rendering text!
        // It's worth exploring that class to really see what it can do!
        ImageTextUtil.drawText(image,
                               text,
                               lineWrapAt,
                               Properties.createFontFromAttributes(fontFamily, isBold, isItalic, 12),
                               textAlign,
                               outlineColor,
                               outlineWidth,
                               fillColor);

        // Render the results into our ImagePanel:
        //imagePanel.setImage(image);
        // We don't actually need to invoke setImage(), because in this example, we're directly
        // updating the image instance already contained by that panel. But in an actual application,
        // if you've created a new BufferedImage instance, you would need to call setImage() to
        // update the panel to use that new instance. The setImage() method will invoke repaint() for you.

        // For our demo application, we just need to tell the panel to repaint itself:
        imagePanel.repaint();
    }

    /**
     * All of our FormFields can share this one ValueChangedListener, which simply
     * grabs all of our current settings and uses them to render the example image.
     */
    private final ValueChangedListener changeListener = new ValueChangedListener() {
        @Override
        public void formFieldValueChanged(FormField field) {
            text = textField.getText();
            fontFamily = fontField.getSelectedFont().getFamily();
            isBold = fontField.getSelectedFont().isBold();
            isItalic = fontField.getSelectedFont().isItalic();
            bgGradient = (Gradient)bgColorField.getSelectedValue();
            fillColor = textFillColorField.getColor();
            outlineColor = textOutlineColorField.getColor();
            outlineWidth = switch (outlineWidthField.getSelectedIndex()) {
                case 0 -> 0;
                case 1 -> 20;
                case 2 -> 12;
                default -> 8;
            };
            textAlign = textAlignField.getSelectedItem();
            lineWrapAt = (Integer)lineWrapField.getCurrentValue();
            render();
        }
    };
}
