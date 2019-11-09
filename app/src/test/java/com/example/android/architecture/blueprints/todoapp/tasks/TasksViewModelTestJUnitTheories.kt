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
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
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

    private val dataLoadingObserver: Observer<Boolean> = mock()

    private val itemsObserver: Observer<List<Task>> = mock()

    private val newTaskEventObserver: Observer<Event<Unit>> = mock()

    private val noTaskIconResObserver: Observer<Int> = mock()

    private val noTasksLabelObserver: Observer<Int> = mock()

    private val openTaskEventObserver: Observer<Event<String>> = mock()

    private val tasksAddViewVisible: Observer<Boolean> = mock()

    private val snackbarObserver: Observer<Event<Int>> = mock()

    private val currentFilteringLabel: Observer<Int> = mock()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        tasksRepository = FakeRepository()
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        tasksRepository.addTasks(task1, task2, task3)

        tasksViewModel = TasksViewModel(tasksRepository)

        tasksViewModel.dataLoading.observeForever(dataLoadingObserver)
        tasksViewModel.items.observeForever(itemsObserver)
        tasksViewModel.newTaskEvent.observeForever(newTaskEventObserver)
        tasksViewModel.noTaskIconRes.observeForever(noTaskIconResObserver)
        tasksViewModel.noTasksLabel.observeForever(noTasksLabelObserver)
        tasksViewModel.openTaskEvent.observeForever(openTaskEventObserver)
        tasksViewModel.tasksAddViewVisible.observeForever(tasksAddViewVisible)
        tasksViewModel.snackbarText.observeForever(snackbarObserver)
        tasksViewModel.currentFilteringLabel.observeForever(currentFilteringLabel)
    }

    @After
    fun tearDown() {
        tasksViewModel.dataLoading.removeObserver(dataLoadingObserver)
        tasksViewModel.items.removeObserver(itemsObserver)
        tasksViewModel.newTaskEvent.removeObserver(newTaskEventObserver)
        tasksViewModel.noTaskIconRes.removeObserver(noTaskIconResObserver)
        tasksViewModel.noTasksLabel.removeObserver(noTasksLabelObserver)
        tasksViewModel.openTaskEvent.removeObserver(openTaskEventObserver)
        tasksViewModel.tasksAddViewVisible.observeForever(tasksAddViewVisible)
        tasksViewModel.snackbarText.removeObserver(snackbarObserver)
        tasksViewModel.currentFilteringLabel.removeObserver(currentFilteringLabel)
    }

    companion object {

        val LOAD: Action = { loadTasks(forceUpdate = true) }
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
            listOf(FILTER_COMPLETE, LOAD, FILTER_ALL),
            listOf(FILTER_ALL, LOAD, CLEAR_COMPLETED)
        )
    }

    @Theory
    fun dataLoadingAlwaysShownFirst(actions: Actions) {
        actions.forEach { it(tasksViewModel) }

        assertThat(
            dataLoadingObserver
                .observed()
                .take(3)
        ).isEqualTo(
            listOf(
                false, // uninitialized
                true, // loading
                false // loaded
            )
        )
    }

    @Theory
    fun noActiveItemsWhenSetToFilterComplete(actions: Actions) {
        actions.forEach { it(tasksViewModel) }

        assumeThat(currentFilteringLabel.observed().last()).isEqualTo(R.string.label_completed)

        assertThat(itemsObserver.observed().last().any { it.isActive }).isFalse()
    }

    @Theory
    fun noCompletedItemsWhenSetToFilterActive(actions: Actions) {
        actions.forEach { it(tasksViewModel) }

        assumeThat(currentFilteringLabel.observed().last()).isEqualTo(R.string.label_active)

        assertThat(itemsObserver.observed().last().any { it.isCompleted }).isFalse()
    }
}