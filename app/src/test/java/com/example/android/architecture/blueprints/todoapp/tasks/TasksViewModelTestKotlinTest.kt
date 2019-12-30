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

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ACTIVE_TASKS
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.COMPLETED_TASKS
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.*
import io.kotlintest.extensions.SpecLevelExtension
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainInOrder
import io.kotlintest.properties.Gen
import io.kotlintest.properties.PropertyTesting
import io.kotlintest.properties.assertAll
import io.kotlintest.properties.filterIsInstance
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.concurrent.Executors
import kotlin.coroutines.ContinuationInterceptor
import kotlin.random.Random

/**
 * Unit tests for the implementation of [TasksViewModel]
 */
@ExperimentalCoroutinesApi
class TasksViewModelTestKotlinTest : StringSpec() {

    override val defaultTestCaseConfig: TestCaseConfig
        get() = super.defaultTestCaseConfig
    private val dataLoadingObserver: Observer<Boolean> = mock()

    private val itemsObserver: Observer<List<Task>> = mock()

    private val snackbarEventObserver: Observer<Event<Int>> = mock()

    private val addTaskEventObserver: Observer<Event<Unit>> = mock()

    private val openTaskEventObserver: Observer<Event<String>> = mock()

    private val shit = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository

    private lateinit var viewModelContext: ViewModelContext

    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) {
        super.beforeSpecClass(spec, tests)
        PropertyTesting.shouldPrintGeneratedValues = true
        println("beforeSpec")
        Dispatchers.setMain(shit)
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
               override fun executeOnDiskIO(runnable: Runnable) {
                runnable.run()
            }

            override fun postToMainThread(runnable: Runnable) {
                runnable.run()
            }

            override fun isMainThread(): Boolean {
                return true
            }
        })
    }

    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) {
        println("afterSpec")
        ArchTaskExecutor.getInstance().setDelegate(null)
        Dispatchers.resetMain()
        super.afterSpecClass(spec, results)
    }

    override fun beforeTest(testCase: TestCase) {
        println("beforeTest")
        super.beforeTest(testCase)
        tasksRepository = FakeRepository()
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        tasksRepository.addTasks(task1, task2, task3)
        tasksViewModel = TasksViewModel(tasksRepository)
        viewModelContext = ViewModelContext(tasksViewModel, tasksRepository)

        tasksViewModel.dataLoading.observeForever(dataLoadingObserver)
        tasksViewModel.items.observeForever((itemsObserver))
        tasksViewModel.snackbarText.observeForever(snackbarEventObserver)
        tasksViewModel.newTaskEvent.observeForever(addTaskEventObserver)
        tasksViewModel.openTaskEvent.observeForever(openTaskEventObserver)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        tasksViewModel.dataLoading.removeObserver(dataLoadingObserver)
        tasksViewModel.items.removeObserver(itemsObserver)
        tasksViewModel.snackbarText.removeObserver(snackbarEventObserver)
        tasksViewModel.newTaskEvent.removeObserver(addTaskEventObserver)
        tasksViewModel.openTaskEvent.removeObserver(openTaskEventObserver)

        super.afterTest(testCase, result)
    }


    init {

//        "dataLoading is false at completion of action" {
//            assertAll(Gen.list(actioa()))
//            {
//                tasksRepository = FakeRepository()
//                val task1 = Task("Title1", "Description1")
//                val task2 = Task("Title2", "Description2", true)
//                val task3 = Task("Title3", "Description3", true)
//                tasksRepository.addTasks(task1, task2, task3)
//                tasksViewModel = TasksViewModel(tasksRepository)
//                viewModelContext = ViewModelContext(tasksViewModel, tasksRepository)
//                it.forEach { action ->
//                    action.body.invoke(viewModelContext)
//                }
//
////                println(dataLoadingObserver.observed())
//                dataLoadingObserver.observed().take(2).shouldContainInOrder(listOf(true, false))
//            }
//        }


    }

}


