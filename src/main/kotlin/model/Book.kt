import java.time.LocalDate

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int,
    var borrower: String? = null,
    var dueDate: LocalDate? = null
) {
    val isBorrowed: Boolean
        get() = borrower != null

    val isOverdue: Boolean
        get() = dueDate?.isBefore(LocalDate.now()) == true

    fun checkout(borrowerName: String, dueDate: LocalDate) {
        this.borrower = borrowerName
        this.dueDate = dueDate
    }

    fun returnBook() {
        this.borrower = null
        this.dueDate = null
    }

    override fun toString(): String {
        val status = when {
            !isBorrowed -> "Available"
            isOverdue -> "Overdue (due: $dueDate)"
            else -> "Borrowed (due: $dueDate)"
        }
        return "[$id] \"$title\" by $author ($year) - $status" +
            if (isBorrowed) " - Borrower: $borrower" else ""
    }
}