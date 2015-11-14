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

  def foreach[U](f: N ⇒ U) = ordered.foreach(f)
  def map[S](f: N ⇒ S): LSet[S] = new LSet(set.map(f), ordered.map(f))
  def foldLeft[Z](initial: Z)(f: (Z, N) ⇒ Z): Z = ordered.foldLeft(initial)(f)

  def size = ordered.size

  def headOpt: Option[N] = ordered.headOption
  def toSeq: collection.immutable.Seq[N] = ordered
  def iterator: Iterator[N] = ordered.iterator
  def isEmpty: Boolean = ordered.isEmpty

  def mkString(sep: String) = ordered.mkString(sep)
  def mkString(start: String, sep: String, end: String) = ordered.mkString(start, sep, end)

  override def hashCode(): Int = ordered.hashCode()

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case that: LSet[_] ⇒ this.ordered == that.ordered
      case _ ⇒ false
    }

  override def toString: String = "LSet(" + ordered.mkString(", ") + ")"
}

object LSet {
  final def apply[N](items: N*): LSet[N] = new LSet[N](Set(), Vector()) ++ items
}
