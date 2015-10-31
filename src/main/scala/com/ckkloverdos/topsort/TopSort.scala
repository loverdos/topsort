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

import com.ckkloverdos.topsort.event.{ResultListener, TopSortListener}

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
  def sort[G, N](graphStructure: GraphStructure[G, N], graph: G, listener: TopSortListener[N]): Boolean = {
    val sorted = mutable.LinkedHashSet[N]()
    val searchPath = mutable.LinkedHashSet[N]()

    def sortNodes(nodes: Iterator[N], level: Int): Boolean = {
      if(nodes.hasNext) {
        val node = nodes.next()

        if(sorted.contains(node)) {
          listener.onAlreadySorted(node, level)
          return true
        }

        listener.onNewNode(node, level)

        if(searchPath.contains(node)) {
          listener.onCycle(searchPath.toList, level)
          return false
        }

        searchPath += node
        listener.onAddSearchPath(searchPath, node, level)

        val dependencies = graphStructure.nodeDependencies(graph, node)

        sortNodes(dependencies, level + 1) && {
          sorted += node
          listener.onAcceptSorted(node, level)

          searchPath -= node
          listener.onRemoveSearchPath(searchPath, node, level)

          sortNodes(nodes, level)
        }
      }
      else true
    }

    val nodes = graphStructure.nodes(graph)
    val result = sortNodes(nodes, 0)
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
  def sort[G, N](graphStructure: GraphStructure[G, N], graph: G): TopSortResult[N] = {
    var result: TopSortResult[N] = null
    val listener = new ResultListener[N](result = _)
    this.sort(graphStructure, graph, listener)
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
  def sortEx[G, N](graphStructure: GraphStructure[G, N], graph: G): Traversable[N] = {
    sort(graphStructure, graph) match {
      case ok @ TopSortOk(sorted) ⇒ sorted
      case ko @ TopSortCycle(path) ⇒ throw new TopSortCycleException(path)
    }
  }
}

class TopSortCycleException[N](path: Traversable[N]) extends Exception

object TopSort extends TopSort
