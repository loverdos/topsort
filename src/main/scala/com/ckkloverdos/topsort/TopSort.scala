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

import com.ckkloverdos.topsort.event.{TopSortListeners, ResultListener, TopSortListener}

import scala.collection.mutable

/**
 * Result parent type for topological sorting.
 * Two are the possible outcomes: Success is denoted by [[com.ckkloverdos.topsort.TopSortOk]]
 * and failure is denoted by [[com.ckkloverdos.topsort.TopSortCycle]].
 *
 * @tparam N is the graph node type
 *
 * @see TopSortOk, TopSortCycle
 */
sealed trait TopSortResult[N]

/**
 * A successful topological sorting result.
 *
 * @param sorted
 * @tparam N is the graph node type
 */
final case class TopSortOk[N](sorted: Traversable[N]) extends TopSortResult[N]

/**
 * A failed topological sorting result due to a cyclic dependency.
 * The `path` provided is the detected cycle.
 *
 * @param path The detected cycle.
 * @tparam N is the graph node type
 *
 */
final case class TopSortCycle[N](path: Traversable[N]) extends TopSortResult[N]

/**
 * Generalized, event-based topological sorter.
 */
class TopSort {
  def sort[G, N](
    graphStructure: GraphStructure[G, N],
    graph: G,
    listener: TopSortListener[N]
  ): Boolean = {
    // A node is immediately added to the `searchPath` if it does not exist
    // there (otherwise we have a cycle)
    val searchPath = mutable.LinkedHashSet[N]()

    // A node is added to the sorted set only after its dependencies
    // have been added to the sorted set
    val sorted = mutable.LinkedHashSet[N]()

    def sortNodes(dependents: List[N], nodes: Iterator[N], level: Int): Boolean =
      if(!nodes.hasNext)
        true
      else
        sortNodeStep(dependents, nodes.next(), nodes, level)

    def sortNodeStep(dependents: List[N], node: N, remaining: Iterator[N], level: Int): Boolean = {
      // Start checking a node.
      // Checking ends in one of:
      //   1. The node already exists in the sorted set
      //   2. The node has already been searched, so we have a cyclic dependency
      //   3. We can proceed searching the node and its dependencies
      listener.onEnter(dependents, node, level)

      if(sorted.contains(node)) {
        listener.onAlreadySorted(node, level)

        // It is a bug to just return true here and ignore the `remaining` nodes.
        return sortNodes(dependents, remaining, level)
      }

      if(searchPath.contains(node)) {
        // maybe we would like to add `node` to `searchPath`, so that
        // when we print `searchPath` we see `node` on both ends
        // but this is not possible: `searchPath` is a set and so
        // `node` already exists there, it cannot be re-added.
        listener.onCycle(searchPath, level)
        return false
      }

      searchPath += node
      listener.onAddedToSearchPath(searchPath, node, level)

      val dependencies = graphStructure.nodeDependencies(graph, node)

      val dependencyResult = sortNodes(node :: dependents, dependencies, level + 1)
      if(!dependencyResult) {
        sorted.clear()
        searchPath.clear()
        return false
      }

      sorted += node
      listener.onAddedToSorted(dependents, node, level)

      searchPath -= node
      listener.onRemovedFromSearchPath(searchPath, node, level)

      // proceed with the remaining nodes
      sortNodes(dependents, remaining, level)
    }

    val nodes = graphStructure.nodes(graph)

    val result = sortNodes(Nil, nodes, 0)
    if(result)
      listener.onResultSorted(sorted)
    else
      listener.onResultCycle(searchPath)

    result
  }

  /**
   * Sorts a graph topologically and returns the result.
   *
   * This method does not throw any [[com.ckkloverdos.topsort.TopSortCycleException]].
   *
   * @param graphStructure
   * @param graph
   * @tparam G
   * @tparam N
   * @return
   */
  def sortResult[G, N](
    graphStructure: GraphStructure[G, N],
    graph: G,
    listener: TopSortListener[N] = TopSortListener.NoOpListener
  ): TopSortResult[N] = {
    var result: TopSortResult[N] = null
    val resultListener = new ResultListener[N](result = _)
    val allListeners = new TopSortListeners(resultListener, listener)
    this.sort(graphStructure, graph, allListeners)
    assert(result ne null)
    result
  }

  /**
   * Sorts a graph topologically and returns the sorted nodes,
   * from the more independent one to the more dependent one.
   *
   * It throws a [[com.ckkloverdos.topsort.TopSortCycleException]] in case there is a cycle.
   *
   * @param graphStructure
   * @param graph
   * @tparam G
   * @tparam N
   * @return
   */
  def sortEx[G, N](
    graphStructure: GraphStructure[G, N],
    graph: G,
    listener: TopSortListener[N] = TopSortListener.NoOpListener
  ): Traversable[N] = {
    sortResult(graphStructure, graph, listener) match {
      case ok @ TopSortOk(sorted) ⇒ sorted
      case ko @ TopSortCycle(path) ⇒ throw new TopSortCycleException(path)
    }
  }
}

class TopSortCycleException[N](path: Traversable[N]) extends Exception

object TopSort extends TopSort
