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
package event

/**
 * A listener of interesting events from [[com.ckkloverdos.topsort.TopSort TopSort]].
 */
trait TopSortListener[N] {
  def onEnter(dependents: List[N], node: N, level: Int): Unit = {}

  /**
    *
    * @param dependents The parent node hierarchy.
    * @param node The `node` of interest.
    * @param exitCause
    * @param searchPath The current search path.
    * @param level The level/depth in the search DAG. Visiting the dependent nodes increases the level.
    */
  def onExit(
    dependents: List[N],
    node: N,
    exitCause: ExitCause,
    searchPath: Traversable[N],
    level: Int
  ): Unit = {}

  /**
    * Notifies that the algorithm begins processing the `dependencies` of the `node`.
    */
  def onNodeDependenciesBegin(dependents: List[N], node: N, dependencies: Iterator[N], level: Int): Unit = {}

  /**
    * Notifies that the algorithm has ended processing the `dependencies` of the `node`,
    * also providing the so-far `result`. A cycle has been reached if and only if the
    * `result` is `false` (but the algorithm has not finished yet).
    */
  def onNodeDependenciesEnd(dependents: List[N], node: N, result: Boolean, level: Int): Unit = {}

  /**
    * Notifies that the algorithm proceeds with the given `node`, that is
    * it has not been sorted yet and it has not been searched before (so that
    * we are not in cycle).
    *
    * @param path
    * @param addedNode
    * @param level
    */
  def onAddedToSearchPath(path: Traversable[N], dependents: List[N], addedNode: N, level: Int): Unit = {}

  def onRemovedFromSearchPath(path: Traversable[N], removedNode: N, level: Int): Unit = {}

  /**
    * Notifies that topological sorting has completed in success.
    * @param sorted
    */
  def onResultSorted(sorted: Traversable[N]): Unit = {}

  /**
    * Notifies that topological sorting has completed in failure and a cycle has been detected.
    * @param path
    */
  def onResultCycle(path: Traversable[N]): Unit = {}

  /**
    * Sequentially combines `this` listener with `that` listener, creating a new listener.
    * @param that the other listener
    * @return the new listener
    */
  def andThen(that: TopSortListener[N]): TopSortListener[N] = new TopSortListeners(this, that)
}

object TopSortListener {
  final def NoOpListener[N]: TopSortListener[N] = new TopSortListener[N] {}
}

