package com.example.goalsetter

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.setPadding
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Calendar

class AddGoalActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val tasksList = mutableListOf<String>() // List to store task names
    private lateinit var tasksContainer: LinearLayout // Container for displaying tasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goal)

        val title: EditText = findViewById(R.id.title)
        val description: EditText = findViewById(R.id.description)
        val duedate: TextView = findViewById(R.id.duedate)
        val taskStates = tasksList.map { false }
        val tasks: TextView = findViewById(R.id.tasks)
        tasksContainer = findViewById(R.id.tasksContainer) // Container to display checkboxes

        val savebtn: MaterialButton = findViewById(R.id.savebtn)
        duedate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateText = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                duedate.text = dateText
            }, year, month, day)
            datePickerDialog.show()
        }
        tasks.setOnClickListener {
            showAddTaskDialog()
        }

        savebtn.setOnClickListener {
            if (validateFields(title, description, duedate)) {
                val goal = Goal(
                    id = "",
                    title = title.text.toString(),
                    description = description.text.toString(),
                    progress = 0.0,
                    duedate = duedate.text.toString(),
                    tasks = tasksList,
                    taskStates = taskStates
                )

                db.collection("goals").add(goal)
                    .addOnSuccessListener { finish() }
                    .addOnFailureListener { e -> Log.e("AddGoal", "Error adding goal", e) }
            }
        }
    }
    // Dialog Box to add new task
    @SuppressLint("MissingInflatedId")
    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.addnewtasks_dialogbox, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.addnewtask)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add New Task")
            .setPositiveButton("Add") { _, _ ->
                val taskName = taskNameEditText.text.toString().trim()

                if (!TextUtils.isEmpty(taskName)) {
                    tasksList.add(taskName) // Add task to the list
                    addTaskCheckbox(taskName) // Add a checkbox for the task
                } else {
                    taskNameEditText.error = "Task name cannot be empty"
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateFields(title: EditText, description: EditText, duedate: TextView): Boolean {
        var isValid = true

        if (title.text.toString().trim().isEmpty()) {
            title.error = "Title cannot be empty"
            isValid = false
        }
        if (description.text.toString().trim().isEmpty()) {
            description.error = "Description cannot be empty"
            isValid = false
        }
        if (duedate.text.toString().trim().isEmpty()) {
            duedate.error = "Due date cannot be empty"
            isValid = false
        }

        return isValid
    }


    // Add a new task checkbox dynamically
    private fun addTaskCheckbox(taskName: String) {
        val checkBox = CheckBox(this).apply {
            text = taskName
            setTextColor(ContextCompat.getColor(this@AddGoalActivity, android.R.color.black))

            isChecked = false
        }
        checkBox.setPadding(10,0,0,0)

        tasksContainer.addView(checkBox)
    }
}