//    @Test
//    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() {
//        // thanos snap
//        // Pause dispatcher so we can verify initial values
//
//        // Given an initialized TasksViewModel with initialized tasks
//        // When loading of Tasks is requested
//        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)
//
//        // Trigger loading of tasks
//        tasksViewModel.loadTasks(true)
//
//        // Then progress indicator is shown
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isTrue()
//
//        // Execute pending coroutines actions
////        mainCoroutineRule.resumeDispatcher()
//
//        // Then progress indicator is hidden
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()
//
//        // And data correctly loaded
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(3)
//    }
//
//    @Test
//    fun loadActiveTasksFromRepositoryAndLoadIntoView() {
//        // Given an initialized TasksViewModel with initialized tasks
//        // When loading of Tasks is requested
//        tasksViewModel.setFiltering(ACTIVE_TASKS)
//
//        // Load tasks
//        tasksViewModel.loadTasks(true)
//
//        // Then progress indicator is hidden
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()
//
//        // And data correctly loaded
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(1)
//    }
//
//    @Test
//    fun loadCompletedTasksFromRepositoryAndLoadIntoView() {
//        // Given an initialized TasksViewModel with initialized tasks
//        // When loading of Tasks is requested
//        tasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)
//
//        // Load tasks
//        tasksViewModel.loadTasks(true)
//
//        // Then progress indicator is hidden
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()
//
//        // And data correctly loaded
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(2)
//    }
//
//    @Test
//    fun loadTasks_error() {
//        // Make the repository return errors
//        tasksRepository.setReturnError(true)
//
//        // Load tasks
//        tasksViewModel.loadTasks(true)
//
//        // Then progress indicator is hidden
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()
//
//        // And the list of items is empty
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).isEmpty()
//
//        // And the snackbar updated
//        assertSnackbarMessage(tasksViewModel.snackbarText, R.string.loading_tasks_error)
//    }
//
//    @Test
//    fun clickOnFab_showsAddTaskUi() {
//        // When adding a new task
//        tasksViewModel.addNewTask()
//
//        // Then the event is triggered
//        val value = LiveDataTestUtil.getValue(tasksViewModel.newTaskEvent)
//        assertThat(value.getContentIfNotHandled()).isNotNull()
//    }
//
//    @Test
//    fun clickOnOpenTask_setsEvent() {
//        // When opening a new task
//        val taskId = "42"
//        tasksViewModel.openTask(taskId)
//
//        // Then the event is triggered
//        assertLiveDataEventTriggered(tasksViewModel.openTaskEvent, taskId)
//    }
//
//    @Test
//    fun clearCompletedTasks_clearsTasks() = /*mainCoroutineRule.runBlockingTest */run {
//        // When completed tasks are cleared
//        tasksViewModel.clearCompletedTasks()
//
//        // Fetch tasks
//        tasksViewModel.loadTasks(true)
//
//        // Fetch tasks
//        val allTasks = LiveDataTestUtil.getValue(tasksViewModel.items)
//        val completedTasks = allTasks.filter { it.isCompleted }
//
//        // Verify there are no completed tasks left
//        assertThat(completedTasks).isEmpty()
//
//        // Verify active task is not cleared
//        assertThat(allTasks).hasSize(1)
//
//        // Verify snackbar is updated
//        assertSnackbarMessage(
//                tasksViewModel.snackbarText, R.string.completed_tasks_cleared
//        )
//    }
//
//    @Test
//    fun showEditResultMessages_editOk_snackbarUpdated() {
//        // When the viewmodel receives a result from another destination
//        tasksViewModel.showEditResultMessage(EDIT_RESULT_OK)
//
//        // The snackbar is updated
//        assertSnackbarMessage(
//                tasksViewModel.snackbarText, R.string.successfully_saved_task_message
//        )
//    }
//
//    @Test
//    fun showEditResultMessages_addOk_snackbarUpdated() {
//        // When the viewmodel receives a result from another destination
//        tasksViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)
//
//        // The snackbar is updated
//        assertSnackbarMessage(
//                tasksViewModel.snackbarText, R.string.successfully_added_task_message
//        )
//    }
//
//    @Test
//    fun showEditResultMessages_deleteOk_snackbarUpdated() {
//        // When the viewmodel receives a result from another destination
//        tasksViewModel.showEditResultMessage(DELETE_RESULT_OK)
//
//        // The snackbar is updated
//        assertSnackbarMessage(
//                tasksViewModel.snackbarText, R.string.successfully_deleted_task_message
//        )
//    }
//
//    @Test
//    fun completeTask_dataAndSnackbarUpdated() {
//        // With a repository that has an active task
//        val task = Task("Title", "Description")
//        tasksRepository.addTasks(task)
//
//        // Complete task
//        tasksViewModel.completeTask(task, true)
//
//        // Verify the task is completed
//        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted).isTrue()
//
//        // The snackbar is updated
//        assertSnackbarMessage(
//                tasksViewModel.snackbarText, R.string.task_marked_complete
//        )
//    }
//
//    @Test
//    fun activateTask_dataAndSnackbarUpdated() {
//        // With a repository that has a completed task
//        val task = Task("Title", "Description", true)
//        tasksRepository.addTasks(task)
//
//        // Activate task
//        tasksViewModel.completeTask(task, false)
//
//        // Verify the task is active
//        assertThat(tasksRepository.tasksServiceData[task.id]?.isActive).isTrue()
//
//        // The snackbar is updated
//        assertSnackbarMessage(
//                tasksViewModel.snackbarText, R.string.task_marked_active
//        )
//    }
//
//    @Test
//    fun getTasksAddViewVisible() {
//        // When the filter type is ALL_TASKS
//        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)
//
//        // Then the "Add task" action is visible
//        assertThat(LiveDataTestUtil.getValue(tasksViewModel.tasksAddViewVisible)).isTrue()
//    }
//}

