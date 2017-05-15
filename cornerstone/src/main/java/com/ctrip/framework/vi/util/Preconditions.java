package com.ctrip.framework.cornerstone.util;

/**
 * Created by jiang.j on 2016/4/7.
 */

/**
 * Internal convenience methods that help a method or constructor check whether it was invoked
 * correctly. Please notice that this should be considered an internal implementation detail, and
 * it is subject to change without notice.
 */
public final class Preconditions {
  private Preconditions() {
  }

  /**
   * Ensures the object reference is not null.
   */
  public static <T> T checkNotNull(T obj, String name) {
    if (obj == null) {
      String msg = String.format("parameter '%s' cannot be null", name);
      throw new NullPointerException(msg);
    }
    return obj;
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the
   * calling method.
   *
   * @param expression a boolean expression
   * @throws IllegalArgumentException if {@code expression} is false
   */
  public static void checkArgument(
      boolean expression, String errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(errorMessage);
    }
  }
}
