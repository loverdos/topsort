/*
 * Copyright (c) 2013-2014 Christos KK Loverdos
 */

package com.ckkloverdos.topsort.event

/**
 * A [[com.ckkloverdos.topsort.event.TopSortListener TopSortListener]]
 * that prints events using a printer function, optionally
 * transforming the graph nodes to a custom string representation.
 */
class PrintListener[N](
  protected val p: (String) ⇒ Unit,
  protected val f: (N) ⇒ String = (node: N) ⇒ node.toString
) extends TopSortListener[N] {
  protected def w(s: String, level: Int) = p((" " * (level * 2)) + s)

  protected def d(dependents: List[N]) =
    dependents match {
      case Nil ⇒ ""
      case _ ⇒ s" (← ${dependents.map(f).mkString(" ← ")})"
    }

  protected def p(path: Traversable[N]) = path.map(f).mkString(" → ")

  protected def s(sorted: Traversable[N]) = sorted.map(f).mkString(", ")

  def copy(p: (String) ⇒ Unit = this.p, f: (N) ⇒ String = this.f) =
    new PrintListener(p, f)

  override def onCycle(path: Traversable[N], level: Int) =
    w(s"Cycle: ${p(path)}", level)

  override def onAddedToSearchPath(path: Traversable[N], dependents: List[N], addedNode: N, level: Int) =
    w(s"Search path (+ ${f(addedNode)}): ${p(path)}", level)

  override def onRemovedFromSearchPath(path: Traversable[N], removedNode: N, level: Int) =
    w(s"Search path (- ${f(removedNode)}): ${p(path)}", level)

  override def onAddedToSorted(dependents: List[N], node: N, level: Int) =
    w(s"ACCEPT <${f(node)}>${d(dependents)}", level)

  override def onAlreadySorted(dependents: List[N], node: N, level: Int): Unit =
    w(s"Leave already sorted: ${f(node)}", level)

  override def onResultSorted(sorted: Traversable[N]): Unit =
    w(s"SORTED: ${s(sorted)}", 0)

  override def onResultCycle(path: Traversable[N]): Unit =
    w(s"CYCLE: ${p(path)}", 0)
}


