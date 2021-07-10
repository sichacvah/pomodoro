package com.sergey_kurochkin.core

fun <E> Iterable<E>.updatedAt(index: Int, elem: E) = mapIndexed { i, existing ->  if (i == index) elem else existing }
