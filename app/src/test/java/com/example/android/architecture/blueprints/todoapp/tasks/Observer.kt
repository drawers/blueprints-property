package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.todoapp.Event
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.verify

/**
 * Returns all values passed to a mocked [Observer]
 *
 * Example:
 *
 * ```
 * @Mock
 * lateinit var viewStateObserver : Observer<PetViewState>
 *
 * @Test
 * fun `puppies`() {
 *  // arrange
 *
 *  // act
 *
 *  // assert
 *  viewStateObserver.observed().last() shouldEqual Puppies()
 * }
 * ```
 * @param T the type parameter of the [Observer]
 *
 */
inline fun <reified T : Any> Observer<T>.observed(): List<T> {
    argumentCaptor<T>().apply {
        verify(this@observed, atLeastOnce()).onChanged(capture())
        return allValues
    }
}

/**
 * Returns the last value passed to a mocked [Observer]
 */
inline fun <reified T : Any> Observer<T>.lastValue(): T {
    argumentCaptor<T>().apply {
        verify(this@lastValue, atLeastOnce()).onChanged(capture())
        return lastValue
    }
}

/**
 * Flattens a [Collection] of [Event] with type T into a Collection of T
 *
 *
 * Example:
 * ```
 * @Mock
 * lateinit var viewEventObserver : Observer<Event<PetViewEvent>>
 *
 * @Test
 * fun `kittens`() {
 *  // arrange
 *
 *  // act
 *
 *  // assert
 *  viewEventObserver.observed().contents().last() shouldEqual PetViewEvent.Kittens
 * }
 * ```
 * @param [T] the type parameter of the [Event] whose contents are to be exposed
 */
inline fun <reified T : Any> Collection<Event<T>>.contents(): Collection<T> {
    return this.map {
        it.peekContent()
    }
}