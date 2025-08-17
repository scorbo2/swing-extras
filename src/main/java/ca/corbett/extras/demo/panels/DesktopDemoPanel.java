package ca.corbett.extras.demo.panels;

import ca.corbett.extras.CustomizableDesktopPane;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.extras.image.LogoProperty;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DesktopDemoPanel extends PanelBuilder {

    private Gradient gradient;
    private CustomizableDesktopPane desktopPane;

    public DesktopDemoPanel() {
        gradient = new Gradient(GradientType.STAR, Color.BLACK, Color.BLUE);

        LogoProperty logoProperty = new LogoProperty("demo");
        logoProperty.setLogoHeight(80);
        BufferedImage logoImage = LogoGenerator.generateImage("Logo", logoProperty);
        desktopPane = new CustomizableDesktopPane(logoImage, CustomizableDesktopPane.LogoPlacement.BOTTOM_RIGHT, 0.5f,
                                                  gradient);
    }

    @Override
    public String getTitle() {
        return "DesktopPane";
    }

    @Override
    public JPanel build() {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        StringBuilder sb = new StringBuilder();
        sb.append("<html>If your application uses JDesktopPane, you may be frustrated with the lack of<br/>");
        sb.append("options for customization.  Meet the CustomizableDesktopPane!</html>");
        LabelField labelField = new LabelField(sb.toString());
        labelField.getMargins().setTop(14).setBottom(18);
        formPanel.add(labelField);

        final ColorField bgColorField = new ColorField("Background:", ColorSelectionType.EITHER).setGradient(gradient)
                                                                                                .setColor(Color.BLACK);
        bgColorField.addValueChangedListener(field -> {
            Object val = bgColorField.getSelectedValue();
            if (val instanceof Color) {
                //TODO wtf is this trying to do gradient.setColor1((Color)val);
                //TODO and this gradient.setColor2((Color)val);
            }
            else {
                gradient = (Gradient)val;
            }
            desktopPane.setGradientConfig(gradient);
        });
        formPanel.add(bgColorField);

        NumberField alphaField = new NumberField("Logo alpha:", 0.5, 0.0, 1.0, 0.1);
        alphaField.addValueChangedListener(field -> {
            desktopPane.setLogoImageTransparency(alphaField.getCurrentValue().floatValue());
        });
        formPanel.add(alphaField);

        List<String> options = new ArrayList<>();
        for (CustomizableDesktopPane.LogoPlacement placement : CustomizableDesktopPane.LogoPlacement.values()) {
            options.add(placement.toString());
        }
        ComboField<String> placementCombo = new ComboField<>("Logo placement:", options, 4, false);
        placementCombo.addValueChangedListener(field -> {
            desktopPane.setLogoImagePlacement(
                    CustomizableDesktopPane.LogoPlacement.fromLabel(placementCombo.getSelectedItem()));
        });
        formPanel.add(placementCombo);

        PanelField buttonWrapper = new PanelField();
        buttonWrapper.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("Add frame");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JInternalFrame frame = new JInternalFrame("Example frame", true, true, true, true) {
                    @Override
                    public void setVisible(boolean v) {
                        setSize(new Dimension(250, 100));
                        setLocation(100, 100);
                        setMinimumSize(new Dimension(250, 100));
                        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
                        setResizable(true);
                        super.setVisible(v);
                    }

                };
                desktopPane.add(frame);
                frame.setVisible(true);
            }

        });
        buttonWrapper.getPanel().add(button);
        buttonWrapper.getMargins().setAll(0).setLeft(24).setTop(8);
        formPanel.add(buttonWrapper);

        container.add(formPanel, BorderLayout.SOUTH);
        container.add(desktopPane, BorderLayout.CENTER);
        return container;
    }
}
