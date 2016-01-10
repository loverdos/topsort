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

    // A node is added to the `topSorted` (= topologically sorted) set
    // only after its dependencies have been added to the `topSorted` set
    // and they do not introduce a cycle
    val topSorted = mutable.LinkedHashSet[N]()

    def sortNodes(dependents: List[N], nodes: Iterator[N], level: Int): Boolean =
      if(!nodes.hasNext)
        true
      else
        sortNodeStep(dependents, nodes.next(), nodes, level)

    def sortNodeStep(dependents: List[N], node: N, remaining: Iterator[N], level: Int): Boolean = {
      // Start checking a node.
      // Checking ends in one of:
      //   1. The node already exists in the `topSorted` set
      //   2. The node has already been searched, so we have a cyclic dependency
      //   3. Checking the dependencies result in a cycle
      //   4. We can proceed searching the remaining (as given by the graph structure) nodes
      listener.onEnter(dependents, node, level)

      // 1. 1st exit point, as mentioned above
      if(topSorted.contains(node)) {
        listener.onAlreadySorted(dependents, node, level)

        // It is a bug to just return true here and ignore the `remaining` nodes.
        return sortNodes(dependents, remaining, level)
      }

      // 2. 2nd exit point, as mentioned above
      if(searchPath.contains(node)) {
        // maybe we would like to add `node` to `searchPath`, so that
        // when we print `searchPath` we see `node` on both ends
        // but this is not possible: `searchPath` is a set and so
        // `node` already exists there, it cannot be re-added.

        listener.onCycle(searchPath, level)
        return false
      }

      // ADD to search path, since it has not been topSorted and we are not in a cycle
      searchPath += node
      listener.onAddedToSearchPath(searchPath, dependents, node, level)

      val dependencies0 = graphStructure.nodeDependencies(graph, node)
      val (dependencies, dependenciesCopy) = dependencies0.duplicate

      val dependents1 = node :: dependents
      val level1 = level + 1

      // Notify that we begin processing the dependencies of the node
      listener.onNodeDependenciesBegin(dependents1, node, dependenciesCopy, level1)

      // Process the dependencies first
      val dependencyResult = sortNodes(dependents1, dependencies, level1)

      // Notify that we have ended processing the dependencies of the node
      // also informing of the result
      listener.onNodeDependenciesEnd(dependents1, node, dependencyResult, level)

      // 3. 3rd exit point, as mentioned above
      if(!dependencyResult) {
        return false
      }

      // ADD to `topSorted` set, after we have successfully (== no cycle)
      // processed (= topologically sorted) the dependencies
      topSorted += node
      listener.onAddedToSorted(dependents, node, level)

      searchPath -= node
      listener.onRemovedFromSearchPath(searchPath, node, level)

      // proceed with the remaining nodes
      // 4. 4th exit point, as mentioned above
      sortNodes(dependents, remaining, level)
    }

    // Get all the nodes in the form of an iterator
    val nodes = graphStructure.nodes(graph)

    // Sort them topologically, detecting any cycles in the process.
    // A `false` result means we detected a cycle, so the sorting failed.
    val result = sortNodes(Nil, nodes, 0)
    if(result)
      listener.onResultSorted(topSorted)
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

object TopSort extends TopSort
