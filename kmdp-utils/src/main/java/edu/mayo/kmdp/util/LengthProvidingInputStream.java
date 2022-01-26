/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A decorating <code>InputStream</code> that is able to reliably provide the length of the stream.
 * Other
 * <code>InputStream</code>s are not able to reliably provide length without reading from the
 * stream which is problematic because reading is a one-time operation.
 */
public class LengthProvidingInputStream extends InputStream {

  protected long length;
  protected InputStream inputStream;

  public LengthProvidingInputStream(InputStream inputStream, long length) {
    this.inputStream = inputStream;
    this.length = length;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public long getLength() {
    return length;
  }

  @Override
  public int read() throws IOException {
    return inputStream.read();
  }

}
