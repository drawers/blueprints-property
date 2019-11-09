package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.verify

inline fun <reified T : Any> Observer<T>.observed(): List<T> {
    argumentCaptor<T>().apply {
        verify(this@observed, atLeastOnce()).onChanged(capture())
        return allValues
    }
}