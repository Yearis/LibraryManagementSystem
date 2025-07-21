package library.models;

public class Book {
    private static int bookCounter = 1000;

    private final String bookID;
    private String title;
    private String author;
    private String genre;
    private String contentRating; // {G, PG-13, R}
    private Boolean isAvailable;
    private boolean isPresent = true;

    // getters
    public String getBookID() { return bookID; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getContentRating() { return contentRating; }
    public Boolean getAvailable() { return isAvailable; }
    public boolean isPresent() { return isPresent; }

    // setters
    void setTitle(String title) { this.title = title; }
    void setAuthor(String author) { this.author = author; }
    void setGenre(String genre) { this.genre = genre; }
    void setContentRating(String contentRating) { this.contentRating = contentRating; }
    void setAvailable(Boolean available) { isAvailable = available; }
    void setPresent(boolean present) { isPresent = present; }

    private Transaction currentTransaction;
    public Transaction getCurrentTransaction() { return currentTransaction; }
    void setCurrentTransaction(Transaction currentTransaction) { this.currentTransaction = currentTransaction; }


    public Book (String title, String author, String genre, String contentRating) {

        this.bookID = generateUniqueCode(genre) + "_" + ++bookCounter;

        this.setTitle(title);
        this.setAuthor(author);
        this.setGenre(genre);
        this.setContentRating(contentRating);
        this.setAvailable(true);

        // here we will add these books in our future sql table
    }

    public void removeBook() {

        if (!this.isPresent) {
            throw new IllegalStateException("Book " + this.bookID + " is already removed");
        }
        // here we remove the book from sql table, but we keep the bookID for future purposes it will just show book not available now
        this.setAvailable(false);
        this.setPresent(false);
        currentTransaction = null;
    }

    public void markAsBorrowed(Transaction transaction) {
        if (!this.isAvailable || !this.isPresent) {
            // Provides a specific message based on availability/presence
            String reason = !this.isAvailable ? "not available for borrowing" : "has been removed";
            throw new IllegalStateException("Book " + this.bookID + " cannot be borrowed because it is " + reason + ".");
        }

        setAvailable(false);
        setCurrentTransaction(transaction);
    }

    public void markAsReturned() {
        if (this.isAvailable) { // If it's available, it wasn't out on loan
            throw new IllegalStateException("Book " + this.bookID + " is already marked as available.");
        }
        if (this.currentTransaction == null) { // Must have a current transaction to be returned
            throw new IllegalStateException("Book " + this.bookID + " was not currently borrowed (no active transaction).");
        }

        setAvailable(true);
        setCurrentTransaction(null); // Transaction completed
    }

    private String generateUniqueCode(String genre) {

        // splits the input "genre" into words using spaces
        String[] words = genre.trim().split("\\s+");
        StringBuilder uniqueCode = new StringBuilder();

        // Takes the first 3 characters of the first word, or the whole word if it is shorter than 3
        uniqueCode.append(words[0].substring(0, Math.min(words[0].length(), 3)).toUpperCase());

        // For subsequent words, we take the first 2 chars, or the whole word if shorter
        for (int i = 1; i < words.length; i++) {
            uniqueCode.append(words[i].substring(0, Math.min(words[i].length(), 2)).toUpperCase());
        }

        return uniqueCode.toString();
    }

}
