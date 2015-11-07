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
  def onEnter(dependentOpt: Option[N], node: N, level: Int): Unit = {}

  def onAlreadySorted(node: N, level: Int): Unit = {}

  /**
    * Notifies that the `path` leads to a cycle and, as a consequence, the
    * topologically sorting procedure is about to end.
    *
    * @param path
    * @param level
    */
  def onCycle(path: Traversable[N], level: Int): Unit = {}

  def onAddedToSearchPath(path: Traversable[N], addedNode: N, level: Int): Unit = {}

  def onRemovedFromSearchPath(path: Traversable[N], removedNode: N, level: Int): Unit = {}

  /**
    * Notifies that the `node` has been added to the collection of the already sorted nodes.
    * @param dependentOpt
    * @param node
    * @param level
    */
  def onAddedToSorted(node: N, level: Int): Unit = {}

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

class TopSortListeners[N](listeners: TopSortListener[N]*) extends TopSortListener[N] {
  override def onEnter(dependentOpt: Option[N], node: N, level: Int): Unit =
    listeners.foreach(_.onEnter(dependentOpt, node, level))

  override def onCycle(path: Traversable[N], level: Int): Unit =
    listeners.foreach(_.onCycle(path, level))

  override def onAddedToSearchPath(path: Traversable[N], addedNode: N, level: Int): Unit =
    listeners.foreach(_.onAddedToSearchPath(path, addedNode, level))

  override def onRemovedFromSearchPath(path: Traversable[N], removedNode: N, level: Int): Unit =
    listeners.foreach(_.onRemovedFromSearchPath(path, removedNode, level))

  override def onAddedToSorted(node: N, level: Int): Unit =
    listeners.foreach(_.onAddedToSorted(node, level))

  override def onAlreadySorted(node: N, level: Int): Unit =
    listeners.foreach(_.onAlreadySorted(node, level))

  override def onResultSorted(sorted: Traversable[N]): Unit =
    listeners.foreach(_.onResultSorted(sorted))

  override def onResultCycle(path: Traversable[N]): Unit =
    listeners.foreach(_.onResultCycle(path))
}


