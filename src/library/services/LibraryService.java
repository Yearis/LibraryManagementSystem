package library.services;

import library.models.Book;
import library.models.Member;
import library.models.Transaction;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.Objects;

public class LibraryService {

    public enum PeriodUnit { DAYS, WEEKS, MONTHS }

    private LocalDate getDueDate(LocalDate issueDate, Scanner sc) {
        Objects.requireNonNull(issueDate, "Issue Date cannot be Null");

        System.out.println("Choose borrow period unit:");
        System.out.println("1. DAYS");
        System.out.println("2. WEEKS");
        System.out.println("3. MONTHS");
        System.out.print("Enter choice (1-3): ");

        while (!sc.hasNextInt()) {
            System.out.println("Invalid input! Enter a number.");
            sc.next(); // Discard bad input
        }

        int choice = sc.nextInt();

        // We check the choice and on the basis of user input we put unit from PeriodUnit
        PeriodUnit unit = switch (choice) {
            case 1 -> PeriodUnit.DAYS;
            case 2 -> PeriodUnit.WEEKS;
            case 3 -> PeriodUnit.MONTHS;
            default -> throw new IllegalArgumentException("Invalid Choice!");
        };

        System.out.println("Enter the duration you want to borrow for (eg. '2' for 2 DAYS/WEEKS/MONTHS) : ");
        int duration = sc.nextInt();

        // here we convert the total number of days from user selection of unit
        int totalDays = switch (unit) {
            case DAYS -> duration;
            case WEEKS -> duration * 7;
            case MONTHS -> duration * 30;
        };

        if (totalDays <= 0 || totalDays > 90) {
            throw new IllegalArgumentException("Invalid Duration! Duration must be between 1 and 90 days.");
        }
        return issueDate.plusDays(totalDays);
    }


    public void borrowBook(Book book, Member member, Scanner sc) {
        Objects.requireNonNull(book, "Book cannot be null.");
        Objects.requireNonNull(member, "Member cannot be null.");
        Objects.requireNonNull(sc, "Scanner cannot be null.");

        if (member.getPendingDues() > 0) {
            throw new IllegalArgumentException("Clear pending dues before borrowing book.\n Your Pending Dues is: ₹ " + member.getPendingDues());
        } else {
            if (!book.isPresent()) {
                throw new IllegalArgumentException("This book has been permanently removed from our collection and is no longer available for borrowing.");
            } else if (!book.getAvailable()) {
                System.out.println("Sorry, the book " + book.getTitle() + " is is currently not available for borrowing.");
                return; // we exit the method if it's not available
            }

            // here we check if the selected book is appropriate for members age
            if (book.getContentRating().equalsIgnoreCase("PG-13") && member.getAge() < 13) {
                throw new IllegalArgumentException("Age Restriction: This content is only available for viewers 13 years of age or older.");
            } else if (book.getContentRating().equalsIgnoreCase("R") && member.getAge() < 18) {
                throw new IllegalArgumentException("Age Restriction: This content is only available for viewers 18 years of age or older.");
            }

            if (member.getBorrowedBooks().size() < member.getMaxBorrowLimit()) {

                LocalDate issueDate = LocalDate.now();
                LocalDate calculateDueDate = this.getDueDate(issueDate, sc);

                Transaction newTransaction = new Transaction(
                        member.getMemberID(),
                        book.getBookID(),
                        issueDate,
                        calculateDueDate
                );

                book.markAsBorrowed(newTransaction);

                member.addBookToBorrowedList(book); // the book now is borrowed

                System.out.println("Book '" + book.getTitle() + "' borrowed successfully by " + member.getName() + ".");
                System.out.println("Issue Date: " + newTransaction.getIssueDate());
                System.out.println("Due Date: " + newTransaction.getDueDate());

            } else {
                throw new IllegalArgumentException("Borrowed Limit Reached");
            }
        }
    }

    public void returnBook(Book book, Member member) {
        Objects.requireNonNull(book, "Book cannot be null.");
        Objects.requireNonNull(member, "Member cannot be null.");

        Transaction currentTransaction = book.getCurrentTransaction();

        if (currentTransaction == null) {
            throw  new IllegalStateException("Book " + book.getBookID() + " is not currently borrowed.");
        }

        // Check if the book is already returned or not
        if (currentTransaction.getReturnDate() != null) {
            throw new IllegalStateException("Book " + book.getBookID() + " already returned on " + currentTransaction.getReturnDate());
        }

        LocalDate returnDate = LocalDate.now();

        currentTransaction.setReturnDate(returnDate);

        // Calculates fine if returnDate is past dueDate
        currentTransaction.calculateFine();

        // this removes book from users borrowedBooks arraylist then marks its available and then puts today's date as return date
        member.removeBookFromBorrowedList(book);

        book.markAsReturned();

        // Sets Dues in Member Class
        member.setPendingDues(member.getPendingDues() + currentTransaction.getFine());

        System.out.println("Book '" + book.getTitle() + "' returned successfully by " + member.getName() + ".");
        System.out.println("Return Date: " + currentTransaction.getReturnDate());
        if (currentTransaction.getFine() > 0) {
            System.out.println("Fine incurred: ₹ " + currentTransaction.getFine());
            System.out.println("Member's total pending dues: ₹ " + member.getPendingDues());
        } else {
            System.out.println("No fine incurred.");
        }
    }

//    public void searchBook(Book book) {
//        // we here check if a book is available or not
//        // this will require an SQL Table
//    }

}
