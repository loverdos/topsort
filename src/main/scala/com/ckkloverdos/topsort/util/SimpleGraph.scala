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

import com.ckkloverdos.topsort.event.TopSortListener
import com.ckkloverdos.topsort.{GraphStructure, TopSort, TopSortResult}

import scala.language.implicitConversions

/**
 * A simple immutable graph representation as a map of nodes to its dependencies.
 */
final case class SimpleGraph[N](map: Map[N, Set[N]]) {
  def +(ab: (N, N)): SimpleGraph[N] = {
    val (a, b) = ab
    val newBSet =
      map.get(a) match {
        case None ⇒ Set(b)
        case Some(set) ⇒ set + b
      }
    val newMap = map.updated(a, newBSet)
    SimpleGraph(newMap)
  }

  def +(a: N): SimpleGraph[N] =
    if(map.contains(a))
      this
    else
      SimpleGraph(map.updated(a, Set()))

  def ++(that: SimpleGraph[N]): SimpleGraph[N] = {

    val keys = this.map.keySet ++ that.map.keySet
    val newValue =
      for(key ← keys) yield {
        val thisSet = this.map.getOrElse(key, Set())
        val thatSet = that.map.getOrElse(key, Set())

        (key, thisSet ++ thatSet)
      }

    SimpleGraph(Map(newValue.toSeq:_*))
  }

  def topSort(listener: TopSortListener[N]): Boolean = TopSort.sort(GraphStructure, this, listener)

  def topSort: TopSortResult[N] = TopSort.sort(GraphStructure, this)

  def topSortEx: Traversable[N] = TopSort.sortEx(GraphStructure, this)

  object GraphStructure extends GraphStructure[SimpleGraph[N], N] {
    def nodes(graph: SimpleGraph[N]): Iterator[N] = graph.map.keysIterator

    def nodeDependencies(graph: SimpleGraph[N], node: N): Iterator[N] =
      graph.map.get(node) match {
        case None ⇒ Iterator.empty
        case Some(deps) ⇒ deps.iterator
      }
  }
}

