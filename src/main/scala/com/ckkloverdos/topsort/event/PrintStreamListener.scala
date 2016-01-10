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
 * A [[com.ckkloverdos.topsort.event.TopSortListener TopSortListener]]
 * that prints events to a [[java.io.PrintStream PrintStream]], optionally
 * transforming the graph nodes to a custom string representation.
 */
class PrintStreamListener[N](
  protected val out: PrintStream,
  override protected val f: (N) ⇒ String = (node: N) ⇒ node.toString
) extends PrintListener[N](out.println(_:String), f)

object PrintStreamListener {
  def StdOut[N] = new PrintStreamListener[N](System.out)
  def StdErr[N] = new PrintStreamListener[N](System.err)
}
