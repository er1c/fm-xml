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

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

final class TestXmlReaderPathStack extends AnyFunSuite with Matchers {
  test("Single Element Stack") {
    val stack: XmlReaderPathStack = new XmlReaderPathStack(1)

    stack.length should equal(0)
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(0)

    stack.pushElement("foo")
    stack.length should equal(1)
    stack(0) should equal("foo")
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(1)

    stack.pushElement("bar")
    stack.length should equal(2)
    stack(0) should equal("foo")
    stack(1) should equal(null)
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(2)

    stack.pushElement("baz")
    stack.length should equal(3)
    stack(0) should equal("foo")
    stack(1) should equal(null)
    stack(2) should equal(null)
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(3)

    stack.pop()
    stack.length should equal(2)
    stack(0) should equal("foo")
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(2)

    stack.pop()
    stack.length should equal(1)
    stack(0) should equal("foo")
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(1)

    stack.pop()
    stack.length should equal(0)
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(0)

    stack.pop()
    stack.length should equal(0)
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(0)
  }

  test("Five Element Stack") {
    val stack: XmlReaderPathStack = new XmlReaderPathStack(5)

    stack.pushElement("one")
    stack.pushElement("two")
    stack.pushElement("three")
    stack.pushElement("four")
    stack.pushElement("five")
    stack.pushElement("six")

    stack.length should equal(6)

    stack(0) should equal("one")
    stack(1) should equal("two")
    stack(2) should equal("three")
    stack(3) should equal("four")
    stack(4) should equal("five")
    stack(5) should equal(null)
    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(6)

    stack.pop()
    stack.length should equal(5)

    stack.pop()
    stack.length should equal(4)

    stack.pop()
    stack.length should equal(3)

    stack.pop()
    stack.length should equal(2)

    stack.pop()
    stack.length should equal(1)

    stack.pop()
    stack.length should equal(0)

    an[ArrayIndexOutOfBoundsException] should be thrownBy stack(0)
  }
}
