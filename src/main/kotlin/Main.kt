import database.Database
import data.Library
import user.UserManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun main() {
    val database = Database()
    database.connect()
    
    val library = Library(database)
    val userManager = UserManager(database)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    println("Welcome to Cat-a-Log - Library Management System")
    println("\nDefault accounts:")
    println("  Admin: username='admin', password='admin123'")
    println("  User: username='user1', password='password'")

    try {
        while (true) {
            printMenu(userManager)
            print("\nEnter your choice: ")
            
            val input = readLine()?.trim() ?: ""
            
            when (input) {
                "1" -> {
                    if (checkLoggedIn(userManager)) {
                        addBook(library)
                    }
                }
                "2" -> library.listAllBooks()
                "3" -> searchBooks(library)
                "4" -> viewBookDetails(library)
                "5" -> {
                    if (checkLoggedIn(userManager)) {
                        checkoutBook(library, userManager, formatter)
                    }
                }
                "6" -> {
                    if (checkLoggedIn(userManager)) {
                        returnBook(library, userManager)
                    }
                }
                "7" -> {
                    if (checkLoggedIn(userManager)) {
                        userManager.getCurrentUsername()?.let { 
                            library.listBorrowedBooksByUser(it)
                        }
                    }
                }
                "8" -> {
                    if (checkLoggedIn(userManager)) {
                        userManager.getCurrentUsername()?.let { 
                            library.listOverdueBooksByUser(it)
                        }
                    }
                }
                "9" -> login(userManager)
                "10" -> register(userManager)
                "11" -> userManager.logout()
                "12" -> {
                    println("Goodbye!")
                    break
                }
                "" -> { /* Empty input, just show menu again */ }
                else -> println("Invalid choice. Please try again.")
            }
            println()
        }
    } finally {
        database.disconnect()
    }
}

fun printMenu(userManager: UserManager) {
    println("\n--- Cat-a-Log Menu ---")
    if (userManager.isLoggedIn()) {
        println("Logged in as: ${userManager.getCurrentUsername()}")
    } else {
        println("Not logged in (limited access)")
    }
    println()
    println("1. Add a book ${if (!userManager.isLoggedIn()) "[Login required]" else ""}")
    println("2. List all books")
    println("3. Search books")
    println("4. View book details")
    println("5. Check out a book ${if (!userManager.isLoggedIn()) "[Login required]" else ""}")
    println("6. Return a book ${if (!userManager.isLoggedIn()) "[Login required]" else ""}")
    println("7. List my borrowed books ${if (!userManager.isLoggedIn()) "[Login required]" else ""}")
    println("8. List my overdue books ${if (!userManager.isLoggedIn()) "[Login required]" else ""}")
    println("9. Login")
    println("10. Register")
    println("11. Logout")
    println("12. Exit")
}

fun addBook(library: Library) {
    print("Enter title: ")
    val title = readLine()?.trim()
    if (title.isNullOrBlank()) {
        println("Title cannot be empty.")
        return
    }
    
    print("Enter author: ")
    val author = readLine()?.trim()
    if (author.isNullOrBlank()) {
        println("Author cannot be empty.")
        return
    }
    
    print("Enter year: ")
    val year = readLine()?.trim()?.toIntOrNull()
    
    if (year == null) {
        println("Invalid year.")
        return
    }
    
    library.addBook(title, author, year)
}

fun searchBooks(library: Library) {
    println("\n1. Search by title")
    println("2. Search by author")
    print("Enter your choice: ")
    
    when (readLine()?.trim()) {
        "1" -> {
            print("Enter title to search: ")
            val query = readLine()?.trim()
            if (!query.isNullOrBlank()) {
                library.searchByTitle(query)
            }
        }
        "2" -> {
            print("Enter author to search: ")
            val query = readLine()?.trim()
            if (!query.isNullOrBlank()) {
                library.searchByAuthor(query)
            }
        }
        else -> println("Invalid choice.")
    }
}

fun viewBookDetails(library: Library) {
    print("Enter book ID: ")
    val id = readLine()?.trim()?.toIntOrNull()
    
    if (id == null) {
        println("Invalid ID.")
        return
    }
    
    library.viewBook(id)
}

fun checkoutBook(library: Library, userManager: UserManager, formatter: DateTimeFormatter) {
    print("Enter book ID: ")
    val id = readLine()?.trim()?.toIntOrNull()
    
    if (id == null) {
        println("Invalid ID.")
        return
    }
    
    val borrower = userManager.getCurrentUsername()
    if (borrower == null) {
        println("You must be logged in to check out books.")
        return
    }
    
    print("Enter due date (yyyy-MM-dd): ")
    val dueDateStr = readLine()?.trim()
    
    try {
        val dueDate = LocalDate.parse(dueDateStr, formatter)
        library.checkoutBook(id, borrower, dueDate)
    } catch (e: DateTimeParseException) {
        println("Invalid date format. Please use yyyy-MM-dd.")
    }
}

fun returnBook(library: Library, userManager: UserManager) {
    print("Enter book ID: ")
    val id = readLine()?.trim()?.toIntOrNull()
    
    if (id == null) {
        println("Invalid ID.")
        return
    }
    
    library.returnBook(id)
}

fun login(userManager: UserManager) {
    print("Enter username: ")
    val username = readLine()?.trim()
    
    if (username.isNullOrBlank()) {
        println("Username cannot be empty.")
        return
    }
    
    print("Enter password: ")
    val password = readLine()?.trim()
    
    if (password.isNullOrBlank()) {
        println("Password cannot be empty.")
        return
    }
    
    userManager.login(username, password)
}

fun register(userManager: UserManager) {
    print("Enter username: ")
    val username = readLine()?.trim()
    
    if (username.isNullOrBlank()) {
        println("Username cannot be empty.")
        return
    }
    
    print("Enter password: ")
    val password = readLine()?.trim()
    
    if (password.isNullOrBlank()) {
        println("Password cannot be empty.")
        return
    }
    
    userManager.registerUser(username, password)
}

fun checkLoggedIn(userManager: UserManager): Boolean {
    if (!userManager.isLoggedIn()) {
        println("You must be logged in to perform this action.")
        return false
    }
    return true
}