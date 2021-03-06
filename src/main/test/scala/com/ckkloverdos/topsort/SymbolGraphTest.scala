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

package com.ckkloverdos.topsort

import com.ckkloverdos.topsort.event.{ExitCause, PrintStreamListener, TopSortListener}
import com.ckkloverdos.topsort.util.{LSet, LTopSortPerNode, MLTopSortPerNode, SymbolGraph}
import org.junit.{Assert, Test}

import scala.annotation.tailrec
import scala.collection.mutable

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
    val sorted = graph.topSortEx()

    sameIterators(Iterator('b, 'c, 'd, 'a), sorted.toIterator)
  }

  @Test def parseNoCycle2(): Unit = {
    val graph = SymbolGraph("a -> b; b -> c;;;; c -> d")
    val sorted = graph.topSortEx()

    sameIterators(Iterator('d, 'c, 'b, 'a), sorted.toIterator)
  }

  @Test def parseCycle1(): Unit = {
    val graph = SymbolGraph("a -> a")
    try {
      val _ = graph.topSortEx()
      Assert.fail(""""a -> a" is a cycle""")
    }
    catch {
      case _: TopSortCycleException[_] ⇒
    }
  }

  @Test def checkDependents(): Unit = {
    val graphStr =
      """
        | A -> A_1, A_2, A_3
        | B -> B_1, B_2
      """.stripMargin

    val graph = SymbolGraph(graphStr)

    assert(graph.dependenciesOf('A).size == 3, "graph.getOrEmpty('A).size == 3")
    assert(graph.dependenciesOf('B).size == 2, "graph.getOrEmpty('B).size == 2")

    // A map of the direct dependencies (not top-sorted)
    val map = new mutable.LinkedHashMap[Symbol, mutable.LinkedHashSet[Symbol]]
    def add(from: Symbol, to: Symbol): Unit = {
      map.get(from) match {
        case Some(toSet) ⇒ toSet += to
        case None ⇒
          val toSet = new mutable.LinkedHashSet[Symbol]
          toSet += to
          map(from) = toSet
      }
    }
    def addOne(node: Symbol): Unit = {
      map.get(node) match {
        case None ⇒ map(node) = new mutable.LinkedHashSet[Symbol]
        case _ ⇒
      }
    }

    val listener = new TopSortListener[Symbol] {
      override def onEnter(dependents: List[Symbol], node: Symbol, level: Int): Unit = {
        for(dependent ← dependents) {
          add(dependent, node)
        }
        addOne(node)
      }

      override def onExit(
        dependents: List[Symbol],
        node: Symbol,
        exitCause: ExitCause,
        searchPath: Traversable[Symbol],
        level: Int
      ): Unit = {
        exitCause match {
          case ExitCause.AlreadySorted ⇒
            for(dependent ← dependents) {
              add(dependent, node)
            }

          case _ ⇒
        }
      }
    }

    val path = graph.topSortEx(PrintStreamListener.StdOut.andThen(listener))
    val sortedNodes = path.to[mutable.LinkedHashSet]
    val allNodes = graph.allNodes
    for {
      node ← allNodes
    } {
      assert(sortedNodes.contains(node), s"sortedNodes.contains($node)")
    }

    Assert.assertEquals(Set('A, 'A_1, 'A_2, 'A_3, 'B, 'B_1, 'B_2), map.keySet.toSet)
    Assert.assertEquals(Set('A_1, 'A_2, 'A_3), map('A))
    Assert.assertEquals(Set('B_1, 'B_2), map('B))
  }

  @Test def checkYabOrders(): Unit = {
    val graphStr =
      """
        | /::fatjar   → /a::jar, /c::jar, /b::jar
        | /a::jar     → /a::compile
        | /b::jar     → /b::compile
        | /c::jar     → /c::compile
        | /c::compile → /b::compile, /a::compile
        | /c::fatjar  → /c::jar
      """.stripMargin

    val graph = SymbolGraph(graphStr)

    val lTopSortedListener = new LTopSortPerNode[Symbol]
    val mlTopSortedListener = new MLTopSortPerNode[Symbol]

    val path = graph.topSortEx(
      PrintStreamListener.StdOut[Symbol].
        andThen(lTopSortedListener).
        andThen(mlTopSortedListener)
    )
    val lTopSortedMap = lTopSortedListener.topSortedMap
    val mlTopSortedMap = mlTopSortedListener.topSortedMap

    val pathReport = path.mkString("[", ", ", "]")
    println("====================")
    println(pathReport)

    println("====================")
    for { node ← lTopSortedMap.keySet } {
      val deps = lTopSortedMap(node)
      val depsReport = deps.mkString(", ")
      println(s"$node → $depsReport")
    }

    Assert.assertEquals(path.size, lTopSortedMap.allNodes.size)
    Assert.assertEquals(mlTopSortedMap.size, lTopSortedMap.size)

    // Every topsorted path must start with a node that depends on no other
    for { node ←  lTopSortedMap.keySet } {
      val topSortedDeps = lTopSortedMap(node)

      topSortedDeps.toSeq.headOption match {
        case None ⇒
        case Some(first) ⇒
          Assert.assertEquals(true, lTopSortedMap.contains(node))
          val firstDeps = lTopSortedMap(first)
          Assert.assertEquals(
            s"Every topsorted path must start with a node that depends on no other. Offending node: $first",
            LSet[Symbol](),
            firstDeps
          )
      }
    }
  }
}
