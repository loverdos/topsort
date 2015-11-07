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

/**
 * Provides API related to the node structure of a graph.
 * This is totally external to concrete graph representations.
 * The reason for its introduction is to not
 * impose specific nominal sub-typing on Graph implementations.
 *
 * `G` is the graph data type and `N` is the node data type.
 */
trait GraphStructure[G, N] {
  /**
   * Given a `graph`, returns an iterator of its nodes.
   *
   * @param graph The given graph.
   * @return An iterator of the `graph` nodes.
   */
  def nodes(graph: G): Iterator[N]

  /**
   * Given a `node` belonging to `graph`, returns an iterator with the `node` dependencies.
   */
  def nodeDependencies(graph: G, node: N): Iterator[N]
}
