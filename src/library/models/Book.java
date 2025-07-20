package library.models;

import java.time.LocalDate;
import java.util.Scanner;

public class Book {
    private static int bookCounter = 1000;

    private String bookID;
    private String title;
    private String author;
    private String genre;
    private String contentRating; // {G, PG-13, R}
    private Boolean isAvailable;
    private LocalDate issueDate;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private boolean isPresent = true;

    // getters
    public String getBookID() { return bookID; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getContentRating() { return contentRating; }
    public Boolean getAvailable() { return isAvailable; }
    public boolean isPresent() { return isPresent; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LocalDate getDueDate() { return dueDate; }

    // setters
    public void setBookID(String bookID) { this.bookID = bookID; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setContentRating(String contentRating) { this.contentRating = contentRating; }
    public void setAvailable(Boolean available) { isAvailable = available; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPresent(boolean present) { isPresent = present; }

    private Transaction currentTransaction;

    public void addBooks(String title, String author, String genre, String contentRating) {

        this.setBookID(generateUniqueCode(genre) + "_" + ++bookCounter);

        this.setTitle(title);
        this.setAuthor(author);
        this.setGenre(genre);
        this.setContentRating(contentRating);
        this.setAvailable(true);

        // here we will add these books in our future sql table
    }

    public void removeBook(String bookID) {
        // here we remove the book from sql table, but we keep the bookID for future purposes it will just show book not available now
        this.setAvailable(false);
        this.setPresent(false);
    }


    public enum PeriodUnit { DAYS, WEEKS, MONTHS }

    public void searchBook(Book book) {
        // we here check if a book is available or not
        // this will require an SQL Table
    }

    public void borrowBook(Book book, Member member, Scanner sc) {
        if (member.pendingDues > 0) {
            throw new IllegalArgumentException("Clear pending dues before borrowing book.\n Your Pending Dues is: ₹ " + member.pendingDues);
        } else {
            if (!book.isPresent()) {
                throw new IllegalArgumentException("This book has been permanently removed from our collection and is no longer available for borrowing.");
            } else if (book.getAvailable()) {
                // here we check if the selected book is appropriate for members age
                if (getContentRating().equalsIgnoreCase("PG-13") && member.getAge() < 13) {
                    throw new IllegalArgumentException("Age Restriction: This content is only available for viewers 13 years of age or older.");
                } else if (getContentRating().equalsIgnoreCase("R") && member.getAge() < 18) {
                    throw new IllegalArgumentException("Age Restriction: This content is only available for viewers 18 years of age or older.");
                }

                if (member.borrowedBooks.size() < member.getMaxBorrowLimit()) {
                    member.borrowedBooks.add(book);
                    book.setAvailable(false); // the book now is borrowed
                    book.setIssueDate(LocalDate.now());
                    book.setDueDate(getDueDate(sc));

                    this.currentTransaction = new Transaction(
                            member.memberID,
                            this.getBookID(),
                            this.getIssueDate(),
                            this.getDueDate()
                    );
                } else {
                    throw new IllegalArgumentException("Borrowed Limit Reached");
                }
            } else {
                System.out.println("Sorry the book currently is not available!");
            }
        }
    }

    public void returnBook(Book book, Member member) {
        // Check if the book is already returned or not
        if (currentTransaction.getReturnDate() != null) {
            throw new IllegalStateException("Book " + book.getBookID() + " already returned on " + currentTransaction.getReturnDate());
        }

        // this removes book from users borrowedBooks arraylist then marks its available and then puts today's date as return date
        member.borrowedBooks.remove(book);
        book.setReturnDate(LocalDate.now());
        this.currentTransaction.setReturnDate();

        // Calculates fine if returnDate is past dueDate
        currentTransaction.calculateFine(getReturnDate(), getDueDate());

        // Sets Dues in Member Class
        member.setPendingDues(currentTransaction.getFine());

        book.setAvailable(true);
    }

    private LocalDate getDueDate(Scanner sc) {

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
        setDueDate(getIssueDate().plusDays(totalDays));
        return getDueDate();
    }

    // this stores our codes
//    HashMap<String, Integer> codeCounts = new HashMap<>();

    private String generateUniqueCode(String genre) {

        String[] words = genre.trim().split("\\s+");

        StringBuilder uniqueCode = new StringBuilder();
        if (genre.length() >= 3) {
            uniqueCode.append(words[0].substring(0, 3).toUpperCase());
        } else { // for genres whose length is less than 3 (e.g. AI)
            uniqueCode.append(words[0].substring(0, genre.length() + 1).toUpperCase());
        }


        // if genre has more than one word (e.g. Science Fiction)
        for (int i = 1; i < words.length; i++) {
            // here "Science Fiction" becomes SCIFI
            uniqueCode.append(words[i], 0, 2);
        }

        // check if a uniqueCode is present in the map or not and sets the counter for that
//        int count = codeCounts.getOrDefault(uniqueCode.toString(), 0);
//
//        codeCounts.put(uniqueCode.toString(), count + 1);
        return uniqueCode.toString();
    }
}
