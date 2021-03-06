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

package com.ckkloverdos.topsort.event

class TopSortListeners[N](listeners: TopSortListener[N]*) extends TopSortListener[N] {
  override def onEnter(dependents: List[N], node: N, level: Int): Unit =
    listeners.foreach(_.onEnter(dependents, node, level))

  override def onAddedToSearchPath(path: Traversable[N], dependents: List[N], addedNode: N, level: Int): Unit =
    listeners.foreach(_.onAddedToSearchPath(path, dependents, addedNode, level))

  override def onNodeDependenciesBegin( dependents: List[N], node: N, dependencies: Iterator[N], level: Int): Unit =
    listeners.foreach(_.onNodeDependenciesBegin(dependents, node, dependencies, level))

  override def onNodeDependenciesEnd( dependents: List[N], node: N, result: Boolean, level: Int): Unit  =
    listeners.foreach(_.onNodeDependenciesEnd(dependents, node, result, level))

  override def onRemovedFromSearchPath(path: Traversable[N], removedNode: N, level: Int): Unit =
    listeners.foreach(_.onRemovedFromSearchPath(path, removedNode, level))

  override def onExit( dependents: List[N], node: N, exitCause: ExitCause, searchPath: Traversable[N], level: Int): Unit =
    listeners.foreach(_.onExit(dependents, node, exitCause, searchPath, level))

  override def onResultSorted(sorted: Traversable[N]): Unit =
    listeners.foreach(_.onResultSorted(sorted))

  override def onResultCycle(path: Traversable[N]): Unit =
    listeners.foreach(_.onResultCycle(path))
}
