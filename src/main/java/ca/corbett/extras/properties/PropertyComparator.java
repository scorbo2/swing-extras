package ca.corbett.extras.properties;

import java.util.Comparator;

/**
 * Can be used for sorting of AbstractProperty instances by their fully qualified names.
 *
 * @author scorbett
 * @since sc-util 1.8
 */
public class PropertyComparator implements Comparator<AbstractProperty> {

    @Override
    public int compare(AbstractProperty o1, AbstractProperty o2) {
        return o1.fullyQualifiedName.compareTo(o2.fullyQualifiedName);
    }

}
