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

import scala.collection.mutable

class LTopSortPerNode[N] extends TopSortListener[N] {
  private var lmap = LMap[N]()
  private val addDependency: (N, N) ⇒ Unit = { case fromto ⇒ lmap += fromto }
  private val addSortedNode: (N) ⇒ Unit = node ⇒ lmap += node

  private val topSortPerNode = new TopSortPerNode[N](addDependency, addSortedNode)

  override def onAlreadySorted(dependents: List[N], node: N, level: Int): Unit =
    topSortPerNode.onAlreadySorted(dependents, node, level)

  override def onAddedToSorted(dependents: List[N], node: N, level: Int): Unit =
    topSortPerNode.onAddedToSorted(dependents, node, level)

  def topSortedMap: LMap[N] = lmap
}

class MLTopSortPerNode[N] extends TopSortListener[N] {
  // mutable linked map
  private val mlmap = new mutable.LinkedHashMap[N, mutable.LinkedHashSet[N]]

  private def get(n: N) =
    if(mlmap.contains(n))
      mlmap(n)
    else {
      val value = new mutable.LinkedHashSet[N]
      mlmap(n) = value
      value
    }

  private def ensure(n: N) = get(n)

  private val addDependency: (N, N) ⇒ Unit = { case (from, to) ⇒ get(from) += to }
  private val addSortedNode: (N) ⇒ Unit = node ⇒ ensure(node)

  private val topSortPerNode = new TopSortPerNode[N](addDependency, addSortedNode)

  override def onAlreadySorted(dependents: List[N], node: N, level: Int): Unit =
    topSortPerNode.onAlreadySorted(dependents, node, level)

  override def onAddedToSorted(dependents: List[N], node: N, level: Int): Unit =
    topSortPerNode.onAddedToSorted(dependents, node, level)

  def topSortedMap: mutable.LinkedHashMap[N, mutable.LinkedHashSet[N]] = mlmap
}
