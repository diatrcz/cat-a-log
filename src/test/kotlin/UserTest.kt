package test.kotlin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import user.User

class UserTest {
    
    @Test
    fun `user should be created with correct properties`() {
        val user = User("testuser", "password123", isAdmin = false)
        
        assertEquals("testuser", user.username)
        assertEquals("password123", user.password)
        assertFalse(user.isAdmin)
    }
    
    @Test
    fun `admin user should have isAdmin flag set to true`() {
        val admin = User("admin", "admin123", isAdmin = true)
        
        assertTrue(admin.isAdmin)
    }
    
    @Test
    fun `regular user should have isAdmin flag set to false by default`() {
        val user = User("user", "pass", isAdmin = false)
        
        assertFalse(user.isAdmin)
    }
    
    @Test
    fun `users with same username and password should be equal`() {
        val user1 = User("testuser", "pass123", false)
        val user2 = User("testuser", "pass123", false)
        
        assertEquals(user1, user2)
    }
    
    @Test
    fun `users with different usernames should not be equal`() {
        val user1 = User("user1", "pass123", false)
        val user2 = User("user2", "pass123", false)
        
        assertNotEquals(user1, user2)
    }
    
    @Test
    fun `admin and regular user with same credentials should not be equal`() {
        val admin = User("testuser", "pass123", true)
        val regular = User("testuser", "pass123", false)
        
        assertNotEquals(admin, regular)
    }
}