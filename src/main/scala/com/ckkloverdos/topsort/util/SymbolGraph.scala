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
import com.ckkloverdos.topsort.util.SimpleGraph.LSet

import scala.language.implicitConversions

/**
 * Convenience methods to create a graph of symbols from a simple
 * textual representation.
 * The motivation is to help debugging.
 */
object SymbolGraph {
  final type SymbolGraph = SimpleGraph[Symbol]

  def Empty: SymbolGraph = SimpleGraph[Symbol]()

  //////////////////////////////////////////////
  // ~ approximate implementation ~
  //////////////////////////////////////////////
  //   graph  ::= fromto (';'  fromto)*
  //           |  fromto ('\n' fromto)*
  //   fromto ::= node '->' node (',' node)*
  //   node   ::= symbol
  //////////////////////////////////////////////
  def apply(source: String): SymbolGraph = {
    val fromtoArray = source.
      replaceAll("""/\*.+?\*/""", ""). // comments (!)
      split("\n|;").
      map(_.trim).
      filterNot(_.isEmpty)

    def parseFromTo(fromto: String): SymbolGraph = {
      val split = fromto.
        split("->|→"). // Separate by "->"
        map(_.trim).           // Trim results
        filterNot(_.isEmpty)   // Keep only non empty strings

      split match {
        case Array(from, to) ⇒
          val fromSymbol = Symbol(from)
          val toSymbols = LSet[Symbol]() ++ to.
            split(",").
            map(_.trim).
            filterNot(_.isEmpty).
            map(Symbol(_))

          val initialGraph: SymbolGraph = Empty ++ (fromSymbol, toSymbols)

          // The initial graph (zero element in foldLeft)
          // is the one with the dependencies and then we
          // create one empty mapping for each dependency (`toSymbols`).
          toSymbols.foldLeft(initialGraph)(_ + _)
      }
    }

    fromtoArray.
      map(parseFromTo).
      foldLeft(Empty)(_ ++ _)
  }
  
  val AGraph: SymbolGraph = Empty +
    ('A → 'C) +
          ('C → 'D) +
    ('A → 'D) +
          ('D → 'B)

  val BGraphStr =
    """
      | A → B
      | A → C
      | A → D
      | C → D
      | D → A
    """.stripMargin
  val BGraph: SymbolGraph = SymbolGraph(BGraphStr)

  def main(args: Array[String]): Unit = {
    println(AGraph)
    println("+======= A Graph =======")
    val printListener = PrintStreamListener.StdOut[Symbol]
    println(AGraph.topSort(printListener))
    println("-======= A Graph =======")
//    println("+======= B Graph =======")
//    println(BGraph.topSortResult(printListener))
//    println("-======= B Graph =======")
  }
}
