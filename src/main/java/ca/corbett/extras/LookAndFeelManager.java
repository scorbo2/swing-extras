package ca.corbett.extras;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoDarkFuchsiaIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGrayIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatMonocaiIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTAtomOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTAtomOneLightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTDraculaIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTGitHubDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTGitHubIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTLightOwlIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialDarkerIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialDeepOceanIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialOceanicIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialPalenightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMonokaiProIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMoonlightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTNightOwlIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTSolarizedLightIJTheme;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.aero.AeroLookAndFeel;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import com.jtattoo.plaf.bernstein.BernsteinLookAndFeel;
import com.jtattoo.plaf.fast.FastLookAndFeel;
import com.jtattoo.plaf.graphite.GraphiteLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.luna.LunaLookAndFeel;
import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
import com.jtattoo.plaf.mint.MintLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;
import com.jtattoo.plaf.smart.SmartLookAndFeel;
import com.jtattoo.plaf.texture.TextureLookAndFeel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.UIManager.installLookAndFeel;

/**
 * This is a simple wrapper class to manage the various look and feels that
 * spring-extras supports.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2025-04-22
 */
public class LookAndFeelManager {

    private static final Logger logger = Logger.getLogger(LookAndFeelManager.class.getName());

    private static final List<ChangeListener> changeListeners = new ArrayList<>();

    /**
     * Can be invoked (ideally at application startup before any GUI elements are shown)
     * to install all the "extra" LaFs that swing-extras supports, namely from the
     * FlatLaf package and also JTattoo. With the JTattoo themes we have to tweak
     * them a bit to avoid some wonky default behaviour with menus (it wants to show
     * a sideways logo in every menu for some reason - we can disable it).
     * <p>
     *     Implementation note: this code is in a public static method instead of
     *     in a static initializer block because your app might not care about
     *     LaF, in which case you can just ignore this method and avoid whatever
     *     memory penalty in would otherwise incur.
     * </p>
     */
    public static void installExtraLafs() {
        // The "core" themes provided by FlatLaf:
        FlatLightLaf.installLafInfo();
        FlatDarkLaf.installLafInfo();

        // The FlatLaf themes
        // (full list here: https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-intellij-themes#how-to-use)
        FlatArcIJTheme.installLafInfo();
        FlatArcOrangeIJTheme.installLafInfo();
        FlatArcDarkIJTheme.installLafInfo();
        FlatArcDarkOrangeIJTheme.installLafInfo();
        FlatCarbonIJTheme.installLafInfo();
        FlatCobalt2IJTheme.installLafInfo();
        FlatCyanLightIJTheme.installLafInfo();
        FlatDarkFlatIJTheme.installLafInfo();
        FlatDarkPurpleIJTheme.installLafInfo();
        FlatDraculaIJTheme.installLafInfo();
        FlatGradiantoDarkFuchsiaIJTheme.installLafInfo();
        FlatGradiantoDeepOceanIJTheme.installLafInfo();
        FlatGradiantoMidnightBlueIJTheme.installLafInfo();
        FlatGradiantoNatureGreenIJTheme.installLafInfo();
        FlatGrayIJTheme.installLafInfo();
        FlatGruvboxDarkHardIJTheme.installLafInfo();
        FlatHiberbeeDarkIJTheme.installLafInfo();
        FlatHighContrastIJTheme.installLafInfo();
        FlatLightFlatIJTheme.installLafInfo();
        FlatMaterialDesignDarkIJTheme.installLafInfo();
        FlatMonocaiIJTheme.installLafInfo();
        FlatMonokaiProIJTheme.installLafInfo();
        FlatNordIJTheme.installLafInfo();
        FlatOneDarkIJTheme.installLafInfo();
        FlatSolarizedDarkIJTheme.installLafInfo();
        FlatSolarizedLightIJTheme.installLafInfo();
        FlatSpacegrayIJTheme.installLafInfo();
        FlatVuesionIJTheme.installLafInfo();
        FlatXcodeDarkIJTheme.installLafInfo();
        FlatMTArcDarkIJTheme.installLafInfo();
        FlatMTAtomOneDarkIJTheme.installLafInfo();
        FlatMTAtomOneLightIJTheme.installLafInfo();
        FlatMTDraculaIJTheme.installLafInfo();
        FlatMTGitHubIJTheme.installLafInfo();
        FlatMTGitHubDarkIJTheme.installLafInfo();
        FlatMTLightOwlIJTheme.installLafInfo();
        FlatMTMaterialDarkerIJTheme.installLafInfo();
        FlatMTMaterialDeepOceanIJTheme.installLafInfo();
        FlatMTMaterialLighterIJTheme.installLafInfo();
        FlatMTMaterialOceanicIJTheme.installLafInfo();
        FlatMTMaterialPalenightIJTheme.installLafInfo();
        FlatMTMonokaiProIJTheme.installLafInfo();
        FlatMTMoonlightIJTheme.installLafInfo();
        FlatMTNightOwlIJTheme.installLafInfo();
        FlatMTSolarizedDarkIJTheme.installLafInfo();
        FlatMTSolarizedLightIJTheme.installLafInfo();

        // JTattoo:
        Properties props = new Properties();
        props.put("logoString", "");
        AcrylLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Acryl", AcrylLookAndFeel.class.getName());
        AeroLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Aero", AeroLookAndFeel.class.getName());
        AluminiumLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Aluminium", AluminiumLookAndFeel.class.getName());
        BernsteinLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Bernstein", BernsteinLookAndFeel.class.getName());
        FastLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Fast", FastLookAndFeel.class.getName());
        GraphiteLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Graphite", GraphiteLookAndFeel.class.getName());
        HiFiLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - HiFi", HiFiLookAndFeel.class.getName());
        LunaLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Luna", LunaLookAndFeel.class.getName());
        McWinLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - McWin", McWinLookAndFeel.class.getName());
        MintLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Mint", MintLookAndFeel.class.getName());
        NoireLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Noire", NoireLookAndFeel.class.getName());
        SmartLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Smart", SmartLookAndFeel.class.getName());
        TextureLookAndFeel.setCurrentTheme(props);
        installLookAndFeel("JTattoo - Texture", TextureLookAndFeel.class.getName());
    }

