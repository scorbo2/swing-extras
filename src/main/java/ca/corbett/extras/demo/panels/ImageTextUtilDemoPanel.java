package ca.corbett.extras.demo.panels;

import ca.corbett.extras.Version;
import ca.corbett.extras.gradient.GradientColorField;
import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageTextUtil;
import ca.corbett.extras.properties.Properties;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.TextField;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A quick demo panel to show off the capabilities of ImageTextUtil.
 *
 * @author scorbo2
 * @since 2025-03-15
 */
public class ImageTextUtilDemoPanel extends PanelBuilder {
    private final BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
    private ImagePanel imagePanel;

    private TextField textField;
    private FontField fontField;
    private GradientColorField bgColorField;
    private ColorField textFillColorField;
    private ColorField textOutlineColorField;
    private ComboField outlineWidthField;
    private ComboField textAlignField;
    private NumberField lineWrapField;

    private String text;
    private String fontFamily;
    private boolean isBold;
    private boolean isItalic;
    private GradientConfig bgGradient;
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
        bgGradient = new GradientConfig();
        bgGradient.setColor1(Color.BLUE);
        bgGradient.setColor2(Color.BLACK);
        bgGradient.setGradientType(GradientUtil.GradientType.HORIZONTAL_STRIPE);
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

        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);

        LabelField labelField = new LabelField("ImageTextUtil");
        labelField.setTopMargin(18);
        labelField.setFont(labelField.getFieldLabelFont().deriveFont(Font.BOLD, 18f));
        formPanel.addFormField(labelField);

        labelField = new LabelField("<html>The ImageTextUtil class gives you a way<br>" +
                "to write multiple lines of text to an image<br>" +
                "with optional fill and outline properties.<br>" +
                "Line wrapping can be handled automatically!</html>");
        labelField.setBottomMargin(10);
        labelField.setFont(labelField.getFieldLabelFont().deriveFont(Font.PLAIN, 12f));
        formPanel.addFormField(labelField);

        textField = new TextField("Text:", 22, 4, true);
        textField.setText(text);
        textField.addValueChangedAction(changeAction);
        formPanel.addFormField(textField);

        fontField = new FontField("Font:");
        fontField.addValueChangedAction(changeAction);
        formPanel.addFormField(fontField);

        bgColorField = new GradientColorField("Background:", bgGradient);
        bgColorField.addValueChangedAction(changeAction);
        formPanel.addFormField(bgColorField);

        textFillColorField = new ColorField("Text fill:", fillColor);
        textFillColorField.addValueChangedAction(changeAction);
        formPanel.addFormField(textFillColorField);

        textOutlineColorField = new ColorField("Text outline:", outlineColor);
        textOutlineColorField.addValueChangedAction(changeAction);
        formPanel.addFormField(textOutlineColorField);

        List<String> options = new ArrayList<>();
        for (ImageTextUtil.TextAlign align : ImageTextUtil.TextAlign.values()) {
            options.add(align.toString());
        }
        textAlignField = new ComboField("Text align:", options, 4, false);
        textAlignField.addValueChangedAction(changeAction);
        formPanel.addFormField(textAlignField);

        options.clear();
        options.add("None");
        options.add("Thin");
        options.add("Medium");
        options.add("Thick");
        outlineWidthField = new ComboField("Outline width:", options, 1, false);
        outlineWidthField.addValueChangedAction(changeAction);
        formPanel.addFormField(outlineWidthField);

        lineWrapField = new NumberField("Line wrap:", 25, 5, 100, 1);
        lineWrapField.addValueChangedAction(changeAction);
        formPanel.addFormField(lineWrapField);

        formPanel.render();
        panel.add(formPanel, BorderLayout.WEST);

        render();

        return panel;
    }

    private void render() {
        Graphics2D graphics = image.createGraphics();
        if (bgGradient != null) {
            GradientUtil.fill(bgGradient, graphics, 0, 0, image.getWidth(), image.getHeight());
        } else {
            graphics.setColor(bgSolidColor);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        }
        graphics.dispose();

        if (text.isBlank()) {
            text = Version.NAME;
        }

        ImageTextUtil.drawText(image, text, lineWrapAt, Properties.createFontFromAttributes(fontFamily, isBold, isItalic, 12), textAlign, outlineColor, outlineWidth, fillColor);
        imagePanel.setImage(image);
    }

    private final AbstractAction changeAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            text = textField.getText();
            fontFamily = fontField.getSelectedFont().getFamily();
            isBold = fontField.getSelectedFont().isBold();
            isItalic = fontField.getSelectedFont().isItalic();
            Object something = bgColorField.getSelectedValue();
            if (something instanceof Color) {
                bgGradient = null;
                bgSolidColor = (Color) something;
            } else {
                bgSolidColor = null;
                bgGradient = (GradientConfig) something;
            }
            fillColor = textFillColorField.getColor();
            outlineColor = textOutlineColorField.getColor();
            switch (outlineWidthField.getSelectedIndex()) {
                case 0:
                    outlineWidth = 0;
                    break;
                case 1:
                    outlineWidth = 20;
                    break;
                case 2:
                    outlineWidth = 12;
                    break;
                case 3:
                    outlineWidth = 8;
                    break;
            }
            textAlign = ImageTextUtil.TextAlign.fromLabel(textAlignField.getSelectedItem());
            lineWrapAt = (Integer) lineWrapField.getCurrentValue();
            render();
        }
    };
}
