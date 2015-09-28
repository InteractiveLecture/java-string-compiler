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

import com.sun.tools.javac.api.JavacTool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

public class StringCompiler {

  private final List<JavaFileObject> javaFileObjects = new ArrayList<>();

  /**
   * Compiles all sources previously added as compilation tasks.
   * @param cl the classloader to use.
   * @return a CompilationResult object.
   */
  public CompilationResult startCompilation(ClassLoader cl) {
    CompilationResult result = new CompilationResult(cl);
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      compiler = JavacTool.create();
    }
    DiagnosticCollector<JavaFileObject> diagnostics =
        new DiagnosticCollector<>();

    JavaFileManagerProxy jfm = new JavaFileManagerProxy(
        compiler.getStandardFileManager(diagnostics, null, null));

    compiler.getTask(null, jfm, diagnostics, null, null, javaFileObjects).call();
    diagnostics.getDiagnostics().stream().forEach(result::addDiagnostic);
    try {
      jfm.close();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    javaFileObjects.clear();

    jfm.getCompiledBytes().forEach((classname, bytes) -> {
        if (bytes != null && bytes.length > 0) {
          result.getCompiledClasses().put(
              classname, this.defineClass(result.getClassLoader(), classname, bytes));
        }
      });

    return result;
  }

  public CompilationResult startCompilation() {
    return startCompilation(new MockClassLoader());
  }


  public void addCompilationTask(String className, String sourceCode) {
    javaFileObjects.add(new StringJavaFileObject(className, sourceCode));
  }

  private Class<?> defineClass(ClassLoader classLoader, String className, byte[] bytes) {
    Class<?> result = null;
    try {
      Method method = ClassLoader.class.getDeclaredMethod(
          "defineClass", String.class, byte[].class, int.class, int.class);
      method.setAccessible(true);
      result = (Class<?>) method.invoke(classLoader, className, bytes, 0, bytes.length);
    } catch (IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new AssertionError(e);
    }
    return result;
  }


}
