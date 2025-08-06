/**
 * All FormFields support optional validation in the form of a FieldValidator.
 * Each FormField can have zero or more FieldValidator instances assigned to it.
 * These validators will be invoked on the field when the FormPanel is validated.
 * A validation success or failure label will appear next to the FormField automatically,
 * with helpful tooltip text as needed.
 * <p>
 * You can, of course, provide your own FieldValidator implementation, if you have
 * specific business logic or validation rules that you need to use.
 * <p>
 * If multiple FieldValidators are assigned to a FormField, they will each be invoked,
 * and all of them must return a successful validation in order for the field to
 * be considered valid.
 */
package ca.corbett.forms.validators;
