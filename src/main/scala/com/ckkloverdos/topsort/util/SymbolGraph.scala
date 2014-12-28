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
import com.ckkloverdos.topsort.{TopSortResult, GraphStructure, TopSort}

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

  def topSort(listener: TopSortListener[Symbol]): Boolean =
    TopSort.sort(SymbolGraph.SymbolGraphStruture, this, listener)

  def topSort: TopSortResult[Symbol] =
    TopSort.sort(SymbolGraph.SymbolGraphStruture, this)
}

object SymbolGraph {
  object SymbolGraphStruture extends GraphStructure[SymbolGraph, Symbol] {
    def nodes(structure: SymbolGraph): Iterator[Symbol] =
      structure.map.keysIterator

    def nodeDependencies(structure: SymbolGraph, node: Symbol): Iterator[Symbol] =
      structure.map.get(node) match {
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
