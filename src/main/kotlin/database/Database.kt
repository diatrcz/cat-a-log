package database

import data.Book
import user.User
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.LocalDate

class Database(private val dbPath: String = "catalog.db") {
    private var connection: Connection? = null

    fun connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
            println("Database connected: $dbPath")
            createTables()
        } catch (e: Exception) {
            println("Error connecting to database: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            connection?.close()
            println("Database disconnected.")
        } catch (e: Exception) {
            println("Error disconnecting from database: ${e.message}")
        }
    }

    private fun createTables() {
        val createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                is_admin INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        val createBooksTable = """
            CREATE TABLE IF NOT EXISTS books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                year INTEGER NOT NULL,
                borrower TEXT,
                due_date TEXT,
                FOREIGN KEY (borrower) REFERENCES users(username)
            )
        """.trimIndent()

        try {
            connection?.createStatement()?.use { stmt ->
                stmt.execute(createUsersTable)
                stmt.execute(createBooksTable)
            }
        } catch (e: Exception) {
            println("Error creating tables: ${e.message}")
        }
    }

    // User operations
    fun saveUser(user: User): Boolean {
        val sql = "INSERT INTO users (username, password, is_admin) VALUES (?, ?, ?)"
        return try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, user.username)
                stmt.setString(2, user.password)
                stmt.setInt(3, if (user.isAdmin) 1 else 0)
                stmt.executeUpdate()
                true
            } ?: false
        } catch (e: Exception) {
            println("Error saving user: ${e.message}")
            false
        }
    }

    fun getUser(username: String): User? {
        val sql = "SELECT * FROM users WHERE username = ?"
        return try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, username)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    User(
                        username = rs.getString("username"),
                        password = rs.getString("password"),
                        isAdmin = rs.getInt("is_admin") == 1
                    )
                } else null
            }
        } catch (e: Exception) {
            println("Error getting user: ${e.message}")
            null
        }
    }

    fun userExists(username: String): Boolean {
        val sql = "SELECT COUNT(*) FROM users WHERE username = ?"
        return try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, username)
                val rs = stmt.executeQuery()
                rs.next() && rs.getInt(1) > 0
            } ?: false
        } catch (e: Exception) {
            println("Error checking user existence: ${e.message}")
            false
        }
    }

    // Book operations
    fun saveBook(book: Book): Int {
        val sql = "INSERT INTO books (title, author, year, borrower, due_date) VALUES (?, ?, ?, ?, ?)"
        return try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, book.title)
                stmt.setString(2, book.author)
                stmt.setInt(3, book.year)
                stmt.setString(4, book.borrower)
                stmt.setString(5, book.dueDate?.toString())
                stmt.executeUpdate()
                
                // Get the generated ID
                val rs = stmt.generatedKeys
                if (rs.next()) rs.getInt(1) else -1
            } ?: -1
        } catch (e: Exception) {
            println("Error saving book: ${e.message}")
            -1
        }
    }

    fun updateBook(book: Book): Boolean {
        val sql = "UPDATE books SET title = ?, author = ?, year = ?, borrower = ?, due_date = ? WHERE id = ?"
        return try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, book.title)
                stmt.setString(2, book.author)
                stmt.setInt(3, book.year)
                stmt.setString(4, book.borrower)
                stmt.setString(5, book.dueDate?.toString())
                stmt.setInt(6, book.id)
                stmt.executeUpdate()
                true
            } ?: false
        } catch (e: Exception) {
            println("Error updating book: ${e.message}")
            false
        }
    }

    fun getBook(id: Int): Book? {
        val sql = "SELECT * FROM books WHERE id = ?"
        return try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setInt(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    mapResultSetToBook(rs)
                } else null
            }
        } catch (e: Exception) {
            println("Error getting book: ${e.message}")
            null
        }
    }

    fun getAllBooks(): List<Book> {
        val sql = "SELECT * FROM books"
        val books = mutableListOf<Book>()
        try {
            connection?.createStatement()?.use { stmt ->
                val rs = stmt.executeQuery(sql)
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs))
                }
            }
        } catch (e: Exception) {
            println("Error getting all books: ${e.message}")
        }
        return books
    }

    fun searchBooksByTitle(query: String): List<Book> {
        val sql = "SELECT * FROM books WHERE title LIKE ?"
        val books = mutableListOf<Book>()
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, "%$query%")
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs))
                }
            }
        } catch (e: Exception) {
            println("Error searching books by title: ${e.message}")
        }
        return books
    }

    fun searchBooksByAuthor(query: String): List<Book> {
        val sql = "SELECT * FROM books WHERE author LIKE ?"
        val books = mutableListOf<Book>()
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, "%$query%")
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs))
                }
            }
        } catch (e: Exception) {
            println("Error searching books by author: ${e.message}")
        }
        return books
    }

    fun getBorrowedBooks(): List<Book> {
        val sql = "SELECT * FROM books WHERE borrower IS NOT NULL"
        val books = mutableListOf<Book>()
        try {
            connection?.createStatement()?.use { stmt ->
                val rs = stmt.executeQuery(sql)
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs))
                }
            }
        } catch (e: Exception) {
            println("Error getting borrowed books: ${e.message}")
        }
        return books
    }

    fun getBorrowedBooksByUser(username: String): List<Book> {
        val sql = "SELECT * FROM books WHERE borrower = ?"
        val books = mutableListOf<Book>()
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, username)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs))
                }
            }
        } catch (e: Exception) {
            println("Error getting borrowed books by user: ${e.message}")
        }
        return books
    }

    fun getNextBookId(): Int {
        val sql = "SELECT MAX(id) FROM books"
        return try {
            connection?.createStatement()?.use { stmt ->
                val rs = stmt.executeQuery(sql)
                if (rs.next()) {
                    val maxId = rs.getInt(1)
                    maxId + 1
                } else 1
            } ?: 1
        } catch (e: Exception) {
            println("Error getting next book ID: ${e.message}")
            1
        }
    }

    private fun mapResultSetToBook(rs: ResultSet): Book {
        val dueDateStr = rs.getString("due_date")
        return Book(
            id = rs.getInt("id"),
            title = rs.getString("title"),
            author = rs.getString("author"),
            year = rs.getInt("year"),
            borrower = rs.getString("borrower"),
            dueDate = if (dueDateStr != null) LocalDate.parse(dueDateStr) else null
        )
    }
}