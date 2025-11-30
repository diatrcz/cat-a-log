import java.time.LocalDate

class Library {
    private val books = mutableListOf<Book>()
    private var nextId = 1

    fun addBook(title: String, author: String, year: Int) {
        val book = Book(nextId++, title, author, year)
        books.add(book)
        println("Book added: ${book.title}")
    }

    fun listAllBooks() {
        if (books.isEmpty()) {
            println("No books in the library.")
            return
        }
        println("\nAll Books:")
        books.forEach { println(it) }
    }

    fun searchByTitle(query: String) {
        val results = books.filter { it.title.contains(query, ignoreCase = true) }
        displaySearchResults(results, "title containing '$query'")
    }

    fun searchByAuthor(query: String) {
        val results = books.filter { it.author.contains(query, ignoreCase = true) }
        displaySearchResults(results, "author containing '$query'")
    }

    fun viewBook(id: Int) {
        val book = books.find { it.id == id }
        if (book == null) {
            println("Book with ID $id not found.")
        } else {
            println("\nBook Details:")
            println(book)
        }
    }

    fun checkoutBook(id: Int, borrower: String, dueDate: LocalDate) {
        val book = books.find { it.id == id }
        when {
            book == null -> println("Book with ID $id not found.")
            book.isBorrowed -> println("Book is already borrowed by ${book.borrower}.")
            else -> {
                book.checkout(borrower, dueDate)
                println("Book checked out to $borrower. Due date: $dueDate")
            }
        }
    }

    fun returnBook(id: Int) {
        val book = books.find { it.id == id }
        when {
            book == null -> println("Book with ID $id not found.")
            !book.isBorrowed -> println("Book is not currently borrowed.")
            else -> {
                val borrower = book.borrower
                book.returnBook()
                println("Book returned by $borrower.")
            }
        }
    }

    fun listBorrowedBooks() {
        val borrowed = books.filter { it.isBorrowed }
        displaySearchResults(borrowed, "borrowed books")
    }

    fun listBorrowedBooksByUser(username: String) {
        val userBooks = books.filter { it.borrower == username }
        displaySearchResults(userBooks, "books borrowed by $username")
    }

    fun listOverdueBooks() {
        val overdue = books.filter { it.isOverdue }
        displaySearchResults(overdue, "overdue books")
    }

    fun listOverdueBooksByUser(username: String) {
        val overdue = books.filter { it.borrower == username && it.isOverdue }
        displaySearchResults(overdue, "overdue books for $username")
    }

    private fun displaySearchResults(results: List<Book>, description: String) {
        if (results.isEmpty()) {
            println("No $description found.")
        } else {
            println("\nFound ${results.size} $description:")
            results.forEach { println(it) }
        }
    }
}