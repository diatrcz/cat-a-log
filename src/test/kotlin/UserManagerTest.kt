package test.kotlin

import database.Database
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import user.UserManager
import java.io.File

class UserManagerTest {

    private lateinit var database: Database
    private lateinit var userManager: UserManager
    private val testDbPath = "test_usermanager.db"

    @BeforeEach
    fun setup() {
        File(testDbPath).delete()
        database = Database(testDbPath)
        database.connect()
        userManager = UserManager(database)
    }

    @AfterEach
    fun teardown() {
        database.disconnect()
        File(testDbPath).delete()
    }

    @Test
    fun `default users should be created on initialization`() {
        assertTrue(database.userExists("admin"))
        assertTrue(database.userExists("user1"))
    }

    @Test
    fun `admin user should be admin`() {
        val admin = database.getUser("admin")
        assertNotNull(admin)
        assertTrue(admin?.isAdmin ?: false)
    }

    @Test
    fun `user1 should not be admin`() {
        val user = database.getUser("user1")
        assertNotNull(user)
        assertFalse(user?.isAdmin ?: true)
    }

    @Test
    fun `registerUser should create new user`() {
        val result = userManager.registerUser("newuser", "newpass")

        assertTrue(result)
        assertTrue(database.userExists("newuser"))
    }

    @Test
    fun `registerUser should fail for existing username`() {
        userManager.registerUser("testuser", "pass123")
        val result = userManager.registerUser("testuser", "differentpass")

        assertFalse(result)
    }

    @Test
    fun `registerUser should create regular user by default`() {
        userManager.registerUser("regularuser", "pass")
        val user = database.getUser("regularuser")

        assertFalse(user?.isAdmin ?: true)
    }

    @Test
    fun `login should succeed with correct credentials`() {
        userManager.registerUser("testuser", "password123")
        val result = userManager.login("testuser", "password123")

        assertTrue(result)
        assertTrue(userManager.isLoggedIn())
    }

    @Test
    fun `login should fail with incorrect password`() {
        userManager.registerUser("testuser", "password123")
        val result = userManager.login("testuser", "wrongpassword")

        assertFalse(result)
        assertFalse(userManager.isLoggedIn())
    }

    @Test
    fun `login should fail with non-existent username`() {
        val result = userManager.login("nonexistent", "password")

        assertFalse(result)
        assertFalse(userManager.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false initially`() {
        assertFalse(userManager.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return true after successful login`() {
        userManager.registerUser("testuser", "pass123")
        userManager.login("testuser", "pass123")

        assertTrue(userManager.isLoggedIn())
    }

    @Test
    fun `getCurrentUsername should return null when not logged in`() {
        assertNull(userManager.getCurrentUsername())
    }

    @Test
    fun `getCurrentUsername should return username when logged in`() {
        userManager.registerUser("testuser", "pass123")
        userManager.login("testuser", "pass123")

        assertEquals("testuser", userManager.getCurrentUsername())
    }

    @Test
    fun `logout should clear current user`() {
        userManager.registerUser("testuser", "pass123")
        userManager.login("testuser", "pass123")
        userManager.logout()

        assertFalse(userManager.isLoggedIn())
        assertNull(userManager.getCurrentUsername())
    }

    @Test
    fun `isAdmin should return false for regular user`() {
        userManager.registerUser("testuser", "pass123", false)
        userManager.login("testuser", "pass123")

        assertFalse(userManager.isAdmin())
    }

    @Test
    fun `isAdmin should return true for admin user`() {
        userManager.login("admin", "admin123")

        assertTrue(userManager.isAdmin())
    }

    @Test
    fun `isAdmin should return false when not logged in`() {
        assertFalse(userManager.isAdmin())
    }

}