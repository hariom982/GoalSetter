package com.example.goalsetter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var goalsRecyclerView: RecyclerView
    private lateinit var addGoalFab: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goalsRecyclerView = findViewById(R.id.goalsRecyclerView)
        addGoalFab = findViewById(R.id.addGoalFab)

        addGoalFab.setOnClickListener {
            val intent = Intent(this, AddGoalActivity::class.java)
            startActivity(intent)
        }

        loadGoals()

    }

    private fun loadGoals() {
        db.collection("goals").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("MainActivity", "Listen failed.", e)
                return@addSnapshotListener
            }

            val goals = snapshots?.map { document ->
                val progress = document.getDouble("progress") ?: 0.0  // Default to 0.0 if null

                val tasks = document.get("tasks") as? List<String> ?: emptyList()
                val taskStates = document.get("taskStates") as? List<Boolean> ?: List(tasks.size) { false }
                Goal(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    description = document.getString("description") ?: "",
                    duedate = document.getString("duedate") ?: "No Date",
                    progress = progress,
                    tasks = document.get("tasks") as? List<String> ?: emptyList(),
                    taskStates = taskStates
                )
            } ?: emptyList()

            goalsRecyclerView.layoutManager = LinearLayoutManager(this)
            goalsRecyclerView.adapter = GoalAdapter(goals,this)
        }
    }
}