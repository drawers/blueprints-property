package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainInOrder
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.properties.Gen
import io.kotlintest.properties.PropertyContext
import io.kotlintest.properties.PropertyTesting
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class TasksViewModelTest : StringSpec() {

    private lateinit var tasksViewModel: TasksViewModel

    private lateinit var tasksRepository: FakeRepository

    private lateinit var tasksContext: TasksContext

    private val dataLoadingObserver: Observer<Boolean> = mock()

    private val itemsObserver: Observer<List<Task>> = mock()

    private val snackbarEventObserver: Observer<Event<Int>> = mock()

    private val addTaskEventObserver: Observer<Event<Unit>> = mock()

    private val openTaskEventObserver: Observer<Event<String>> = mock()

    private val currentFilteringLabelObserver: Observer<Int> = mock()

    private val taskAddViewVisibleObserver: Observer<Boolean> = mock()

    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) {
        super.beforeSpecClass(spec, tests)
        PropertyTesting.shouldPrintGeneratedValues = true
        Dispatchers.setMain(TestCoroutineDispatcher())
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
        super.afterSpecClass(spec, results)
        ArchTaskExecutor.getInstance().setDelegate(null)
        Dispatchers.resetMain()
        super.afterSpecClass(spec, results)
    }

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        tasksRepository = FakeRepository()
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        tasksRepository.addTasks(task1, task2, task3)
        tasksViewModel = TasksViewModel(tasksRepository)
        tasksContext = TasksContext(tasksViewModel, tasksRepository)


        tasksViewModel.dataLoading.observeForever(dataLoadingObserver)
        tasksViewModel.items.observeForever((itemsObserver))
        tasksViewModel.snackbarText.observeForever(snackbarEventObserver)
        tasksViewModel.newTaskEvent.observeForever(addTaskEventObserver)
        tasksViewModel.openTaskEvent.observeForever(openTaskEventObserver)
        tasksViewModel.currentFilteringLabel.observeForever(currentFilteringLabelObserver)
        tasksViewModel.tasksAddViewVisible.observeForever(taskAddViewVisibleObserver)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)
        tasksViewModel.dataLoading.removeObserver(dataLoadingObserver)
        tasksViewModel.items.removeObserver(itemsObserver)
        tasksViewModel.snackbarText.removeObserver(snackbarEventObserver)
        tasksViewModel.newTaskEvent.removeObserver(addTaskEventObserver)
        tasksViewModel.openTaskEvent.removeObserver(openTaskEventObserver)
        tasksViewModel.currentFilteringLabel.removeObserver(currentFilteringLabelObserver)
        tasksViewModel.tasksAddViewVisible.observeForever(taskAddViewVisibleObserver)
    }

    init {
        "dataLoading off after loaded" {
            assertAll {
                dataLoadingObserver.observed().take(3).shouldContainInOrder(
                        listOf(
                                false, // uninitialized
                                true, // loading
                                false // loaded
                        )
                )
            }
        }

        "no completed items when filter set to active" {
            assertAll {
                if (currentFilteringLabelObserver.lastValue() == R.string.label_active) {
                    itemsObserver.lastValue().none { it.isCompleted }.shouldBeTrue()
                }
            }
        }

        "no active items when filter set to complete" {
            assertAll {
                if (currentFilteringLabelObserver.lastValue() == R.string.label_completed) {
                    itemsObserver.lastValue().none { it.isActive }.shouldBeTrue()
                }
            }
        }

        "item count never exceeds number of tasks in repository" {
            assertAll {
                itemsObserver.observed().last().size shouldBeLessThanOrEqual tasksRepository.tasksServiceData.size
            }
        }


        "items mapped correctly from those in repository" {
            assertAll {
                itemsObserver.observed().last().forEach {
                    val task = tasksRepository.tasksServiceData[it.id]!!

                    it.title shouldBe task.title
                    it.description shouldBe task.description
                    it.isActive shouldBe task.isActive
                    it.isCompleted shouldBe task.isCompleted
                }
            }
        }

        "load error shows snackbar" {
            assertAll(
                    listOf(
                            LoadError
                    ),
                    listOf(
                            Load,
                            AddNewTask,
                            LoadError
                    )
            ) {
                snackbarEventObserver.observed().contents().last() shouldBe R.string.loading_tasks_error
            }
        }

        "clicking on FAB shows addTaskUI" {
            assertAll(
                    listOf(
                            Load,
                            AddNewTask
                    )
            ) {
                addTaskEventObserver.observed().contents().last().shouldNotBeNull()
            }
        }

        "clicking on open task sends event" {
            assertAll(
                    listOf(
                            Load,
                            OpenTask
                    )
            ) {
                openTaskEventObserver.observed().contents().last().shouldBe(OpenTask.OPEN_TASK_ID)
            }
        }

        "clear completed tasks clears tasks" {
            assertAll(
                    listOf(
                            Load,
                            ClearCompleted
                    )
            ) {
                itemsObserver.observed().last().any { it.isCompleted }.shouldBeFalse()
                snackbarEventObserver.observed().contents().last() shouldBe R.string.completed_tasks_cleared
            }
        }

        "edit okay updates snackbar" {
            assertAll(
                    listOf(
                            Load,
                            ShowEditResultMessage
                    )
            ) {
                snackbarEventObserver.observed().contents().last() shouldBe R.string.successfully_saved_task_message
            }
        }

        "add okay updates snackbar" {
            assertAll(
                    listOf(
                            Load,
                            ShowAddEditResultMessage
                    )
            ) {
                snackbarEventObserver.observed().contents().last() shouldBe R.string.successfully_added_task_message
            }
        }


        "delete okay updates snackbar" {
            assertAll(
                    listOf(
                            Load,
                            ShowDeleteOkMessage
                    )
            ) {
                snackbarEventObserver.observed().contents().last() shouldBe R.string.successfully_deleted_task_message
            }
        }

        "mark task complete updates data and snackbar" {
            assertAll(
                    listOf(
                            Load,
                            CompleteTask
                    )
            ) {
                tasksRepository.tasksServiceData[SAMPLE_TASK.id]!!.isCompleted.shouldBeTrue()
                snackbarEventObserver.observed().contents().last() shouldBe R.string.task_marked_complete
            }
        }

        "mark task active updates data and snackbar"{
            assertAll(
                    listOf(
                            Load,
                            ActivateTask
                    )
            ) {
                tasksRepository.tasksServiceData[SAMPLE_TASK_COMPLETE.id]!!.isActive.shouldBeTrue()
                snackbarEventObserver.observed().contents().last() shouldBe R.string.task_marked_active
            }
        }

        "add view visible when filter is all tasks" {
            assertAll {
                if (currentFilteringLabelObserver.lastValue() == R.string.label_all) {
                    taskAddViewVisibleObserver.lastValue().shouldBeTrue()
                }
            }
        }
    }

    private fun assertAll(vararg actions: List<Action>, block: PropertyContext.() -> Unit) {
        assertAll(iterations = 1, gena = Gen.from(actions)) { genActions ->
            genActions.forEach {
                it.body(tasksContext)
            }

            genActions.apply {
                block()
            }
        }
    }


    private fun assertAll(block: PropertyContext.() -> Unit) {
        assertAll(
                iterations = 10,
                gena = Gen.list(Gen.action())
        )
        { actions ->
            actions.forEach {
                it.body(tasksContext)
            }

            apply {
                block()
            }
        }
    }
}