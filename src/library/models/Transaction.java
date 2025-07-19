package library.models;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Transaction {
    // global counter
    private static int transactionCounter = 100000;

    private final String transactionID;
    private final String memberID;
    private String borrowedBookID;
    private LocalDate issueDate;
    private LocalDate returnDate = null;
    private LocalDate dueDate;
    long fine;

    public String getTransactionID() { return transactionID; }
    public String getMemberID() { return memberID; }
    public String getBorrowedBookID() { return borrowedBookID; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }

    Transaction(String memberID, String  borrowedBookID, LocalDate issueDate, LocalDate dueDate) {

        this.memberID = memberID;
        this.borrowedBookID = borrowedBookID;
        this.issueDate = issueDate;
        this.dueDate = dueDate;

        this.transactionID = "TR_" + String.format("%06d", ++transactionCounter);
    }

    public void setReturnDate() {
        this.returnDate = LocalDate.now();
    }

    public LocalDate getReturnDate() {
        return this.returnDate;
    }

    public void calculateFine(@NotNull LocalDate returnDate, @NotNull LocalDate dueDate) {
        Objects.requireNonNull(returnDate, "Return date cannot be null");
        Objects.requireNonNull(dueDate, "Due date cannot be null");

        // Calculates fine if returnDate is past dueDate
        if (returnDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Return Date cant be before Issue Date");
        }

        if (returnDate.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
            this.fine = Math.max(0, daysLate) * 5; // every late day result in 5rs fine
        } else {
            this.fine = 0;
        }
    }
    public long getFine() {
        return this.fine;
    }
}
