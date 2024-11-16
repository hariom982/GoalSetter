package com.example.goalsetter

data class Goal(
    var id: String,
    val title: String,
    val description: String,
    val duedate: String,
    val progress: Double,
    val tasks: List<String> = listOf(),
    val taskStates: List<Boolean> = emptyList()// Default to an empty list
){
    constructor() : this("", "", "", "",0.0, emptyList(), emptyList())
}
