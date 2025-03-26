package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;

public class EnumProperty<T extends Enum<?>> extends AbstractProperty {

    T someEnum;

    public EnumProperty(String name, String label) {
        super(name, label);
        someEnum.getDeclaringClass().getEnumConstants()[0].;
    }

    @Override
    public void saveToProps(Properties props) {

    }

    @Override
    public void loadFromProps(Properties props) {

    }

    @Override
    public FormField generateFormField() {
        return null;
    }

    @Override
    public void loadFromFormField(FormField field) {

    }
}
