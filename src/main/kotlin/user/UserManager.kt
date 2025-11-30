class UserManager {
    private val users = mutableMapOf<String, User>()
    private var currentUser: User? = null

    init {
        registerUser("admin", "admin123", isAdmin = true)
        registerUser("user1", "password", isAdmin = false)
    }

    fun registerUser(username: String, password: String, isAdmin: Boolean = false): Boolean {
        if (users.containsKey(username)) {
            println("Username already exists.")
            return false
        }
        users[username] = User(username, password, isAdmin)
        println("User registered successfully!")
        return true
    }

    fun login(username: String, password: String): Boolean {
        val user = users[username]
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
            println("Succesfully logged out!")
            currentUser = null
        } else {
            println("No user is currently logged in.")
        }
    }

    fun isLoggedIn(): Boolean = currentUser != null

    fun getCurrentUsername(): String? = currentUser?.username

    fun isAdmin(): Boolean = currentUser?.isAdmin == true
}