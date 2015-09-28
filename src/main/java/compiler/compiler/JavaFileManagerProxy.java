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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

class JavaFileManagerProxy implements JavaFileManager {
  private final StandardJavaFileManager fileManager;
  private final Map<String, ByteArrayOutputStream> buffers = new LinkedHashMap<>();

  JavaFileManagerProxy(StandardJavaFileManager fileManager) {
    this.fileManager = fileManager;
  }

  @Override
  public ClassLoader getClassLoader(Location location) {
    return fileManager.getClassLoader(location);
  }

  @Override
  public Iterable<JavaFileObject> list(Location location,
                                       String packageName,
                                       Set<Kind> kinds,
                                       boolean recurse) throws IOException {
    return fileManager.list(location, packageName, kinds, recurse);
  }

  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    return fileManager.inferBinaryName(location, file);
  }

  @Override
  public boolean isSameFile(FileObject fileObject, FileObject otherFileObject) {
    return fileManager.isSameFile(fileObject, otherFileObject);
  }

  @Override
  public boolean handleOption(String current, Iterator<String> remaining) {
    return fileManager.handleOption(current, remaining);
  }

  @Override
  public boolean hasLocation(Location location) {
    return fileManager.hasLocation(location);
  }

  @Override
  public JavaFileObject getJavaFileForInput(Location location,
                                            String className,
                                            Kind kind) throws IOException {
    if (location == StandardLocation.CLASS_OUTPUT
        && buffers.containsKey(className) && kind == Kind.CLASS) {
      final byte[] bytes = buffers.get(className).toByteArray();
      return new SimpleJavaFileObject(URI.create(className), kind) {

        @Override
        public InputStream openInputStream() {
          return new ByteArrayInputStream(bytes);
        }
      };
    }
    return fileManager.getJavaFileForInput(location, className, kind);
  }


  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location, final String className,
      Kind kind, FileObject sibling) throws IOException {

    return new SimpleJavaFileObject(URI.create(className), kind) {

      @Override
      public OutputStream openOutputStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        buffers.put(className, baos);
        return baos;
      }
    };
  }

  @Override
  public FileObject getFileForInput(
      Location location, String packageName,
      String relativeName) throws IOException {

    return fileManager.getFileForInput(location, packageName, relativeName);
  }

  @Override
  public FileObject getFileForOutput(
      Location location, String packageName,
      String relativeName, FileObject sibling) throws IOException {

    return fileManager.getFileForOutput(location, packageName, relativeName, sibling);
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
    fileManager.close();
  }

  @Override
  public int isSupportedOption(String option) {
    return fileManager.isSupportedOption(option);
  }

  public void clearBuffers() {
    buffers.clear();
  }


  public Map<String, byte[]> getCompiledBytes() {
    Map<String, byte[]> ret = new LinkedHashMap<>(buffers.size() * 2);
    buffers.entrySet().stream().forEach((entry) -> {
        ret.put(entry.getKey(), entry.getValue().toByteArray());
      });
    buffers.clear();
    return ret;
  }
}
