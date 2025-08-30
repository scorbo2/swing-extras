package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.Version;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageTextUtil;
import ca.corbett.extras.properties.Properties;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ValueChangedListener;

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
    private Color bgSolidColor;
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
        outlineColor = Color.ORANGE;
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
        panel.add(imagePanel, BorderLayout.CENTER);

        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(12);

        final LabelField label = LabelField.createBoldHeaderLabel("ImageTextUtil", 20, 0, 8);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(label);

        LabelField labelField = new LabelField("<html>The ImageTextUtil class gives you a way<br>" +
                                            "to write multiple lines of text to an image<br>" +
                                            "with optional fill and outline properties.<br>" +
                                            "Line wrapping can be handled automatically!</html>");
        labelField.getMargins().setBottom(10);
        formPanel.add(labelField);

        textField = LongTextField.ofFixedSizeMultiLine("Text:", 4, 16);
        textField.setText(text);
        textField.addValueChangedListener(changeListener);
        formPanel.add(textField);

        fontField = new FontField("Font:");
        fontField.addValueChangedListener(changeListener);
        fontField.setShowSizeField(false);
        formPanel.add(fontField);

        bgColorField = new ColorField("Background:", ColorSelectionType.GRADIENT).setGradient(bgGradient);
        bgColorField.addValueChangedListener(changeListener);
        formPanel.add(bgColorField);

        textFillColorField = new ColorField("Text fill:", ColorSelectionType.SOLID).setColor(fillColor);
        textFillColorField.addValueChangedListener(changeListener);
        formPanel.add(textFillColorField);

        textOutlineColorField = new ColorField("Text outline:", ColorSelectionType.SOLID).setColor(outlineColor);
        textOutlineColorField.addValueChangedListener(changeListener);
        formPanel.add(textOutlineColorField);

        List<String> options = new ArrayList<>();
        for (ImageTextUtil.TextAlign align : ImageTextUtil.TextAlign.values()) {
            options.add(align.toString());
        }
        textAlignField = new ComboField<>("Text align:",
                                          List.of(ImageTextUtil.TextAlign.values()), 4, false);
        textAlignField.addValueChangedListener(changeListener);
        formPanel.add(textAlignField);

        options.clear();
        options.add("None");
        options.add("Thin");
        options.add("Medium");
        options.add("Thick");
        outlineWidthField = new ComboField<>("Outline width:", options, 1, false);
        outlineWidthField.addValueChangedListener(changeListener);
        formPanel.add(outlineWidthField);

        lineWrapField = new NumberField("Line wrap:", 25, 5, 100, 1);
        lineWrapField.addValueChangedListener(changeListener);
        formPanel.add(lineWrapField);

        panel.add(formPanel, BorderLayout.WEST);

        render();

        return panel;
    }

    private void render() {
        Graphics2D graphics = image.createGraphics();
        if (bgGradient != null) {
            GradientUtil.fill(bgGradient, graphics, 0, 0, image.getWidth(), image.getHeight());
        }
        else {
            graphics.setColor(bgSolidColor);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        }
        graphics.dispose();

        if (text.isBlank()) {
            text = Version.NAME;
        }

        ImageTextUtil.drawText(image, text, lineWrapAt,
                               Properties.createFontFromAttributes(fontFamily, isBold, isItalic, 12), textAlign,
                               outlineColor, outlineWidth, fillColor);
        imagePanel.setImage(image);
    }

    private final ValueChangedListener changeListener = new ValueChangedListener() {
        @Override
        public void formFieldValueChanged(FormField field) {
            text = textField.getText();
            fontFamily = fontField.getSelectedFont().getFamily();
            isBold = fontField.getSelectedFont().isBold();
            isItalic = fontField.getSelectedFont().isItalic();
            Object something = bgColorField.getSelectedValue();
            if (something instanceof Color) {
                bgGradient = null;
                bgSolidColor = (Color)something;
            }
            else {
                bgSolidColor = null;
                bgGradient = (Gradient)something;
            }
            fillColor = textFillColorField.getColor();
            outlineColor = textOutlineColorField.getColor();
            //@formatter:off
            switch (outlineWidthField.getSelectedIndex()) {
                case 0: outlineWidth = 0; break;
                case 1: outlineWidth = 20; break;
                case 2: outlineWidth = 12; break;
                case 3: outlineWidth = 8; break;
            }
            //@formatter:on
            textAlign = textAlignField.getSelectedItem();
            lineWrapAt = (Integer)lineWrapField.getCurrentValue();
            render();
        }
    };
}
