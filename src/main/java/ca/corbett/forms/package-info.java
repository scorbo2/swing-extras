/**
 * <H2>Welcome to swing-forms!</H2>
 * The swing-forms library provides a layer of abstraction on top of
 * Java Swing components, and in particular eliminates the need to
 * worry about a LayoutManager for forms-like interfaces - that is,
 * presenting a group of related input fields to the user to allow
 * for viewing and modification.
 *
 * <h3>Online documentation</h3>
 * In addition to the javadocs that you are reading right now,
 * the swing-forms library is also well documented in the
 * <a href="https://www.corbett.ca/swing-extras-book/">swing-extras book</a>,
 * which is available online.
 *
 * <h3>Quick guide to the code</h3>
 * Here is a very quick overview of the code:
 * <ul>
 *     <li>The FormPanel class wraps a collection of FormField instances
 *         and gives you an easy way to display them, allow user input,
 *         and provide form validation before accepting the new values.
 *     <li>The FormField class provides an abstract starting point for
 *         the various FormField implementations. Generally speaking,
 *         each FormField implementing class wraps a different Java
 *         Swing UI component.
 *     <li>The FieldValidator interface provides a starting point
 *         for form field validation. A few FieldValidator implementations
 *         are included out of the box for some basic validation, or you
 *         can very easily build your own FieldValidator implementation.
 * </ul>
 *
 * <h3>Extending the code</h3>
 * The swing-forms library is designed with extensibility in mind! If you
 * need a new type of FormField, you can either create a PanelField and
 * add whatever components you need into it, or you can extend FormField
 * and write your own custom FormField. If you need custom validation rules
 * for your input form or for a particular FormField, you can easily
 * extend FieldValidator to write whatever validation rules you need.
 *
 * <h3>Additional help</h3>
 * You can fire up the included swing-extras demo app for a quick walk-through
 * of some of the capabilities of swing-forms.
 * <p>
 * You can refer to the <a href="https://www.corbett.ca/swing-extras-book/">swing-extras book</a>
 * for additional documentation and examples.
 * <p>
 * You can visit the <a href="https://github.com/scorbo2/swing-extras/issues">project issues page</a>
 * on GitHub to view the existing issues or to post a question.
 *
 * <h2>Have fun!</h2>
 * Say goodbye forever to writing niggly GridBagLayout code, and enjoy
 * bringing your forms to the screen quickly and painlessly!
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
package ca.corbett.forms;
