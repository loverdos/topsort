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

package com.ckkloverdos.topsort.event;

/**
 * TopSort enters and exits the graph nodes, trying to do dependency discovery
 * and the actual topological sorting. This enumeration specifies the cause
 * for exiting a node.
 */
public enum ExitCause {
  /**
   * The `node` has already been topsorted and, as a consequence, no further
   * processing will take place regarding the `node` and its dependencies.
   * This means the `node` (and its dependencies) has been successfully
   * searched in the past and no cycles have been detected.
   */
  AlreadySorted,

  /**
   * The `node` is part of a cycle and, as a consequence, the
   * topologically sorting procedure is about to end.
   */
  Cycle,

  /**
   * One of the `node` dependencies is part of a cycle and, as a consequence, the
   * topologically sorting procedure is about to end.
   */
  DependencyCycle,

  /**
   * The `node` has been added to the collection of the already sorted nodes.
   * This method is called at most once for every node in the graph.
   * It is called exactly once for each node in the graph if the graph can be
   * topologically sorted, that is it does not contain a cycle.
   *
   * <p/>
   * Do not rely on this method only in order to record the dependents of a node, since
   * the dependents list can be empty. Why it may be empty is related to the graph structure
   * that provides the nodes in the first place.
   */
  Sorted
}
