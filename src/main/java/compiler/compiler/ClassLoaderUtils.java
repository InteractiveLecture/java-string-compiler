package compiler.compiler;


/*
 * Copyright (c) 2015 Rene Richter.
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class support loading and debugging Java Classes dynamically.
 */
public class ClassLoaderUtils {
  private static final Method DEFINE_CLASS_METHOD;
  private static final Method FIND_LOADED_CLASS_METHOD;

  static {
    try {
      DEFINE_CLASS_METHOD = ClassLoader.class.getDeclaredMethod(
          "defineClass", String.class, byte[].class, int.class, int.class);
      DEFINE_CLASS_METHOD.setAccessible(true);
      FIND_LOADED_CLASS_METHOD = ClassLoader.class.getDeclaredMethod(
          "findLoadedClass", String.class);
      FIND_LOADED_CLASS_METHOD.setAccessible(true);

    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }


  public static void defineClass(String className, byte[] bytes) {
    defineClass(
        Thread.currentThread().getContextClassLoader(), className, bytes);
  }


  static Class<?> defineClass(
      ClassLoader classLoader, String className, byte[] bytes) {
    try {
      return (Class<?>) DEFINE_CLASS_METHOD.invoke(
          classLoader, className, bytes, 0, bytes.length);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    } catch (InvocationTargetException e) {
      throw new AssertionError(e.getCause());
    }
  }

  static Class<?> findLoadedClass(String className, ClassLoader cl) {

    Class<?> clazz = null;
    try {
      clazz = (Class<?>) FIND_LOADED_CLASS_METHOD.invoke(cl, className);
    } catch (IllegalAccessException
        | IllegalArgumentException | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }
    return clazz;
  }

}
