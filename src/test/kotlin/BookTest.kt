package test.kotlin

import data.Book
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate

class BookTest {
    
    private lateinit var book: Book
    
    @BeforeEach
    fun setup() {
        book = Book(1, "The Hobbit", "J.R.R. Tolkien", 1937)
    }
    
    @Test
    fun `book should be created with correct properties`() {
        assertEquals(1, book.id)
        assertEquals("The Hobbit", book.title)
        assertEquals("J.R.R. Tolkien", book.author)
        assertEquals(1937, book.year)
        assertNull(book.borrower)
        assertNull(book.dueDate)
    }
    
    @Test
    fun `book should not be borrowed initially`() {
        assertFalse(book.isBorrowed)
    }
    
    @Test
    fun `book should not be overdue initially`() {
        assertFalse(book.isOverdue)
    }
    
    @Test
    fun `checkout should set borrower and due date`() {
        val dueDate = LocalDate.now().plusDays(14)
        book.checkout("John Doe", dueDate)
        
        assertTrue(book.isBorrowed)
        assertEquals("John Doe", book.borrower)
        assertEquals(dueDate, book.dueDate)
    }
    
    @Test
    fun `returnBook should clear borrower and due date`() {
        val dueDate = LocalDate.now().plusDays(14)
        book.checkout("John Doe", dueDate)
        book.returnBook()
        
        assertFalse(book.isBorrowed)
        assertNull(book.borrower)
        assertNull(book.dueDate)
    }
    
    @Test
    fun `book should be overdue when due date is in the past`() {
        val pastDate = LocalDate.now().minusDays(5)
        book.checkout("John Doe", pastDate)
        
        assertTrue(book.isOverdue)
    }
    
    @Test
    fun `book should not be overdue when due date is in the future`() {
        val futureDate = LocalDate.now().plusDays(5)
        book.checkout("John Doe", futureDate)
        
        assertFalse(book.isOverdue)
    }
    
    @Test
    fun `book should not be overdue when due date is today`() {
        val today = LocalDate.now()
        book.checkout("John Doe", today)
        
        assertFalse(book.isOverdue)
    }
    
    @Test
    fun `toString should show available status when not borrowed`() {
        val str = book.toString()
        assertTrue(str.contains("Available"))
        assertFalse(str.contains("Borrower"))
    }
    
    @Test
    fun `toString should show borrowed status with borrower name`() {
        val dueDate = LocalDate.now().plusDays(14)
        book.checkout("Jane Smith", dueDate)
        
        val str = book.toString()
        assertTrue(str.contains("Borrowed"))
        assertTrue(str.contains("Jane Smith"))
        assertTrue(str.contains(dueDate.toString()))
    }
    
    @Test
    fun `toString should show overdue status when past due`() {
        val pastDate = LocalDate.now().minusDays(5)
        book.checkout("Jane Smith", pastDate)
        
        val str = book.toString()
        assertTrue(str.contains("Overdue"))
        assertTrue(str.contains("Jane Smith"))
    }
}