    /**
     * Invoke to switch the current LaF to the specified one (you can either get the
     * LookAndFeelInfo from a LookAndFeelProperty via user input or you can specify
     * it yourself).
     */
    public static void switchLaf(UIManager.LookAndFeelInfo info) {
        switchLaf(info.getClassName());
    }

    /**
     * Given a class name for a LookAndFeel class, switch the current LaF to that one.
     * We make no check here to ensure that the given class actually exists.
     *
     * @param className The name of the LaF class in question.
     */
    public static void switchLaf(String className) {
        try {
            UIManager.setLookAndFeel(className);
            for (Window w : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w);
            }
            fireChangedEvent();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to change look and feel!", e);
        }
    }

    /**
     * Returns the Color for the specified key, or returns the defaultColor if the
     * specified key returns nothing.
     */
    public static Color getLafColor(String key, Color defaultColor) {
        Color c = UIManager.getColor(key);
        return c == null ? defaultColor : c;
    }

    /**
     * If you wish to be informed when the current Look and Feel changes, because
     * you have perhaps set some custom colors across your UI, you can subscribe
     * to receive notification when it happens.
     * <p>
     * Note: you'll only receive a ChangeEvent if the Look and Feel was switched
     * via this class. If your code talks to UIManager directly, then this
     * class is cut out of the loop and the ChangeEvent will not fire.
     * </p>
     *
     * @param listener A ChangeListener to receive a change event when LaF changes.
     */
    public static void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public static void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    private static void fireChangedEvent() {
        ChangeEvent event = new ChangeEvent("LookAndFeelManager");
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(event);
        }
    }
}
