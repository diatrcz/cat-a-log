package test.kotlin

import data.Library
import database.Database
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.time.LocalDate

class LibraryTest {

    private lateinit var database: Database
    private lateinit var library: Library
    private val testDbPath = "test_library.db"
    private val outputStream = ByteArrayOutputStream()
    private val originalOut = System.out

    @BeforeEach
    fun setup() {
        File(testDbPath).delete()
        database = Database(testDbPath)
        database.connect()
        library = Library(database)
        System.setOut(PrintStream(outputStream))
    }

    @AfterEach
    fun teardown() {
        System.setOut(originalOut)
        database.disconnect()
        File(testDbPath).delete()
    }

    private fun getOutput(): String = outputStream.toString()

    @Test
    fun `addBook should add book to database`() {
        library.addBook("Test Book", "Test Author", 2024)

        val books = database.getAllBooks()
        assertEquals(1, books.size)
        assertEquals("Test Book", books[0].title)
    }

    @Test
    fun `listAllBooks should handle empty library`() {
        library.listAllBooks()

        assertTrue(getOutput().contains("No books in the library"))
    }

    @Test
    fun `listAllBooks should display all books`() {
        library.addBook("Book 1", "Author 1", 2024)
        library.addBook("Book 2", "Author 2", 2023)
        outputStream.reset()

        library.listAllBooks()

        val output = getOutput()
        assertTrue(output.contains("Book 1"))
        assertTrue(output.contains("Book 2"))
    }

    @Test
    fun `searchByTitle should find matching books`() {
        library.addBook("The Hobbit", "Tolkien", 1937)
        library.addBook("The Lord of the Rings", "Tolkien", 1954)
        library.addBook("1984", "Orwell", 1949)
        outputStream.reset()

        library.searchByTitle("The")

        val output = getOutput()
        assertTrue(output.contains("The Hobbit"))
        assertTrue(output.contains("The Lord of the Rings"))
        assertFalse(output.contains("1984"))
    }

    @Test
    fun `searchByTitle should handle no matches`() {
        library.addBook("Book 1", "Author", 2024)
        outputStream.reset()

        library.searchByTitle("NonExistent")

        assertTrue(getOutput().contains("No title containing 'NonExistent' found"))
    }

    @Test
    fun `searchByAuthor should find books by author`() {
        library.addBook("The Hobbit", "Tolkien", 1937)
        library.addBook("The Lord of the Rings", "Tolkien", 1954)
        library.addBook("1984", "Orwell", 1949)
        outputStream.reset()

        library.searchByAuthor("Tolkien")

        val output = getOutput()
        assertTrue(output.contains("The Hobbit"))
        assertTrue(output.contains("The Lord of the Rings"))
        assertFalse(output.contains("1984"))
    }

    @Test
    fun `viewBook should display book details`() {
        library.addBook("Test Book", "Test Author", 2024)
        val books = database.getAllBooks()
        outputStream.reset()

        library.viewBook(books[0].id)

        val output = getOutput()
        assertTrue(output.contains("Test Book"))
        assertTrue(output.contains("Test Author"))
        assertTrue(output.contains("2024"))
    }

    @Test
    fun `viewBook should handle non-existent book`() {
        library.viewBook(999)

        assertTrue(getOutput().contains("Book with ID 999 not found"))
    }

    @Test
    fun `checkoutBook should checkout available book`() {
        library.addBook("Test Book", "Author", 2024)
        val books = database.getAllBooks()
        val bookId = books[0].id
        outputStream.reset()

        val dueDate = LocalDate.now().plusDays(14)
        library.checkoutBook(bookId, "John Doe", dueDate)

        val output = getOutput()
        assertTrue(output.contains("Book checked out to John Doe"))

        val updatedBook = database.getBook(bookId)
        assertEquals("John Doe", updatedBook?.borrower)
    }

    @Test
    fun `checkoutBook should fail for already borrowed book`() {
        library.addBook("Test Book", "Author", 2024)
        val books = database.getAllBooks()
        val bookId = books[0].id

        library.checkoutBook(bookId, "User1", LocalDate.now().plusDays(14))
        outputStream.reset()

        library.checkoutBook(bookId, "User2", LocalDate.now().plusDays(7))

        assertTrue(getOutput().contains("already borrowed"))
    }

