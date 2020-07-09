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

import fm.common.FileUtil
import java.io.{File, OutputStream}
import scala.reflect.{classTag, ClassTag}

final case class XmlWriterFactory[T: ClassTag](
  rootName: String,
  defaultNamespaceURI: String = "",
  overrideDefaultNamespaceURI: String = "") {
  private[this] val classes: Seq[Class[_]] = Seq(classTag[T].runtimeClass)

  def write(f: File)(fun: XmlWriter => Unit): Unit = {
    FileUtil.writeFile(f, true) { os => write(os)(fun) }
  }

  def write(os: OutputStream)(fun: XmlWriter => Unit): Unit = {
    val writer = new XmlWriter(classes, rootName, defaultNamespaceURI, os)
    fun(writer)
    writer.close()
  }

  def writer(os: OutputStream): XmlWriter = new XmlWriter(classes, rootName, defaultNamespaceURI, os)

  def parallelWriter(os: OutputStream): ParallelXmlWriter[T] = ParallelXmlWriter(rootName, os)
}
