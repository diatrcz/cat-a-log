# Cat-a-Log - Library Management System

A simple command-line application for managing a library's book collection and tracking borrowed books with user authentication and SQLite database persistence.

## Features

### Data Persistence
- All books and users are stored in a SQLite database (`catalog.db`)
- Data persists between sessions
- Automatic database initialization on first run

### Public Features (No Login Required)
- List all books in the library
- Search books by title or author
- View detailed information about specific books

### User Features (Login Required)
- Add books to the library
- Check out books (automatically assigned to your account)
- Return borrowed books
- List your own borrowed books
- List your own overdue books

### User Management
- User registration
- User login/logout
- Default test accounts included

## Default Accounts

The system comes with two pre-configured accounts for testing:

- **Admin Account**
  - Username: `admin`
  - Password: `admin123`

- **Regular User**
  - Username: `user1`
  - Password: `password`

## Running the Application

### Option 1: Using Gradle (Recommended)

```bash
# Run directly
./gradlew run
```

### Option 2: Using IntelliJ IDEA or other IDE

Simply open the project and run `Main.kt`

## Usage Example

```
Welcome to Cat-a-Log - Library Management System

Default accounts:
  Admin: username='admin', password='admin123'
  User: username='user1', password='password'

--- Cat-a-Log Menu ---
Not logged in (limited access)

1. Add a book [Login required]
2. List all books
3. Search books
4. View book details
5. Check out a book [Login required]
6. Return a book [Login required]
7. List my borrowed books [Login required]
8. List my overdue books [Login required]
9. Login
10. Register
11. Logout
12. Exit

Enter your choice: 9
Enter username: user1
Enter password: password
Login successful! Welcome, user1.

Enter your choice: 1
Enter title: The Hobbit
Enter author: J.R.R. Tolkien
Enter year: 1937
Book added: The Hobbit

Enter your choice: 5
Enter book ID: 1
Enter due date (yyyy-MM-dd): 2025-12-15
Book checked out to user1. Due date: 2025-12-15

Enter your choice: 7
Found 1 books borrowed by user1:
[1] "The Hobbit" by J.R.R. Tolkien (1937) - Borrowed (due: 2025-12-15) - Borrower: user1
```

## Project Structure

- `Main.kt` - CLI interface and menu system
- `Book.kt` - Book data class with borrowing functionality
- `Library.kt` - Library management logic
- `User.kt` - User data class
- `UserManager.kt` - User authentication and management
- `Database.kt` - SQLite database operations
- `build.gradle.kts` - Gradle build configuration
- `catalog.db` - SQLite database file (created automatically)

## Date Format

When entering due dates, use the format: `yyyy-MM-dd` (e.g., 2025-12-31)

## Database

The application uses SQLite to persist all data:

- **Database file**: `catalog.db` (created in the same directory as the application)
- **Tables**: 
  - `users` - Stores user accounts (username, password, admin flag)
  - `books` - Stores all books and their borrowing status
- **Automatic setup**: The database and tables are created automatically on first run
- **Data persistence**: All books and users are saved and will be available when you restart the application

To reset the database, simply delete the `catalog.db` file. The default admin and user1 accounts will be recreated on next run.