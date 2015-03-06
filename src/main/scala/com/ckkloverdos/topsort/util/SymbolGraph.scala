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

import com.ckkloverdos.topsort.event.{PrintStreamListener, TopSortListener}
import com.ckkloverdos.topsort.{GraphStructure, TopSort, TopSortResult}

import scala.annotation.tailrec
import scala.language.implicitConversions

/**
 *
 */
case class SymbolGraph(map: Map[Symbol, Set[Symbol]] = Map()) {
  def +(ab: (Symbol, Symbol)): SymbolGraph = {
    val (a, b) = ab
    val newBSet =
      map.get(a) match {
        case None ⇒ Set(b)
        case Some(set) ⇒ set + b
      }
    val newMap = map.updated(a, newBSet)
    SymbolGraph(newMap)
  }

  def +(a: Symbol): SymbolGraph =
    if(map.contains(a))
      this
    else
      SymbolGraph(map.updated(a, Set()))

  def ++(that: SymbolGraph): SymbolGraph = {

    val keys = this.map.keySet ++ that.map.keySet
    val newValue =
      for(key ← keys) yield {
        val thisSet = this.map.getOrElse(key, Set())
        val thatSet = that.map.getOrElse(key, Set())

        (key, thisSet ++ thatSet)
      }

    SymbolGraph(Map(newValue.toSeq:_*))
  }

  def topSort(listener: TopSortListener[Symbol]): Boolean =
    TopSort.sort(SymbolGraph.SymbolGraphStructure, this, listener)

  def topSort: TopSortResult[Symbol] =
    TopSort.sort(SymbolGraph.SymbolGraphStructure, this)

  def topSortEx: Traversable[Symbol] =
    TopSort.sortEx(SymbolGraph.SymbolGraphStructure, this)
}

object SymbolGraph {
  final val Empty = SymbolGraph()

  //////////////////////////////////////////////
  // Input looks like this:
  //  "a->b;b->c;c->a;a->a,b->a"
  //////////////////////////////////////////////
  // The approximate grammar is:
  //   graph ::= line (';' line)*
  //          |  line (',' line)*
  //   line  ::= node '->' node ('->' node)*
  //   node  ::= 'a' | 'b' | 'c' | ...
  //
  // Errors may be tolerated (e.g. s = "a b" produces an empty graph)
  // but in such cases consider the behaviour as undefined.
  //////////////////////////////////////////////
  def apply(s: String): SymbolGraph = {
    def parseLine(one: String): SymbolGraph = {
      val split0 = one.split("""\s*->\s*""")
      val split1 = split0.map(_.trim)
      val split2 = split1.filterNot(_.isEmpty)
      val split3 = split2.filter(_.length == 1)

      @tailrec
      def iterate(from: String, others: Iterator[String], graph: SymbolGraph): SymbolGraph = {
        if(others.hasNext) {
          val to = others.next()
          iterate(to, others, graph + (Symbol(from), Symbol(to)))
        }
        else graph + Symbol(from)
      }

      val split = split3
      split.length match {
        case 0 ⇒ Empty
        case _ ⇒
          val iterator = split.iterator
          val from = iterator.next()
          iterate(from, iterator, Empty)
      }
    }

    val split0 = s.split("""\s*(;|,)\s*""")
    val split = split0

    split.map(parseLine).foldLeft(Empty)(_ ++ _)
  }

  object SymbolGraphStructure extends GraphStructure[SymbolGraph, Symbol] {
    def nodes(graph: SymbolGraph): Iterator[Symbol] =
      graph.map.keysIterator

    def nodeDependencies(graph: SymbolGraph, node: Symbol): Iterator[Symbol] =
      graph.map.get(node) match {
        case None ⇒ Iterator.empty
        case Some(deps) ⇒ deps.iterator
      }
  }

  implicit class DependableSymbol(val a: Symbol) extends AnyVal {
    def depends(b: Symbol) = (a, b)
    def ==>(b: Symbol) = (a, b)
  }

  val AGraph = new SymbolGraph() +
    ('A depends 'B) + 
    ('A depends 'C) + 
    ('A depends 'D) +
    ('C depends 'D)

  val BGraph = new SymbolGraph() +
    ('A depends 'B) +
    ('A depends 'C) +
    ('A depends 'D) +
    ('C depends 'D) +
    ('D depends 'A)

  def main(args: Array[String]): Unit = {
    println("======= A Graph =======")
    val alistener = PrintStreamListener.StdOut[Symbol]
    println(AGraph.topSort(alistener))
    println("======= A Graph =======")
    println("======= B Graph =======")
    println(BGraph.topSort)
    println("======= B Graph =======")
  }
}
