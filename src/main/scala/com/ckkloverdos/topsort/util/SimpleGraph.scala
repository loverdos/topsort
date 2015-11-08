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
import com.ckkloverdos.topsort.util.SimpleGraph.LSet
import com.ckkloverdos.topsort.{GraphStructure, TopSort, TopSortResult}

import scala.language.implicitConversions

object SimpleGraph {
  class LSet[N] private(
    private[util] val set: Set[N],
    private[util] val ordered: Vector[N]
  ) {
    def +(n: N): LSet[N] =
      if(set(n))
        this
      else
        new LSet(set + n, ordered :+ n)

    def ++(that: Seq[N]): LSet[N] = {
      var acc = this
      for {
        item ← that if !this(item)
      } {
        acc += item
      }
      acc
    }

    def ++(that: LSet[N]): LSet[N] = this ++ that.toSeq

    def -(n: N): LSet[N] =
      if(set(n))
        new LSet(set - n, ordered.filterNot(_ == n))
      else
        this

    def apply(n: N): Boolean = set(n)

    def foreach(f: N ⇒ Unit) = ordered.foreach(f)
    def map[S](f: N ⇒ S): LSet[S] = new LSet(set.map(f), ordered.map(f))
    def foldLeft[Z](initial: Z)(f: (Z, N) ⇒ Z): Z = ordered.foldLeft(initial)(f)

    def size = ordered.size

    def toSeq: collection.immutable.Seq[N] = ordered
    def iterator: Iterator[N] = ordered.iterator
  }

  object LSet {
    final def apply[N](): LSet[N] = new LSet[N](Set(), Vector())
  }

  final def apply[N](): SimpleGraph[N] = new SimpleGraph[N](Map(), LSet())
}

/**
 * A simple immutable graph representation as a map of nodes to its dependencies.
 */
final class SimpleGraph[N] private[util](
  private[util] val map: Map[N, LSet[N]],
  private[util] val keys: LSet[N]
) {
  private def get(from: N): LSet[N] = map.getOrElse(from, LSet())

  def +(fromto: (N, N)): SimpleGraph[N] = {
    val (from, to) = fromto
    val newSet = get(from) + to
    val newMap = map.updated(from, newSet)
    val newKeys = keys + from

    new SimpleGraph(newMap, newKeys)
  }

  def dependenciesOf(node: N): LSet[N] = get(node)

  def +(a: N): SimpleGraph[N] =
    if(map.contains(a))
      this
    else {
      val newMap = map.updated(a, LSet())
      val newKeys = keys + a
      new SimpleGraph(newMap, newKeys)
    }

  def ++(that: SimpleGraph[N]): SimpleGraph[N] = {
    val keys = this.keys ++ that.keys
    val fromTos: LSet[(N, LSet[N])] = // all the keys and their concatenated LSet values
      for(key ← keys) yield {
        val thisSet = this.get(key)
        val thatSet = that.get(key)

        (key, thisSet ++ thatSet)
      }

    val newKeys = fromTos.map(_._1)
    val newMap  = Map(fromTos.toSeq:_*)

    new SimpleGraph(newMap, newKeys)
  }

  def ++(fromto: (N, LSet[N])): SimpleGraph[N] = {
    val (from, to) = fromto
    val that = new SimpleGraph(Map(from → to), LSet() + from)

    this ++ that
  }

  def allNodes: LSet[N] = {
    var acc = keys
    for {
      from ← keys
    } {
      val tos = map(from)
      acc ++= tos
    }
    acc
  }

  def topSort(listener: TopSortListener[N]): Boolean = TopSort.sort(GraphStructure, this, listener)

  def topSortResult(listener: TopSortListener[N] = TopSortListener.NoOpListener[N]): TopSortResult[N] =
    TopSort.sortResult(GraphStructure, this, listener)

  def topSortEx(listener: TopSortListener[N] = TopSortListener.NoOpListener[N]): Traversable[N] =
    TopSort.sortEx(GraphStructure, this, listener)

  object GraphStructure extends GraphStructure[SimpleGraph[N], N] {
    def nodes(graph: SimpleGraph[N]): Iterator[N] = graph.allNodes.iterator

    def nodeDependencies(graph: SimpleGraph[N], node: N): Iterator[N] =
      graph.dependenciesOf(node).iterator
  }
}

