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
final class SimpleGraph[N] private[util](val map: LMap[N]) {
  def +(fromto: (N, N)): SimpleGraph[N] = new SimpleGraph(map + fromto)

  def dependenciesOf(node: N): LSet[N] = map.dependenciesOf(node)

  def +(a: N): SimpleGraph[N] =
    if(map.contains(a))
      this
    else
      new SimpleGraph[N](map + a)

  def ++(that: SimpleGraph[N]): SimpleGraph[N] = new SimpleGraph(this.map ++ that.map)

  def ++(fromto: (N, LSet[N])): SimpleGraph[N] = {
    val that = new SimpleGraph(LMap() ++ fromto)
    this ++ that
  }

  def allNodes: LSet[N] = map.allNodes

  def topSort(listener: TopSortListener[N]): Boolean = TopSort.sort(GraphStructure, this, listener)

  def topSortResult(listener: TopSortListener[N] = TopSortListener.NoOpListener[N]): TopSortResult[N] =
    TopSort.sortResult(GraphStructure, this, listener)

  def topSortEx(listener: TopSortListener[N] = TopSortListener.NoOpListener[N]): Traversable[N] =
    TopSort.sortEx(GraphStructure, this, listener)

  object GraphStructure extends GraphStructure[SimpleGraph[N], N] {
    def nodes(graph: SimpleGraph[N]): Iterator[N] = graph.allNodes.iterator

    def nodeDependencies(graph: SimpleGraph[N], node: N): Iterator[N] =
      graph.dependenciesOf(node).iterator
  }
}

object SimpleGraph {
  final def apply[N](): SimpleGraph[N] = new SimpleGraph[N](LMap())
}
