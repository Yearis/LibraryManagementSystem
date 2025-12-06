package library;

import library.circulation.models.Book;
import library.circulation.models.Member;
import library.circulation.services.LibraryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // --- 1. SETUP ---
        LibraryService libraryService = new LibraryService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=============================================");
        System.out.println("      LIBRARY MANAGEMENT SYSTEM - TEST       ");
        System.out.println("=============================================\n");

        // --- 2. CREATE SAMPLE DATA ---
        System.out.println("--- Creating Sample Books and Members ---");
        List<Book> bookCatalog = new ArrayList<>();
        bookCatalog.add(new Book("The Hobbit", "J.R.R. Tolkien", "Fantasy", "G"));
        bookCatalog.add(new Book("Dune", "Frank Herbert", "Science Fiction", "PG-13"));
        bookCatalog.add(new Book("1984", "George Orwell", "Dystopian", "R"));
        bookCatalog.add(new Book("A Brief History of Time", "Stephen Hawking", "Science", "G"));

        // Use valid emails that match the validation criteria in Member.java
        Member studentMember = new Member("Alice", 20, "Student", "alice@gmail.com", "Password@123", "1234567890");
        Member teacherMember = new Member("Mr. Smith", 45, "Teacher", "smith@yahoo.com", "TeacherPass!1", "0987654321");

        // Let's get our books from the catalog for easier access
        Book hobbitBook = bookCatalog.get(0);
        Book duneBook = bookCatalog.get(1);

        printMemberStatus(studentMember);
        printBookStatus(hobbitBook);
        System.out.println("\n--- Setup Complete ---\n");


        // --- 3. TEST SCENARIOS ---

        // SCENARIO 1: Successful Borrowing
        System.out.println("--- SCENARIO 1: Student Borrows a Book (Success) ---");
        try {
            // We simulate user input "1" for DAYS and "14" for duration
            System.out.println("Alice is borrowing 'The Hobbit'. Please enter '1' for DAYS, then '14' for the duration.");
            libraryService.borrowBook(hobbitBook, studentMember, scanner);
            printMemberStatus(studentMember);
            printBookStatus(hobbitBook);
        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
        }
        System.out.println("\n--------------------------------------------------\n");


        // SCENARIO 2: Attempt to Borrow with Pending Dues (Failure)
        System.out.println("--- SCENARIO 2: Student Tries to Borrow Another Book with Dues (Failure) ---");
        try {
            // Manually set dues to simulate a fine
            studentMember.setPendingDues(50);
            System.out.println("!! Manually setting Alice's pending dues to " + studentMember.getPendingDues() + " for this test.");
            libraryService.borrowBook(duneBook, studentMember, scanner);
        } catch (Exception e) {
            System.out.println("TEST PASSED: Caught expected error -> " + e.getMessage());
            // Clear dues for the next test
            studentMember.payFine(50);
        }
        printMemberStatus(studentMember);
        System.out.println("\n--------------------------------------------------\n");


        // SCENARIO 3: Successful Return (No Fine)
        System.out.println("--- SCENARIO 3: Student Returns 'The Hobbit' (Success) ---");
        try {
            libraryService.returnBook(hobbitBook, studentMember);
            printMemberStatus(studentMember);
            printBookStatus(hobbitBook);
        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
        }
        System.out.println("\n--------------------------------------------------\n");


        // SCENARIO 4: Successful Renewal
        System.out.println("--- SCENARIO 4: Teacher Borrows and Renews a Book (Success) ---");
        try {
            // First, Mr. Smith borrows the Dune book
            System.out.println("Mr. Smith is borrowing 'Dune'. Please enter '1' for DAYS, then '7' for the duration.");
            libraryService.borrowBook(duneBook, teacherMember, scanner);
            System.out.println("\nNow, Mr. Smith will renew 'Dune'.");
            System.out.println("Please enter '1' for DAYS, then '10' for the renewal duration.");
            libraryService.renewBook(duneBook, teacherMember, scanner);
            printMemberStatus(teacherMember);
            printBookStatus(duneBook);
        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
        }
        System.out.println("\n--------------------------------------------------\n");

        // SCENARIO 5: Return a book with a fine (We can't change the system date, so we assume it's overdue)
        System.out.println("--- SCENARIO 5: Teacher Returns 'Dune' (Assuming it's overdue) ---");
        System.out.println("NOTE: To properly test fines, the due date would need to be in the past. We are testing the logic flow.");
        try {
            // To simulate a fine, we can't change the system clock.
            // The logic inside returnBook() will correctly calculate a fine if LocalDate.now() is after the due date.
            // For this test, we just call return and check the output.
            libraryService.returnBook(duneBook, teacherMember);
            printMemberStatus(teacherMember);

            // SCENARIO 6: Pay the fine
            if (teacherMember.getPendingDues() > 0) {
                System.out.println("\n--- SCENARIO 6: Teacher Pays Fine (Success) ---");
                teacherMember.payFine(teacherMember.getPendingDues());
                printMemberStatus(teacherMember);
            } else {
                System.out.println("No fine was incurred, skipping fine payment test.");
            }

        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
        }


        // --- End of Tests ---
        System.out.println("\n=============================================");
        System.out.println("              TESTING COMPLETE               ");
        System.out.println("=============================================\n");

        scanner.close();
    }

    /**
     * Helper method to print the current status of a Member.
     * @param member The member whose status will be printed.
     */
    private static void printMemberStatus(Member member) {
        System.out.println(
                String.format("\t> MEMBER STATUS [ID: %s, Name: %s] -> Borrowed: %d, Dues: ₹%d",
                        member.getMemberID(),
                        member.getName(),
                        member.getBorrowedBooks().size(),
                        member.getPendingDues()
                )
        );
    }

    /**
     * Helper method to print the current status of a Book.
     * @param book The book whose status will be printed.
     */
    private static void printBookStatus(Book book) {
        System.out.println(
                String.format("\t> BOOK STATUS [ID: %s, Title: %s] -> Available: %b, Transaction: %s",
                        book.getBookID(),
                        book.getTitle(),
                        book.getAvailable(),
                        (book.getCurrentTransaction() != null ? book.getCurrentTransaction().getTransactionID() : "None")
                )
        );
    }
}
