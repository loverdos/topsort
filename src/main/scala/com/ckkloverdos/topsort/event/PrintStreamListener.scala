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

import java.io.PrintStream

/**
 *
 */
class PrintStreamListener[N](out: PrintStream) extends TopSortListener[N] {
  def w(s: String, level: Int) = out.println((" " * (level * 2)) + s)

  private[this] def d(dependents: List[N]) =
    dependents match {
      case Nil ⇒ ""
      case _ ⇒ s" (← ${dependents.mkString("←")})"
    }

  override def onEnter(dependents: List[N], node: N, level: Int) =
    w(s"Enter: $node${d(dependents)}", level)

  override def onCycle(path: Traversable[N], level: Int) =
    w(s"Cycle: ${path.mkString(" → ")}", level)

  override def onAddedToSearchPath(path: Traversable[N], addedNode: N, level: Int) =
    w(s"Search path (+ $addedNode): ${path.mkString(" → ")}", level)

  override def onRemovedFromSearchPath(path: Traversable[N], removedNode: N, level: Int) =
    w(s"Search path (- $removedNode): ${path.mkString(" → ")}", level)

  override def onAddedToSorted(dependents: List[N], node: N, level: Int) =
    w(s"ACCEPT <$node>${d(dependents)}", level)

  override def onAlreadySorted(node: N, level: Int): Unit =
    w(s"Leave already sorted: $node", level)

  override def onResultSorted(sorted: Traversable[N]): Unit =
    w(s"SORTED: ${sorted.mkString(", ")}", 0)

  override def onResultCycle(path: Traversable[N]): Unit =
    w(s"CYCLE: ${path.mkString(", ")}", 0)
}

object PrintStreamListener {
  def StdOut[N] = new PrintStreamListener[N](System.out)
  def StdErr[N] = new PrintStreamListener[N](System.err)
}
