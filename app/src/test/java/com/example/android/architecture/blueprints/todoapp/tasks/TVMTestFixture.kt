package com.example.android.architecture.blueprints.todoapp.tasks

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.ListShrinker
import kotlin.random.Random

internal fun Gen.Companion.action(): Gen<Action> = object : Gen<Action> {

    override fun constants(): Iterable<Action> = Action::class.sealedSubclasses.map {
        it.objectInstance!!
    }

    override fun random(): Sequence<Action> = generateSequence {
        constants().drop(Random.nextInt(constants().count())).first()
    }
}

internal fun <T> Gen.Companion.smallList(gen: Gen<T>, int: Int): Gen<List<T>> = object : Gen<List<T>> {
    override fun constants(): Iterable<List<T>> = listOf(gen.constants().toList())
    override fun random(): Sequence<List<T>> = generateSequence {
        val size = Random.nextInt(int)
        gen.random().take(size).toList()
    }

    override fun shrinker() = ListShrinker<T>()
}

internal sealed class Action(val body: ViewModelContext.() -> Unit) {
    override fun toString(): String {
        return this::class.simpleName!!
    }
}

internal object Load : Action({ viewModel.loadTasks(forceUpdate = false) })
internal object LoadForce : Action({ viewModel.loadTasks(forceUpdate = true) })
internal object LoadError : Action({
    repo.setReturnError(true)
    viewModel.loadTasks(true)
})

internal object FilterActiveTasks : Action({
    viewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)
})

internal object FilterCompletedTasks : Action({
    viewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)
})

internal object FilterAllTasks : Action({
    viewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)
})

internal object AddNewTask : Action(
        { viewModel.addNewTask() }
)

internal object OpenTask : Action(
        { viewModel.openTask("42") }
)


//internal object ClearCompleted : Action(
//        {
//            viewModel.clearCompletedTasks()
//            viewModel.loadTasks(true)
//        }
//)

internal object ShowEditResultMessage : Action({
    viewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)
})

internal object ShowDeleteOkMessage : Action({
    viewModel.showEditResultMessage(DELETE_RESULT_OK)
})

internal object CompleteTask : Action({
    val task = Task(
            title = "Title",
            description = "Description"
    )
    repo.addTasks(
            task
    )
    viewModel.completeTask(
            task = task,
            completed = true
    )
})

internal object ActivateTask : Action({
    val task = Task(
            title = "Title",
            description = "Description",
            isCompleted = true
    )
    repo.addTasks(task)
    viewModel.completeTask(task = task, completed = false)
})

internal class ViewModelContext(
        val viewModel: TasksViewModel,
        val repo: FakeRepository
)