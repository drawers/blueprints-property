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
package com.example.android.architecture.blueprints.todoapp.data.source

import androidx.annotation.VisibleForTesting
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Error
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.LinkedHashMap

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeRepository : TasksRepository {

    var tasksServiceData: LinkedHashMap<String, Task> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        synchronized(this) {
            shouldReturnError = value
        }
    }

    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<Task> {
        synchronized(this) {
            if (shouldReturnError) {
                return Error(Exception("Test exception"))
            }
            tasksServiceData[taskId]?.let {
                return Success(it)
            }
            return Error(Exception("Could not find task"))
        }
    }

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {
        synchronized(this) {
            if (shouldReturnError) {
                return Error(Exception("Test exception"))
            }
            return Success(tasksServiceData.values.toList())
        }
    }

    override suspend fun saveTask(task: Task) {
        synchronized(this) {
            tasksServiceData[task.id] = task
        }
    }

    override suspend fun completeTask(task: Task) {
        synchronized(this) {
            val completedTask = Task(task.title, task.description, true, task.id)
            tasksServiceData[task.id] = completedTask
        }
    }

    override suspend fun completeTask(taskId: String) {
        // Not required for the remote data source.
        throw NotImplementedError()
    }

    override suspend fun activateTask(task: Task) {
        synchronized(this) {
            val activeTask = Task(task.title, task.description, false, task.id)
            tasksServiceData[task.id] = activeTask
        }
    }

    override suspend fun activateTask(taskId: String) {
        throw NotImplementedError()
    }

    override suspend fun clearCompletedTasks() {
        synchronized(this) {
            tasksServiceData = tasksServiceData.filterValues {
                !it.isCompleted
            } as LinkedHashMap<String, Task>
        }
    }

    override suspend fun deleteTask(taskId: String) {
        synchronized(this) {
            tasksServiceData.remove(taskId)
        }
    }

    override suspend fun deleteAllTasks() {
        synchronized(this) {
            tasksServiceData.clear()
        }
    }

    @VisibleForTesting
    fun addTasks(vararg tasks: Task) {
        synchronized(this) {
            for (task in tasks) {
                tasksServiceData[task.id] = task
            }
        }
    }
}
