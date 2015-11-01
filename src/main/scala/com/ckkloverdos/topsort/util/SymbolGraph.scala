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

import com.ckkloverdos.topsort.event.PrintStreamListener

import scala.annotation.tailrec
import scala.language.implicitConversions

/**
 * Convenience methods to create a graph of symbols from a simple
 * textual representation.
 * The motivation is to help debugging.
 */
object SymbolGraph {
  final type SymbolGraph = SimpleGraph[Symbol]
  final val Empty: SymbolGraph = SimpleGraph[Symbol](Map())

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

  implicit class DependableSymbol(val a: Symbol) extends AnyVal {
    def depends(b: Symbol) = (a, b)
    def ==>(b: Symbol) = (a, b)
  }

  val AGraph = Empty +
    ('A ==> 'B) /* 'A depends on 'B */ +
    ('A ==> 'C) + 
    ('A ==> 'D) +
    ('C ==> 'D)

  val BGraph = Empty +
    ('A ==> 'B) +
    ('A ==> 'C) +
    ('A ==> 'D) +
    ('C ==> 'D) +
    ('D ==> 'A)

  def main(args: Array[String]): Unit = {
    println("+======= A Graph =======")
    val printListener = PrintStreamListener.StdOut[Symbol]
    println(AGraph.topSort(printListener))
    println("-======= A Graph =======")
    println("+======= B Graph =======")
    println(BGraph.topSortResult(printListener))
    println("-======= B Graph =======")
  }
}
