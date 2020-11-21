//Copyright 2007 Ember Corporation. All rights reserved.

package com.silabs.pti.util;

import java.util.function.Consumer;

/**
 * Simple listener that reports a selected object. Used by many classes
 * that require the simplest of listeners.
 *
 * This has been coped from IObjectListener so that it can extend
 * a standard java consumer interface, which is useful when using
 * general java functions that work with consumers.
 *
 * And IObjectListener in reality was just a simple consumer, which
 * predated java Consumer interface.
 *
 * Created on Sep 17, 2007
 * @author Timotej (timotej@ember.com)
 * @since 4.13
 */
@FunctionalInterface
public interface IObjectConsumer<T> extends Consumer<T> {
}
