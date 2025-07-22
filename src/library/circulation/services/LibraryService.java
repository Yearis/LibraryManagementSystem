package library.circulation.services;

import library.circulation.models.Book;
import library.circulation.models.Member;
import library.circulation.models.Transaction;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.Objects;

public class LibraryService {

    public enum PeriodUnit { DAYS, WEEKS, MONTHS }

    private LocalDate getDueDate(LocalDate issueDate, Scanner sc) {
        Objects.requireNonNull(issueDate, "Issue Date cannot be Null");

        System.out.println("Choose borrow period unit: ");
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

        // set return date before calculating fine
        currentTransaction.setReturnDate(returnDate);

        // Calculates fine if returnDate is past dueDate
        currentTransaction.calculateFine();

        // Sets Dues in Member Class
        member.setPendingDues(member.getPendingDues() + currentTransaction.getFine());

        currentTransaction.resetFine();

        // this removes book from users borrowedBooks arraylist then marks its available and then puts today's date as return date
        member.removeBookFromBorrowedList(book);

        book.markAsReturned();

        System.out.println("Book '" + book.getTitle() + "' returned successfully by " + member.getName() + ".");
        System.out.println("Return Date: " + currentTransaction.getReturnDate());
        if (currentTransaction.getFine() > 0) {
            System.out.println("Fine incurred: ₹ " + currentTransaction.getFine());
            System.out.println("Member's total pending dues: ₹ " + member.getPendingDues());
        } else {
            System.out.println("No fine incurred.");
        }
    }

    public void renewBook(Book book, Member member, Scanner sc) {
        Objects.requireNonNull(book, "Book cannot be null.");
        Objects.requireNonNull(member, "Member cannot be null.");

        // we took the current transaction of the book which hold its current transaction
        Transaction currentTransaction = book.getCurrentTransaction();

        // to check if it's even borrowed at all
        if (currentTransaction == null) {
            throw new IllegalStateException("The book " + book.getBookID() + " is currently not borrowed");
        } else if (!currentTransaction.getMemberID().equals(member.getMemberID())) {
            // now from books current transaction we can get the id of the member currently borrowing it
            throw new IllegalStateException("The book " + book.getBookID() + " is currently borrowed by someone else");
        } else {
            // if the book is actually borrowed and by the correct member
            // now we can continue with our renewal process


            // before asking for renewal time, we'll check for any late submissions
            LocalDate currentDate = LocalDate.now();

            if (currentDate.isAfter(currentTransaction.getDueDate())) {
                // this sets fine
                currentTransaction.calculateFine();
                member.setPendingDues(member.getPendingDues() + currentTransaction.getFine());
            }

            // here we ask for what kind of period the user wants to renew
            System.out.println("Choose borrow period unit: ");
            System.out.println("1. DAYS");
            System.out.println("2. WEEKS");
            System.out.println("Enter Choice 1 or 2: ");

            int choice = sc.nextInt();

            PeriodUnit unit = switch (choice) {
                case 1 -> PeriodUnit.DAYS;
                case 2 -> PeriodUnit.WEEKS;
                default -> throw new IllegalArgumentException("Invalid Choice!");
            };

            System.out.println("Enter the duration (max limit for renewal is 14 days or 2 weeks): ");
            int duration = sc.nextInt();

            int totalDays = 0;
            if (unit == PeriodUnit.DAYS) {
                totalDays = duration;
            } else if (unit == PeriodUnit.WEEKS) {
                totalDays = duration * 7;
            }

            if (totalDays <= 0 || totalDays > 14) {
                throw new IllegalArgumentException("Renewal duration is between 1 - 14 days.");
            }

            // this passes renewal info to Transaction to handle everything
            currentTransaction.extendDueDate(totalDays);
            System.out.println("Book '" + book.getTitle() + "' successfully renewed!");
            System.out.println("New Due Date: " + currentTransaction.getDueDate());
            System.out.println("Renewal recorded on: " + currentTransaction.getRenewalDate()); // Good for transparency
        }
    }

//    public void searchBook(Book book) {
//        // we here check if a book is available or not
//        // this will require an SQL Table
//    }

}
