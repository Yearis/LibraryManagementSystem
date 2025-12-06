package library.circulation.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Transaction {
    // global counter
    private static int transactionCounter = 100000;

    private final String transactionID;
    private final String memberID;
    private final String borrowedBookID;
    private final LocalDate issueDate;
    private LocalDate returnDate = null;
    private final LocalDate dueDate;
    private long fine;
    private int renewalCount;
    private LocalDate renewalDate;
    private LocalDate extendedDueDate;

    // getters
    public String getTransactionID() { return transactionID; }
    public String getMemberID() { return memberID; }
    public String getBorrowedBookID() { return borrowedBookID; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getReturnDate() { return this.returnDate; }
    public long getFine() { return this.fine; }
    public LocalDate getRenewalDate() {return renewalDate; }
    public LocalDate getDueDate() {
        // this always returns the active dueDate throughout the transaction
        return (extendedDueDate != null) ? extendedDueDate : dueDate;
    }


    public Transaction(String memberID, String  borrowedBookID, LocalDate issueDate, LocalDate dueDate) {
        this.memberID = memberID;
        this.borrowedBookID = borrowedBookID;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.extendedDueDate = null; // currently its null
        this.renewalCount = 0; // currently its 0 and we will increment it for every renewal
        this.returnDate = null;

        this.transactionID = "TR_" + String.format("%06d", ++transactionCounter);
    }

    public void setReturnDate(LocalDate returnDate) {
        Objects.requireNonNull(returnDate, "Return date cannot be null");
        this.returnDate = returnDate;
    }


    public void calculateFine() {
        if (this.returnDate == null) {
            throw new IllegalStateException("Return date must be set before calculating fine.");
        }

        if (returnDate.isAfter(this.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(this.getDueDate(), this.getReturnDate());
            this.fine = Math.max(0, daysLate) * 5; // every late day result in 5rs fine
        } else {
            this.fine = 0; // No fine if returned before or on due date
        }
    }

    public void extendDueDate(int totalDays) {

        if (this.returnDate != null) {
            // meaning book has already been returned
            throw new IllegalStateException("Cannot extend due date for already returned book");
        }
        // First we check if user is eligible to renewBook or not
        if (renewalCount > 0) {
            throw new IllegalStateException("The Book can only be renewed once per Transaction");
        }

        // check
        if (totalDays <= 0 || totalDays > 14) {
            throw new IllegalArgumentException("Renewal date should be between 1 - 14 days");
        }

        // First, we have to set the renewal date as today
        this.renewalDate = LocalDate.now();
        this.extendedDueDate = getDueDate().plusDays(totalDays);
        this.renewalCount++; // increase the counter

        // resets the fine for user
        this.fine = 0;
    }

    public void resetFine() {
        this.fine = 0;
    }

}
