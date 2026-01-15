/**
 * This package contains classes and utilities for working with progress bars, and with
 * long-running tasks that require progress bars. There are two main options here:
 * <ul>
 *     <li> The {@link ca.corbett.extras.progress.MultiProgressDialog} class, which provides a simple way
 *     to automatically show a progress bar during a long-running task. If your operation has a single
 *     list of steps, you can extend the {@link ca.corbett.extras.progress.SimpleProgressWorker} to implement
 *     your long-running task with minimal boilerplate code. Simply fire off progress event methods as the
 *     work proceeds, and the single progress bar is updated automatically. Your operation may instead have compound
 *     operations. That is, a list of major steps, each one of which has a list of minor steps. For example,
 *     traversing over a directory structure (major steps) and processing each file in each directory (minor steps).
 *     In this case you can extend the {@link ca.corbett.extras.progress.MultiProgressWorker} class instead,
 *     which provides more options for firing progress events to update the two progress bars.</li>
 *     <li> The {@link ca.corbett.extras.progress.SplashProgressWindow} class is intended to provide a simple
 *     splash screen with a built-in progress bar, if your application has a length operation to perform
 *     at startup. You can again extend the {@link ca.corbett.extras.progress.SimpleProgressWorker} class to implement
 *     your startup task, and the splash screen will be displayed automatically while the work is performed.
 *     </li>
 * </ul>
 * <p>
 *     The listener interfaces in this package are also helpful if you want to implement your own progress
 *     tracking, or if you just want to be notified when the operation in question begins and ends.
 *     Adapter classes are provided for each listener interface, so you can simply override the methods you care about
 *     without needing to implement every method in the interface.
 * </p>
 */
package ca.corbett.extras.progress;
