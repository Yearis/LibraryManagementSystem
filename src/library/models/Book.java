package library.models;

import java.time.LocalDate;

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
    public Transaction getCurrentTransaction() { return currentTransaction; }
    public void setCurrentTransaction(Transaction currentTransaction) { this.currentTransaction = currentTransaction; }


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
