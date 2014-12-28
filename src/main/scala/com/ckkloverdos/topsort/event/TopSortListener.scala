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
 *
 */
trait TopSortListener[N] {
  def onNewNode(node: N, level: Int): Unit = {}
  def onCycle(path: Traversable[N], level: Int): Unit = {}
  def onAddSearchPath(path: Traversable[N], lastAddition: N, level: Int): Unit = {}
  def onRemoveSearchPath(path: Traversable[N], lastRemoval: N, level: Int): Unit = {}
  def onAcceptSorted(node: N, level: Int): Unit = {}
  def onAlreadySorted(node: N, level: Int): Unit = {}
  def onResultSorted(sorted: Traversable[N]): Unit = {}
  def onResultCycle(path: Traversable[N]): Unit = {}
}


