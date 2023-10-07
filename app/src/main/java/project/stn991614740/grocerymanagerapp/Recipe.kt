package project.stn991614740.grocerymanagerapp

// Data class to represent a recipe with an ID, creation date, and recipe text
data class Recipe(
    val id: String = "",
    val creationDate: com.google.firebase.Timestamp? = null,
    val recipe: String = ""
)
