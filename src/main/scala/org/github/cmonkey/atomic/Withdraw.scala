package org.github.cmonkey.atomic

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec

class Withdraw{

  @tailrec
  final def withdraw(account: AtomicReference[Int], amount: Int): Boolean = {
    val balance = account.get()
    if(amount <= balance){
      val updateBalance = balance - amount
      if(account.compareAndSet(balance, updateBalance)){
        true
      }else{
        withdraw(account, amount)
      }
    }else{
      false
    }
  }
}
