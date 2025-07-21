package library.models;

import library.services.LibraryService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Scanner;

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
    private LocalDate extendedDueDate;

    // getters
    public String getTransactionID() { return transactionID; }
    public String getMemberID() { return memberID; }
    public String getBorrowedBookID() { return borrowedBookID; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return (extendedDueDate != null) ? extendedDueDate : dueDate; }
    public LocalDate getReturnDate() { return this.returnDate; }
    public long getFine() { return this.fine; }


    public Transaction(String memberID, String  borrowedBookID, LocalDate issueDate, LocalDate dueDate) {
        this.memberID = memberID;
        this.borrowedBookID = borrowedBookID;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.extendedDueDate = null; // currently its null
        this.renewalCount = 0; // currently its 0 max it can go is 1 for every transaction

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

        if (returnDate.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
            this.fine = Math.max(0, daysLate) * 5; // every late day result in 5rs fine
        } else {
            this.fine = 0; // No fine if returned before or on due date
        }
    }

    public void extendDueDate(Scanner sc) {

    }

}
