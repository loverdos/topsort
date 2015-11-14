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

package com.ckkloverdos.topsort.util

import com.ckkloverdos.topsort.event.TopSortListener

import scala.collection.mutable

/**
  * Computes the topologically sorted dependencies per node.
  *
  * @tparam N
  */
abstract class TopSortedPerNodeSkeleton[N] extends TopSortListener[N] {
  // Called when a dependency is encountered
  protected def addDependency(dependent: N, dependency: N): Unit
  // Called when a node is added to the topologically sorted set
  protected def addSortedNode(node: N): Unit

  type Dependencies <: AnyRef
  protected def foreachNode[U](f: N ⇒ U): Unit

  protected def ifHasDependencies(node: N)(f: (N, Dependencies) ⇒ Unit): Unit

  // Inserts `frontDeps` as the first topsorted dependencies of `node`
  protected def insertFrontDependencies(node: N, frontDeps: Dependencies): Unit

  override def onAlreadySorted(dependents: List[N], node: N, level: Int): Unit = {
    // we keep adding pairs, since this time `node` may appear with more
    // `dependents` than in `onAddedToSorted`
    dependents.foreach(dependent ⇒ addDependency(dependent, node))
  }

  override def onAddedToSorted(dependents: List[N], node: N, level: Int): Unit = {
    dependents.foreach(dependent ⇒ addDependency(dependent, node))
    addSortedNode(node)
  }

  override def onResultSorted(sorted: Traversable[N]): Unit = {
    // There still may be cases where the full path to a node
    // has not been recorded, if members of the path have been
    // already been sorted.
    //
    // Let's use  'a' -> 'b' -> 'c' as an example, and assume that first we
    //      see and accept 'c' and the we see and accept 'b'. When we see 'a'
    //      and its immediate dependency 'b', we will not check the dependencies
    //      further since 'b' is already sorted. So we miss 'c' as a transitive
    //      dependency of 'a', meaning that 'c' will not have a chance to see 'a'
    //      in the `dependents` list in one of the listener methods.
    //
    // We mitigate the above by post-processing the topsorted nodes and their
    // respective (so far computed) dependencies.
    //
    //////////////////////////////////////////////////////////
    // The following graph exhibits the problematic behavior.
    // The offending node is /c::fatjar
    //
    //  /::fatjar   → /a::jar, /c::jar, /b::jar
    //  /a::jar     → /a::compile
    //  /b::jar     → /b::compile
    //  /c::jar     → /c::compile
    //  /c::compile → /b::compile, /a::compile
    //  /c::fatjar  → /c::jar
    //
    // The topologically sorted nodes are:
    //
    // [/a::compile, /a::jar, /b::compile, /c::compile, /c::jar, /b::jar, /::fatjar, /c::fatjar]
    //
    // And without the following code, the topsorted lists per node are:
    //
    // /a::jar → /a::compile
    // /::fatjar → /a::compile, /a::jar, /b::compile, /c::compile, /c::jar, /b::jar
    // /a::compile →
    // /c::compile → /b::compile, /a::compile
    // /c::jar → /b::compile, /a::compile, /c::compile
    // /b::compile →
    // /b::jar → /b::compile
    // /c::fatjar → /c::jar
    //
    // Note how the last dependency description is erroneous, since /c::jar
    //      already has its own dependencies, which should have appeared
    //      in the dependency list of /c::fatjar as well.
    //
    ////////////////////////////////////////////////

    // Let's use the example of
    //
    //    /c::fatjar → /c::jar
    //    /c::jar → /b::compile, /a::compile, /c::compile
    //
    // where /c::fatjar is te dependent and /c::jar is its first dependency
    foreachNode { dependent ⇒ // /c::fatjar
      ifHasDependencies(dependent) { // → [/c::jar]
        case (first /* /c::jar */, all /* [/c::jar] */) ⇒
          ifHasDependencies(first) { // → [/b::compile, /a::compile, /c::compile]
            case (_, dependenciesOfFirst) ⇒
              insertFrontDependencies(dependent, dependenciesOfFirst)
          }
      }
    }
  }
}

final class LTopSortPerNode[N] extends TopSortedPerNodeSkeleton[N] {
  private[this] var lmap = LMap[N]()

  protected def addDependency(dependent: N, dependency: N): Unit = lmap += dependent → dependency
  protected def addSortedNode(node: N): Unit = lmap += node

  type Dependencies = LSet[N]
  protected def foreachNode[U](f: N ⇒ U): Unit = lmap.keySet.foreach(f)

  protected def ifHasDependencies(node: N)(f: (N, Dependencies) ⇒ Unit): Unit =
    for {
      dependencies ← lmap.get(node)
      first ← dependencies.headOpt
    } {
      f(first, dependencies)
    }

  // Inserts `frontDeps` as the first topsorted dependencies of `node`
  protected def insertFrontDependencies(node: N, frontDeps: LSet[N]): Unit = {
    val currentDeps = lmap(node) // If we reach here, we always have a mapping
    val newDeps = frontDeps ++ currentDeps
    lmap = lmap.updated(node, newDeps)
  }

  def topSortedMap: LMap[N] = lmap
}

class MLTopSortPerNode[N] extends TopSortedPerNodeSkeleton[N] {
  // mutable linked map
  private[this] final val mlmap = new mutable.LinkedHashMap[N, mutable.LinkedHashSet[N]]

  private[this] def get(n: N) =
    if(mlmap.contains(n))
      mlmap(n)
    else {
      val value = new mutable.LinkedHashSet[N]
      mlmap(n) = value
      value
    }

  private[this] def ensure(n: N) = get(n)

  protected def addDependency(dependent: N, dependency: N): Unit = get(dependent) += dependency
  protected def addSortedNode(node: N): Unit = ensure(node)

  type Dependencies = mutable.LinkedHashSet[N]
  protected def foreachNode[U](f: N ⇒ U): Unit = mlmap.keySet.foreach(f)

  protected def ifHasDependencies(node: N)(f: (N, Dependencies) ⇒ Unit): Unit =
    for {
      dependencies ← mlmap.get(node)
      first ← dependencies.headOption
    } {
      f(first, dependencies)
    }

  protected def insertFrontDependencies(node: N, frontDeps: mutable.LinkedHashSet[N]): Unit = {
    val currentDeps = mlmap(node)
    val newDeps = new mutable.LinkedHashSet[N]
    newDeps ++= frontDeps
    newDeps ++= currentDeps
    mlmap(node) = newDeps
  }

  def topSortedMap: mutable.LinkedHashMap[N, mutable.LinkedHashSet[N]] = mlmap
}