    @Test
    fun `checkoutBook should handle non-existent book`() {
        library.checkoutBook(999, "User", LocalDate.now().plusDays(14))

        assertTrue(getOutput().contains("Book with ID 999 not found"))
    }

    @Test
    fun `returnBook should return borrowed book`() {
        library.addBook("Test Book", "Author", 2024)
        val books = database.getAllBooks()
        val bookId = books[0].id

        library.checkoutBook(bookId, "John Doe", LocalDate.now().plusDays(14))
        outputStream.reset()

        library.returnBook(bookId)

        assertTrue(getOutput().contains("Book returned by John Doe"))

        val updatedBook = database.getBook(bookId)
        assertNull(updatedBook?.borrower)
    }

    @Test
    fun `returnBook should fail for non-borrowed book`() {
        library.addBook("Test Book", "Author", 2024)
        val books = database.getAllBooks()
        val bookId = books[0].id
        outputStream.reset()

        library.returnBook(bookId)

        assertTrue(getOutput().contains("not currently borrowed"))
    }

    @Test
    fun `returnBook should handle non-existent book`() {
        library.returnBook(999)

        assertTrue(getOutput().contains("Book with ID 999 not found"))
    }

    @Test
    fun `listBorrowedBooksByUser should show only user's books`() {
        library.addBook("Book 1", "Author", 2024)
        library.addBook("Book 2", "Author", 2024)
        library.addBook("Book 3", "Author", 2024)

        val books = database.getAllBooks()
        library.checkoutBook(books[0].id, "Alice", LocalDate.now().plusDays(14))
        library.checkoutBook(books[1].id, "Bob", LocalDate.now().plusDays(14))
        library.checkoutBook(books[2].id, "Alice", LocalDate.now().plusDays(7))
        outputStream.reset()

        library.listBorrowedBooksByUser("Alice")

        val output = getOutput()
        assertTrue(output.contains("Found 2 books borrowed by Alice"))
    }

    @Test
    fun `listOverdueBooksByUser should show only user's overdue books`() {
        library.addBook("Book 1", "Author", 2024)
        library.addBook("Book 2", "Author", 2024)
        library.addBook("Book 3", "Author", 2024)

        val books = database.getAllBooks()
        val pastDate = LocalDate.now().minusDays(5)
        val futureDate = LocalDate.now().plusDays(5)

        library.checkoutBook(books[0].id, "Alice", pastDate)
        library.checkoutBook(books[1].id, "Alice", futureDate)
        library.checkoutBook(books[2].id, "Bob", pastDate)
        outputStream.reset()

        library.listOverdueBooksByUser("Alice")

        val output = getOutput()
        assertTrue(output.contains("Found 1 overdue books for Alice"))
    }

    @Test
    fun `listBorrowedBooks should show all borrowed books`() {
        library.addBook("Book 1", "Author", 2024)
        library.addBook("Book 2", "Author", 2024)
        library.addBook("Book 3", "Author", 2024)

        val books = database.getAllBooks()
        library.checkoutBook(books[0].id, "Alice", LocalDate.now().plusDays(14))
        library.checkoutBook(books[1].id, "Bob", LocalDate.now().plusDays(7))
        outputStream.reset()

        library.listBorrowedBooks()

        val output = getOutput()
        assertTrue(output.contains("Found 2 borrowed books"))
    }

    @Test
    fun `listOverdueBooks should show all overdue books`() {
        library.addBook("Book 1", "Author", 2024)
        library.addBook("Book 2", "Author", 2024)
        library.addBook("Book 3", "Author", 2024)

        val books = database.getAllBooks()
        val pastDate = LocalDate.now().minusDays(5)
        val futureDate = LocalDate.now().plusDays(5)

        library.checkoutBook(books[0].id, "Alice", pastDate)
        library.checkoutBook(books[1].id, "Bob", pastDate)
        library.checkoutBook(books[2].id, "Charlie", futureDate)
        outputStream.reset()

        library.listOverdueBooks()

        val output = getOutput()
        assertTrue(output.contains("Found 2 overdue books"))
    }
}