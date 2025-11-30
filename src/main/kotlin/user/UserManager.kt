package user

import database.Database

class UserManager(private val database: Database) {
    private var currentUser: User? = null

    init {
        // Create default users if they don't exist
        if (!database.userExists("admin")) {
            database.saveUser(User("admin", "admin123", isAdmin = true))
        }
        if (!database.userExists("user1")) {
            database.saveUser(User("user1", "password", isAdmin = false))
        }
    }

    fun registerUser(username: String, password: String, isAdmin: Boolean = false): Boolean {
        if (database.userExists(username)) {
            println("Username already exists.")
            return false
        }
        val user = User(username, password, isAdmin)
        return if (database.saveUser(user)) {
            println("User registered successfully!")
            true
        } else {
            println("Error registering user.")
            false
        }
    }

    fun login(username: String, password: String): Boolean {
        val user = database.getUser(username)
        return if (user != null && user.password == password) {
            currentUser = user
            println("Login successful! Welcome, ${user.username}.")
            true
        } else {
            println("Invalid username or password.")
            false
        }
    }

    fun logout() {
        if (currentUser != null) {
            println("Goodbye, ${currentUser?.username}!")
            currentUser = null
        } else {
            println("No user is currently logged in.")
        }
    }

    fun isLoggedIn(): Boolean = currentUser != null

    fun getCurrentUsername(): String? = currentUser?.username

    fun isAdmin(): Boolean = currentUser?.isAdmin == true
}