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
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * A demo panel for the CustomizableDesktopPane, showing off color gradient backgrounds
 * and optional placement of a logo image on the desktop background.
 */
public class DesktopDemoPanel extends PanelBuilder {

    private static final Gradient INITIAL_GRADIENT = new Gradient(GradientType.STAR, Color.BLACK, Color.BLUE);
    private final CustomizableDesktopPane desktopPane;

    public DesktopDemoPanel() {
        LogoProperty logoProperty = new LogoProperty("demo");
        logoProperty.setLogoHeight(80);
        BufferedImage logoImage = LogoGenerator.generateImage("Logo", logoProperty);
        desktopPane = new CustomizableDesktopPane(logoImage,
                                                  CustomizableDesktopPane.LogoPlacement.BOTTOM_RIGHT,
                                                  0.5f,
                                                  INITIAL_GRADIENT);
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
        formPanel.setBorderMargin(16);

        String sb = "<html>If your application uses JDesktopPane, you may be frustrated with the lack of<br/>" +
                "options for customization.  Meet the CustomizableDesktopPane!</html>";
        LabelField labelField = new LabelField(sb);
        labelField.getMargins().setTop(14).setBottom(18);
        formPanel.add(labelField);

        // We can set up a ColorField for allowing either solid-color backgrounds or gradient backgrounds.
        final ColorField bgColorField = new ColorField("Background:", ColorSelectionType.EITHER)
                .setColor(Color.BLACK)
                .setGradient(INITIAL_GRADIENT);
        bgColorField.addValueChangedListener(field -> {
            Object val = bgColorField.getSelectedValue();
            if (val instanceof Color) {
                desktopPane.setBgSolidColor((Color)val);
            }
            else {
                desktopPane.setGradientConfig((Gradient)val);
            }
        });
        formPanel.add(bgColorField);

        // If a logo image is to be displayed, you can control its transparency:
        NumberField alphaField = new NumberField("Logo alpha:", 0.5, 0.0, 1.0, 0.1);
        alphaField.addValueChangedListener(field -> {
            desktopPane.setLogoImageTransparency(alphaField.getCurrentValue().floatValue());
        });
        formPanel.add(alphaField);

        // We can easily create a combo field to represent the placement options.
        // This is because ComboField can be driven directly from an enum, as long as sensible toString() is provided.
        ComboField<CustomizableDesktopPane.LogoPlacement> placementCombo;
        placementCombo = new ComboField<>("Logo placement:",
                                          List.of(CustomizableDesktopPane.LogoPlacement.values()),
                                          4,
                                          false);
        placementCombo.addValueChangedListener(field -> {
            desktopPane.setLogoImagePlacement(placementCombo.getSelectedItem());
        });
        formPanel.add(placementCombo);

        // Let's add a button that lets the user put a dummy internal frame into the desktop:
        PanelField buttonWrapper = new PanelField();
        buttonWrapper.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("Add frame");
        button.addActionListener(e -> addFrame());
        buttonWrapper.getPanel().add(button);
        buttonWrapper.getMargins().setAll(0).setLeft(24).setTop(8);
        formPanel.add(buttonWrapper);

        container.add(formPanel, BorderLayout.SOUTH);
        container.add(desktopPane, BorderLayout.CENTER);
        return container;
    }

    /**
     * Invoked internally to add a dummy internal frame to our sample desktop
     * so you can get a feel for how it will look and behave in an actual application.
     */
    private void addFrame() {
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
}
