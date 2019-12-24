/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

typealias Action = TasksViewModel.() -> Unit
typealias Actions = List<Action>

/**
 * Unit tests for the implementation of [TasksViewModel]
 */
@ExperimentalCoroutinesApi
@RunWith(Theories::class)
class TasksViewModelTestJUnitTheories {

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository

    private val itemsObserver: Observer<List<Task>> = mock()

    private val snackbarObserver: Observer<Event<Int>> = mock()

    private val currentFilteringLabel: Observer<Int> = mock()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val dataLoadingObserver: Observer<Boolean> = mock()

    @Before
    fun setupViewModel() {
        tasksRepository = FakeRepository()

        tasksViewModel = TasksViewModel(tasksRepository)

        tasksViewModel.dataLoading.observeForever(dataLoadingObserver)

        tasksViewModel.items.observeForever(itemsObserver)
        tasksViewModel.snackbarText.observeForever(snackbarObserver)
        tasksViewModel.currentFilteringLabel.observeForever(currentFilteringLabel)
    }

    @After
    fun tearDown() {
        tasksViewModel.dataLoading.removeObserver(dataLoadingObserver)
        tasksViewModel.items.removeObserver(itemsObserver)
        tasksViewModel.snackbarText.removeObserver(snackbarObserver)
        tasksViewModel.currentFilteringLabel.removeObserver(currentFilteringLabel)
    }

    companion object {

        val LOAD: Action = { loadTasks(forceUpdate = false) }
        val FILTER_ALL: Action = { setFiltering(TasksFilterType.ALL_TASKS) }
        val FILTER_COMPLETE: Action = { setFiltering(TasksFilterType.COMPLETED_TASKS) }
        val FILTER_ACTIVE: Action = { setFiltering(TasksFilterType.ACTIVE_TASKS) }
        val CLEAR_COMPLETED: Action = { clearCompletedTasks() }

        @JvmField
        @DataPoints
        val actionPoints: List<Actions> = listOf(
            listOf(FILTER_ALL, LOAD),
            listOf(FILTER_COMPLETE, LOAD),
            listOf(FILTER_ACTIVE, LOAD),
            listOf(FILTER_COMPLETE, LOAD, FILTER_ALL, LOAD),
            listOf(FILTER_ALL, LOAD, CLEAR_COMPLETED)
        )
    }

    @Theory
    fun propertyThatShouldApply(actions: Actions) {

    }

    @Theory
    fun dataLoadingAlwaysShownFirst(actions: Actions) {
        actions.execute()

        assertThat(
            dataLoadingObserver
                .observed()
                .take(3)
        ).isEqualTo(
            listOf(
                false,// uninitialized
                true, // loading
                false // loaded
            )
        )
    }

    @Theory
    fun noActiveItemsWhenSetToFilterComplete(actions: Actions) {
        actions.execute()

        assumeThat(currentFilteringLabel.lastValue()).isEqualTo(R.string.label_completed)

        assertThat(itemsObserver.lastValue().none { it.isActive }).isTrue()
    }

    @Theory
    fun noCompletedItemsWhenSetToFilterActive(actions: Actions) {
        actions.execute()

        assumeThat(currentFilteringLabel.lastValue()).isEqualTo(R.string.label_active)

        assertThat(itemsObserver.lastValue().none { it.isCompleted }).isTrue()
    }

    @Theory
    fun noCompletedItemsAfterClearComplete(actions: Actions) {
        assumeThat(actions.last()).isSameAs(CLEAR_COMPLETED)

        actions.execute()

        assertThat(itemsObserver.lastValue().none { it.isCompleted }).isTrue()
    }

    @Theory
    fun snackbarMessageOnClearComplete(actions: Actions) {
        assumeThat(actions.last()).isSameAs(CLEAR_COMPLETED)

        actions.execute()

        assertThat(
            snackbarObserver.observed()
                .contents()
                .last()
        ).isEqualTo(
            R.string.completed_tasks_cleared
        )
    }

    private fun Actions.execute() {
        this.forEach { it(tasksViewModel) }
    }
}