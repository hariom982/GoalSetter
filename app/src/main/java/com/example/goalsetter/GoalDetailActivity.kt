package com.example.goalsetter

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.concurrent.TimeUnit

class GoalDetailActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var progressTextView: TextView
    private lateinit var goalId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_detail)

        val title = intent.getStringExtra("title") ?: "No Title"
        val description = intent.getStringExtra("description") ?: "No Description"
        goalId = intent.getStringExtra("goalId") ?: throw IllegalArgumentException("Goal ID not found")
        val dueDate = intent.getStringExtra("duedate") ?: "No Due Date"
        val tasks = intent.getStringArrayListExtra("tasks") ?: arrayListOf("No Tasks")
        val taskStates = intent.getBooleanArrayExtra("taskStates")?.toMutableList() ?: MutableList(tasks.size) { false }
        val goalId = intent.getStringExtra("goalId") // Getting the goal ID passed from MainActivity
        if (goalId != null) {
            val goalRef = db.collection("goals").document(goalId) // Correctly reference the document in Firestore
            goalRef.update("taskStates", taskStates)
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->
                    Log.e("GoalDetailActivity", "Error updating task state", e)
                }
        }
        //


        findViewById<TextView>(R.id.goalTitle).text = title
        findViewById<TextView>(R.id.goalDescription).text = description
        findViewById<TextView>(R.id.goalDueDate).text = dueDate
        progressTextView = findViewById(R.id.progressTextView)

        val tasksRecyclerView = findViewById<RecyclerView>(R.id.tasksRecyclerView)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksRecyclerView.adapter = TaskAdapter(tasks, taskStates) { position, isChecked ->
            updateTaskState(position, isChecked, tasks, taskStates)
        }
        // Updating initial progress
        updateProgress(taskStates)

        //scheduling daily reminder notifications about the pending goals
        if (isDueDateInFuture(dueDate)) {
            scheduleDailyNotification(this)
        }
    }

    private fun updateTaskState(
        taskIndex: Int,
        isChecked: Boolean,
        tasks: List<String>,
        taskStates: MutableList<Boolean>
    ) {
        taskStates[taskIndex] = isChecked

        val goalRef = db.collection("goals").document(goalId)
        goalRef.update("taskStates", taskStates)
            .addOnSuccessListener {
                Log.d("GoalDetail", "Task state updated successfully.")
                updateProgress(taskStates)
            }
            .addOnFailureListener { e ->
                Log.e("GoalDetail", "Error updating task state", e)
            }
    }

    private fun updateProgress(taskStates: List<Boolean>) {
        val completedTasks = taskStates.count { it }
        val totalTasks = taskStates.size
        val progress = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks * 100).toInt() else 0
        progressTextView.text = "$progress% Completed"

        val goalRef = db.collection("goals").document(goalId)
        goalRef.update("progress", progress.toDouble())
            .addOnSuccessListener {
                Log.d("GoalDetail", "Progress updated successfully in Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e("GoalDetail", "Error updating progress in Firestore", e)
            }
    }

    private fun isDueDateInFuture(dueDate: String): Boolean {
        val currentCalendar = Calendar.getInstance()
        val dueCalendar = Calendar.getInstance()

        // Validate the dueDate format
        val dueDateParts = dueDate.split("/")
        if (dueDateParts.size != 3) {
            Log.e("GoalDetailActivity", "Invalid due date format: $dueDate")
            return false
        }

        return try {
            val day = dueDateParts[0].toInt()
            val month = dueDateParts[1].toInt() - 1
            val year = dueDateParts[2].toInt()

            dueCalendar.set(year, month, day)
            dueCalendar.after(currentCalendar)
        } catch (e: NumberFormatException) {
            Log.e("GoalDetailActivity", "Invalid due date values: $dueDate", e)
            false
        }
    }


    fun scheduleDailyNotification(context: Context) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            //setting the notification timing exactly at 9am
            set(Calendar.HOUR_OF_DAY, 9) // 9  AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0 )
        }

        // Calculate initial delay
        if (currentTime.after(targetTime)) {
            // If 9 AM already passed today, schedule for the next day
            targetTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        // Periodic work request
        val dailyWorkRequest = PeriodicWorkRequestBuilder<GoalNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS) // Delay until 9 AM
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyGoalNotification",
            ExistingPeriodicWorkPolicy.REPLACE, // Replace any existing scheduled work
            dailyWorkRequest
        )
    }


}
