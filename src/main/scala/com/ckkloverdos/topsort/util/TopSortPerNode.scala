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

/**
  * Computes the topologically sorted dependencies per node.
  *
  * @param addDependency Called when a dependency is encountered.
  *                      The first item is the dependent, the second is the dependency.
  * @param addSortedNode Called when a node is added to the topologically sorted set
  * @tparam N The node type
  */
class TopSortPerNode[N](
  addDependency: (N, N) ⇒ Unit,
  addSortedNode: (N) ⇒ Unit
) extends TopSortListener[N] {
  override def onAlreadySorted(dependents: List[N], node: N, level: Int): Unit = {
    // we keep adding pairs, since this time `node` may appear with more
    // `dependents` than in `onAddedToSorted`
    dependents.foreach(dependent ⇒ addDependency(dependent, node))
  }

  override def onAddedToSorted(dependents: List[N], node: N, level: Int): Unit = {
    dependents.foreach(dependent ⇒ addDependency(dependent, node))
    addSortedNode(node)
  }
}
