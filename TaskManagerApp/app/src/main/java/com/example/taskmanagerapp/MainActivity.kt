package com.example.taskmanagerapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var taskInput: EditText
    private lateinit var addButton: Button
    private lateinit var taskList: RecyclerView
    private val tasks = mutableListOf<String>()
    private val completedTasks = mutableSetOf<Int>()
    private lateinit var taskAdapter: TaskAdapter
    private val gson = Gson()
    private val sharedPrefs by lazy { getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskInput = findViewById(R.id.taskInput)
        addButton = findViewById(R.id.addButton)
        taskList = findViewById(R.id.taskList)

        loadTasks()

        taskAdapter = TaskAdapter(tasks, completedTasks) { position ->
            tasks.removeAt(position)
            completedTasks.remove(position)
            taskAdapter.notifyItemRemoved(position)
            saveTasks()
        }

        taskList.adapter = taskAdapter
        taskList.layoutManager = LinearLayoutManager(this)

        addButton.setOnClickListener {
            val task = taskInput.text.toString()
            if (task.isNotEmpty()) {
                tasks.add(task)
                taskAdapter.notifyItemInserted(tasks.size - 1)
                taskInput.text.clear()
                saveTasks()
            }
        }
    }

    private fun saveTasks() {
        val tasksJson = gson.toJson(tasks)
        val completedTasksJson = gson.toJson(completedTasks)

        with(sharedPrefs.edit()) {
            putString("tasks", tasksJson)
            putString("completedTasks", completedTasksJson)
            apply()
        }
    }

    private fun loadTasks() {
        val tasksJson = sharedPrefs.getString("tasks", null)
        val completedTasksJson = sharedPrefs.getString("completedTasks", null)

        if (!tasksJson.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            tasks.addAll(gson.fromJson(tasksJson, type))
        }

        if (!completedTasksJson.isNullOrEmpty()) {
            val type = object : TypeToken<MutableSet<Int>>() {}.type
            completedTasks.addAll(gson.fromJson(completedTasksJson, type))
        }
    }
}
