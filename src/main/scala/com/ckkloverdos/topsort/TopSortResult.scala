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
