/**
 * This package contains the abstract FieldValidator class, along with some example
 * implementations that can be easily attached to FormFields to provide basic validation.
 * <p>
 * To use one of the existing example validators in this package, you simply create
 * an instance of it and give it to your FormField instance:
 * </p>
 * <BLOCKQUOTE><PRE>myField.addFieldValidator(new YMDDateValidator(myField));</PRE></BLOCKQUOTE>
 * <p>
 * To create your own FieldValidator, you must extend the abstract FieldValidator
 * class and implement the validate() method with your logic.
 * </p>
 */
package ca.corbett.forms.validators;
