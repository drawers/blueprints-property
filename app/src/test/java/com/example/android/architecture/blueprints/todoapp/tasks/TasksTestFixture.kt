package com.example.android.architecture.blueprints.todoapp.tasks

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.tasks.OpenTask.OPEN_TASK_ID
import io.kotlintest.properties.Gen

internal class TasksContext(
        val viewModel: TasksViewModel,
        val repo: FakeRepository
)

internal sealed class Action(val body: TasksContext.() -> Unit) {
    override fun toString(): String {
        return this::class.simpleName!!
    }
}

internal fun Gen.Companion.action(): Gen<Action> = object : Gen<Action> {

    override fun constants(): Iterable<Action> = Action::class.sealedSubclasses.map {
        it.objectInstance!!
    }

    override fun random(): Sequence<Action> = constants().shuffled().asSequence()
}

internal fun Gen.Companion.actions(): Gen<List<Action>> = object : Gen<List<Action>> {

    override fun constants(): Iterable<List<Action>> = Action::class.sealedSubclasses.map {
        listOf(LoadForce, it.objectInstance!!, Load)
    }

    override fun random(): Sequence<List<Action>> = constants().shuffled().asSequence()
}


internal val SAMPLE_TASK = Task(
        title = "Sample title",
        description = "Sample description",
        isCompleted = false
)

internal val SAMPLE_TASK_COMPLETE = Task(
        title = "Sample title - complete",
        description = "Sample description - complete",
        isCompleted = false
)

internal object Load : Action({
    viewModel.loadTasks(forceUpdate = false)
})

internal object LoadForce : Action({
    viewModel.loadTasks(forceUpdate = true)
})

internal object LoadError : Action({
    repo.setReturnError(true)
    viewModel.loadTasks(true)
    repo.setReturnError(false)
})

internal object FilterActiveTasks : Action({
    viewModel.loadTasks(true)
    viewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)
    viewModel.loadTasks(false)
})

internal object FilterCompletedTasks : Action({
    viewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)
    viewModel.loadTasks(false)
})

internal object FilterAllTasks : Action({
    viewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)
    viewModel.loadTasks(false)
})

internal object AddNewTask : Action(
        { viewModel.addNewTask() }
)

internal object OpenTask : Action(
        { viewModel.openTask(OPEN_TASK_ID) }
) {
    val OPEN_TASK_ID = "42"
}

internal object ClearCompleted : Action(
        {
            viewModel.clearCompletedTasks()
            viewModel.loadTasks(true)
        }
) {

}

internal object ShowEditResultMessage : Action(
        {
            viewModel.showEditResultMessage(EDIT_RESULT_OK)
        }
)

internal object ShowAddEditResultMessage : Action(
        {
            viewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)
        }
)

internal object ShowDeleteOkMessage : Action(
        {
            viewModel.showEditResultMessage(DELETE_RESULT_OK)
        }
)

internal object CompleteTask : Action({
    repo.addTasks(
            SAMPLE_TASK
    )
    viewModel.completeTask(
            task = SAMPLE_TASK,
            completed = true
    )
})

internal object ActivateTask : Action({
    repo.addTasks(SAMPLE_TASK_COMPLETE)
    viewModel.completeTask(task = SAMPLE_TASK_COMPLETE, completed = false)
})