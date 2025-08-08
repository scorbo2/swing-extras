/**
 * <h2>ca.corbett.forms.fields</h2>
 * This package contains an abstract FormField class, along with some basic
 * implementations. The FormField class itself is designed with extensibility
 * in mind, so if there's a particular form field you need that isn't represented
 * by one of these provided implementation classes, you can easily build your
 * own by extending the FormField class. Use any of these provided implementation
 * classes as a template to guide you in this process.
 *
 * <h3>Responding to field changes</h3>
 * You can use FormField.addValueChangedListener() to register a listener which
 * will be notified whenever the value in that FormField changes. This can allow
 * you to make changes elsewhere on the form depending on what value is present
 * in a given FormField. For example, field B should only be visible if
 * field A has a certain value.
 *
 * <h3>Validating FormFields</h3>
 * You can use FormField.addFieldValidator() to register a FieldValidator with
 * a given FormField. This validator will be invoked when the form itself is
 * validated, and the field will automatically show a validation success
 * or failure label with a helpful tooltip.
 *
 * <h3>Making cosmetic adjustments</h3>
 * You have options for changing the look of your forms. Each FormField has
 * a Margins instance that can be used to offset or indent a FormField.
 * Use FormField.getFieldLabel().setFont() to change the color or size
 * or font face of the field label. All FormField implementing classes
 * also expose the wrapped Java Swing UI component via getFieldComponent(),
 * so you can apply custom styling to the individual form fields as well.
 *
 * @author scorbo2
 */
package ca.corbett.forms.fields;
