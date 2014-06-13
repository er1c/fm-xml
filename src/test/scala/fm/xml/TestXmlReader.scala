/*
 * Copyright 2014 Frugal Mechanic (http://frugalmechanic.com)
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
package fm.xml

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets.UTF_8
import org.scalatest.{FunSuite, Matchers}

final class TestXmlReader extends FunSuite with Matchers {
  
  test("isXml()") {
    def isXML(s: String): Boolean = XmlReader.isXML(new ByteArrayInputStream(s.getBytes(UTF_8))) 
    
    isXML("foo") should equal (false)
    isXML("foo<hello>") should equal (false)
    
    isXML("<hello>") should equal (true) // This looks like XML which is why it's true
    isXML("<!-- foo --><hello>") should equal (true)
    isXML("""<?xml version="1.0" encoding="ISO-8859-1"?><ACES version="2.0">""") should equal (true)
  }
}