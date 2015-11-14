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

class LMap[N] private[util](
  private[util] val map: Map[N, LSet[N]],
  private[util] val keys: LSet[N]
) {
  def contains(n: N) = map.contains(n)

  def +(fromto: (N, N)): LMap[N] = {
    val (from, to) = fromto
    val newSet = getOrEmpty(from) + to
    val newMap = map.updated(from, newSet)
    val newKeys = keys + from

    new LMap(newMap, newKeys)
  }

  def +(a: N): LMap[N] =
    if(map.contains(a))
      this
    else {
      val newMap = map.updated(a, LSet())
      val newKeys = keys + a
      new LMap(newMap, newKeys)
    }

  def ++(that: LMap[N]): LMap[N] = {
    val keys = this.keys ++ that.keys
    val fromTos: LSet[(N, LSet[N])] = // all the keys and their concatenated LSet values
      for(key ← keys) yield {
        val thisSet = this.getOrEmpty(key)
        val thatSet = that.getOrEmpty(key)

        (key, thisSet ++ thatSet)
      }

    val newKeys = fromTos.map(_._1)
    val newMap  = Map(fromTos.toSeq:_*)

    new LMap(newMap, newKeys)
  }

  def ++(fromto: (N, LSet[N])): LMap[N] = {
    val (from, to) = fromto
    val that = new LMap(Map(from → to), LSet(from))

    this ++ that
  }

  def updated(from: N, tos: LSet[N]): LMap[N] = {
    val newMap = map.updated(from, tos)
    val newKeys = if(map.contains(from)) keys else keys + from
    new LMap(newMap, newKeys)
  }

  def apply(n: N): LSet[N] = map(n)
  def get(n: N): Option[LSet[N]] = map.get(n)
  def getOrEmpty(n: N): LSet[N] = map.getOrElse(n, LSet())

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

  def keySet: LSet[N] = keys

  def size: Int = map.size

  def foreach[U](f: (N, LSet[N]) ⇒ U): Unit =
    for {
      dependent ← keys
    } {
      f(dependent, this(dependent))
    }

  override def hashCode(): Int = map.hashCode() * 31 + keys.hashCode()

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case that: LMap[_] ⇒ this.map == that.map
      case _ ⇒ false
    }

  override def toString: String = "LMap(" + keySet.map(k ⇒ s"$k → ${map(k)}").mkString(", ") + ")"
}

object LMap {
  def apply[N]() = new LMap[N](Map(), LSet())
}
