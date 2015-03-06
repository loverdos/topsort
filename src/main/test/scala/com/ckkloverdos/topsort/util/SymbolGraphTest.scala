/*
 * Copyright 2014 Christos KK Loverdos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ckkloverdos.topsort.util

import com.ckkloverdos.topsort.TopSortCycleException
import org.junit.{Assert, Test}

import scala.annotation.tailrec

/**
 *
 */
class SymbolGraphTest {
  @tailrec
  final def sameIterators[N](a: Iterator[N], b: Iterator[N]): Unit = {
    if(a.hasNext) {
      Assert.assertTrue(b.hasNext)
      val a0 = a.next()
      val b0 = b.next()

      Assert.assertEquals(a0, b0)
      sameIterators(a, b)
    }
    else
      Assert.assertFalse(b.hasNext)
  }


  @Test def parseNoCycle1(): Unit = {
    val graph = SymbolGraph("a -> b; a -> c; a -> d")
    val sorted = graph.topSortEx

    sameIterators(Iterator('b, 'c, 'd, 'a), sorted.toIterator)
  }

  @Test def parseNoCycle2(): Unit = {
    val graph = SymbolGraph("a -> b; b -> c;;;; c -> d")
    val sorted = graph.topSortEx

    sameIterators(Iterator('d, 'c, 'b, 'a), sorted.toIterator)
  }

  @Test def parseCycle1(): Unit = {
    val graph = SymbolGraph("a -> a")
    try {
      val sorted = graph.topSortEx
      Assert.fail(""""a -> a" is a cycle""")
    }
    catch {
      case _: TopSortCycleException[_] ⇒
    }
  }
}
