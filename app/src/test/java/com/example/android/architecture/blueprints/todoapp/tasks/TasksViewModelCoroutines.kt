package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.*
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainInOrder
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.properties.Gen
import io.kotlintest.properties.PropertyContext
import io.kotlintest.properties.PropertyTesting
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
class Coroutines : StringSpec() {

    private val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private lateinit var tasksViewModel: TasksViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository

    private lateinit var viewModelContext: ViewModelContext

    private val dataLoadingObserver: Observer<Boolean> = mock()

    private val itemsObserver: Observer<List<Task>> = mock()

    private val snackbarEventObserver: Observer<Event<Int>> = mock()

    private val addTaskEventObserver: Observer<Event<Unit>> = mock()

    private val openTaskEventObserver: Observer<Event<String>> = mock()

    private val currentFilteringLabelObserver: Observer<Int> = mock()


    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) {
        super.beforeSpecClass(spec, tests)
        PropertyTesting.shouldPrintGeneratedValues = true
        Dispatchers.setMain(executor)
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
        println("beforeTest: ${testCase.description}")
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
        tasksViewModel.currentFilteringLabel.observeForever(currentFilteringLabelObserver)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)
        tasksViewModel.dataLoading.removeObserver(dataLoadingObserver)
        tasksViewModel.items.removeObserver(itemsObserver)
        tasksViewModel.snackbarText.removeObserver(snackbarEventObserver)
        tasksViewModel.newTaskEvent.removeObserver(addTaskEventObserver)
        tasksViewModel.openTaskEvent.removeObserver(openTaskEventObserver)
    }

    private fun assertAll(block: PropertyContext.() -> Unit) {
        assertAll(10, Gen.list(Gen.action())) { actions ->
            actions.execute()
            apply {
                block()
            }
        }
    }

    init {
        "dataLoading off after loaded" {
            assertAll {
                dataLoadingObserver.observed().take(3).shouldContainInOrder(listOf(true, false, true))
            }
        }

        "item count never exceeds number of tasks in repository" {
            assertAll {
                itemsObserver.observed().last().size shouldBeLessThanOrEqual tasksRepository.tasksServiceData.size
            }
        }


        "item titles match those in repository" {
            assertAll {
                itemsObserver.observed().last().forEach {
                    it.title shouldBe tasksRepository.tasksServiceData[it.id]!!.title
                }
            }
        }

        "item descriptions match those in repository" {
            assertAll {
                itemsObserver.observed().last().forEach {
                    it.description shouldBe tasksRepository.tasksServiceData[it.id]!!.description
                }
            }
        }

        "item status match those in repository" {
            assertAll {
                itemsObserver.observed().last().forEach {
                    it.isActive shouldBe tasksRepository.tasksServiceData[it.id]!!.isActive
                    it.isCompleted shouldBe tasksRepository.tasksServiceData[it.id]!!.isCompleted
                }
            }
        }

        "no completed items when filter set to active" {
            assertAll {
                if (currentFilteringLabelObserver.lastValue() == R.string.label_active) {
                    itemsObserver.lastValue().any { it.isCompleted }.shouldBeTrue()
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
    }

    private fun List<Action>.execute() {
        forEach {
            it.body.invoke(viewModelContext)
        }
    }
}