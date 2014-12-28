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
class ResultListener[N](onResult: (TopSortResult[N]) ⇒ Unit) extends TopSortListener[N] {
  override def onResultSorted(sorted: Traversable[N]) = onResult(TopSortOk(sorted))
  override def onResultCycle(path: Traversable[N]) = onResult(TopSortCycle(path))
}
