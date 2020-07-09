/*
 * Copyright (c) 2019 Frugal Mechanic (http://frugalmechanic.com)
 * Copyright (c) 2020 the Scala FasterXML/woodstox XML Streaming Library contributors.
 * See the project homepage at: https://er1c.github.io/scala-woodstox/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package woodstox

private final class XmlReaderPathStack(maxSize: Int) extends IndexedSeq[String] {
  private[this] var depth: Int = 0
  private[this] val array = new Array[String](maxSize)

  def length: Int = depth

  def pushElement(elementName: String): Unit = {
    if (depth < maxSize) array(depth) = elementName
    depth += 1
  }

  def pop(): Unit = {
    if (depth > 0) {
      depth -= 1
      if (depth < maxSize) array(depth) = null
    }
  }

  def apply(idx: Int): String = {
    if (idx >= depth) throw new ArrayIndexOutOfBoundsException("Depth is " + depth + " but requested index is " + idx)
    else if (idx >= maxSize) null // Arbitrarily chosen
    else array(idx)
  }
}
