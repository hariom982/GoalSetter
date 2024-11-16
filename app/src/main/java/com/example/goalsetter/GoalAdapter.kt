package com.example.goalsetter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoalAdapter(
    private val goals: List<Goal>,
    private val context: Context
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.titleTextView.text = goal.title
        holder.descriptionTextView.text = goal.description
        holder.dueDateTextView.text = goal.duedate
        val taskCountText = "Total Tasks: ${goal.tasks.size}"
        holder.pendingTasksTextView.text = taskCountText
        holder.progress.text = goal.progress.toString() +"%"

        // Preparing task states if not already provided
        val taskStates = goal.taskStates.ifEmpty { List(goal.tasks.size) { false } }

        // Navigating to GoalDetailActivity when item is clicked
        holder.itemView.setOnClickListener {
            val intent = Intent(context, GoalDetailActivity::class.java).apply {
                putExtra("title", goal.title)
                putExtra("goalId", goal.id)
                putExtra("description", goal.description)
                putExtra("duedate", goal.duedate)
                putStringArrayListExtra("tasks", ArrayList(goal.tasks))
                putExtra("taskStates", taskStates.toBooleanArray())
                putExtra("taskCount", goal.tasks.size)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = goals.size

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titletv)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptiontv)
        val pendingTasksTextView: TextView = itemView.findViewById(R.id.pendingtask)
        val dueDateTextView: TextView = itemView.findViewById(R.id.duedatetv)
        val progress: TextView = itemView.findViewById(R.id.progress)
    }
}
