
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

/**
 * Represents the result of a compilation.
 * @author Rene Richter
 */
public class CompilationResult {
  private Map<String, Class<?>> compiledClasses;
  private Map<Kind, List<Diagnostic>> diagnostics;
  private ClassLoader cl;

  CompilationResult(ClassLoader cl) {
    this.diagnostics = new HashMap<>();
    this.compiledClasses = new HashMap<>();
    this.cl = cl;
  }

  void addDiagnostic(Diagnostic diagnostic) {
    Kind kind = diagnostic.getKind();
    if (this.diagnostics.containsKey(kind)) {
      this.diagnostics.get(kind).add(diagnostic);
    } else {
      List<Diagnostic> list = new ArrayList<>();
      list.add(diagnostic);
      this.diagnostics.put(kind, list);
    }
  }


  /**
   * Convenience method for checking if compilation encountered warnings.
   *
   * @return true if warnings are present.
   */
  public boolean hasWarnings() {
    return this.diagnostics.containsKey(Kind.WARNING);
  }


  /**
   * Convenience method for checking if compilation encountered errors.
   *
   * @return true if compilationerrors are present.
   */
  public boolean hasErrors() {
    return this.diagnostics.containsKey(Kind.ERROR);
  }

  public ClassLoader getClassLoader() {
    return cl;
  }


  public Map<String, Class<?>> getCompiledClasses() {
    return compiledClasses;
  }

  public List<Diagnostic> getDiagnostics(Kind kind) {
    return this.diagnostics.get(kind);
  }

  public Map<Kind, List<Diagnostic>> getDiagnostics() {
    return diagnostics;
  }


  public List<Diagnostic> getErrors() {
    return getDiagnostics(Kind.ERROR);
  }

  public List<Diagnostic> getWarnings() {
    return getDiagnostics(Kind.WARNING);
  }



}
