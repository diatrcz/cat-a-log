package data

import database.Database
import java.time.LocalDate

class Library(private val database: Database) {

    fun addBook(title: String, author: String, year: Int) {
        val nextId = database.getNextBookId()
        val book = Book(nextId, title, author, year)
        val savedId = database.saveBook(book)
        if (savedId > 0) {
            println("Book added: $title")
        } else {
            println("Error adding book.")
        }
    }

    fun listAllBooks() {
        val books = database.getAllBooks()
        if (books.isEmpty()) {
            println("No books in the library.")
            return
        }
        println("\nAll Books:")
        books.forEach { println(it) }
    }

    fun searchByTitle(query: String) {
        val results = database.searchBooksByTitle(query)
        displaySearchResults(results, "title containing '$query'")
    }

    fun searchByAuthor(query: String) {
        val results = database.searchBooksByAuthor(query)
        displaySearchResults(results, "author containing '$query'")
    }

    fun viewBook(id: Int) {
        val book = database.getBook(id)
        if (book == null) {
            println("Book with ID $id not found.")
        } else {
            println("\nBook Details:")
            println(book)
        }
    }

    fun checkoutBook(id: Int, borrower: String, dueDate: LocalDate) {
        val book = database.getBook(id)
        when {
            book == null -> println("Book with ID $id not found.")
            book.isBorrowed -> println("Book is already borrowed by ${book.borrower}.")
            else -> {
                book.checkout(borrower, dueDate)
                if (database.updateBook(book)) {
                    println("Book checked out to $borrower. Due date: $dueDate")
                } else {
                    println("Error checking out book.")
                }
            }
        }
    }

    fun returnBook(id: Int) {
        val book = database.getBook(id)
        when {
            book == null -> println("Book with ID $id not found.")
            !book.isBorrowed -> println("Book is not currently borrowed.")
            else -> {
                val borrower = book.borrower
                book.returnBook()
                if (database.updateBook(book)) {
                    println("Book returned by $borrower.")
                } else {
                    println("Error returning book.")
                }
            }
        }
    }

    fun listBorrowedBooks() {
        val borrowed = database.getBorrowedBooks()
        displaySearchResults(borrowed, "borrowed books")
    }

    fun listBorrowedBooksByUser(username: String) {
        val userBooks = database.getBorrowedBooksByUser(username)
        displaySearchResults(userBooks, "books borrowed by $username")
    }

    fun listOverdueBooks() {
        val overdue = database.getAllBooks().filter { it.isOverdue }
        displaySearchResults(overdue, "overdue books")
    }

    fun listOverdueBooksByUser(username: String) {
        val overdue = database.getBorrowedBooksByUser(username).filter { it.isOverdue }
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