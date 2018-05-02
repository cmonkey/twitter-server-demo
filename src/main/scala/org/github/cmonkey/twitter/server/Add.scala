package org.github.cmonkey.twitter.server

trait Add[T]{
  def add(a: T, b: T): T
}

object Add extends App {
  implicit object AddInt extends Add[Int]{
    override def add(a: Int, b: Int): Int = {
      a + b
    }
  }

  def adder[T:Add](value:T):T => T = implicitly[Add[T]].add(value, _)

  val addOne = adder(1)

  println(addOne(2))
  //println((adder(1)(2))
}