//internal fun actioa(): Gen<Action> = object : Gen<Action> {
//
//    override fun constants(): Iterable<Action> = Action::class.sealedSubclasses.map {
//        it.objectInstance!!
//    }
//
//    override fun random(): Sequence<Action> = generateSequence {
//        constants().drop(Random.nextInt(constants().count())).first()
//    }
//}
//
//internal sealed class Action(val body: ViewModelContext.() -> Unit)
//internal object Load : Action({ viewModel.loadTasks(forceUpdate = false) })
//internal object LoadForce : Action({ viewModel.loadTasks(forceUpdate = true) })
//internal object LoadError : Action({
//    repo.setReturnError(true)
//    viewModel.loadTasks(true)
//})
//
//internal object FilterActiveTasks : Action({
//    viewModel.setFiltering(ACTIVE_TASKS)
//})
//
//internal object FilterCompletedTasks : Action({
//    viewModel.setFiltering(COMPLETED_TASKS)
//})
//
//internal object FilterAllTasks : Action({
//    viewModel.setFiltering(COMPLETED_TASKS)
//})
//
//internal object AddNewTask : Action(
//        { viewModel.addNewTask() }
//)
//
//internal object OpenTask : Action(
//        { viewModel.openTask("42") }
//)
//
//
//internal object ClearCompleted : Action({
//    viewModel.clearCompletedTasks()
//    viewModel.loadTasks(true)
//}
//)
//
//internal object ShowEditResultMessage : Action({
//    viewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)
//})
//
//internal object ShowDeleteOkMessage : Action({
//    viewModel.showEditResultMessage(DELETE_RESULT_OK)
//})
//
//internal object CompleteTask : Action({
//    val task = Task(
//            title = "Title",
//            description = "Description"
//    )
//    repo.addTasks(
//            task
//    )
//    viewModel.completeTask(
//            task = task,
//            completed = true
//    )
//})
//
//internal object ActivateTask : Action({
//    val task = Task(
//            title = "Title",
//            description = "Description",
//            isCompleted = true
//    )
//    repo.addTasks(task)
//    viewModel.completeTask(task = task, completed = false)
//})
//
//internal class ViewModelContext(
//        val viewModel: TasksViewModel,
//        val repo: FakeRepository
//)
