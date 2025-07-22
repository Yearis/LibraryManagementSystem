package library.models;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Member {
    private static int studentCounter = 0;
    private static int teacherCounter = 0;

    // starts with STU(for students) and TCH(for teachers)
    private final String memberID;// final because this id cant be changed later on
    private String occupation;
    private String name;
    private int age;// for age related books (this feature will be added later)
    private String phoneNumber;
    private String email;
    private String password;
    private String hashedPassword;
    private final LocalDate joinDate;
    private int maxBorrowLimit; // for Students its 3 and for Teachers its 5
    private long pendingDues = 0;

    // this list stores currently borrowed books of members
    private final ArrayList<Book> borrowedBooks = new ArrayList<>();

    public List<Book> getBorrowedBooks() {
        // this now prevents unnecessary modifications in our ArrayList and only shows a read only view of borrowed books which is not editable
        return Collections.unmodifiableList(this.borrowedBooks);
    }

    // New Methods to manage Books for Customer
    // adds books to the list
    public void addBookToBorrowedList(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Cannot add a null book to the borrowed list.");
        }
        this.borrowedBooks.add(book);
    }

    // removes books from list
    public void removeBookFromBorrowedList(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Cannot remove a null book from the borrowed list.");
        }
        this.borrowedBooks.remove(book);
    }

    // getters
    public String getOccupation() { return occupation; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getHashedPassword() { return hashedPassword; }
    public LocalDate getJoinDate() { return joinDate; }
    public int getMaxBorrowLimit() { return maxBorrowLimit; }
    public long getPendingDues() { return pendingDues; }
    public String getMemberID() { return memberID; }

    // setters
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }
    public void setMaxBorrowLimit(int maxBorrowLimit) { this.maxBorrowLimit = maxBorrowLimit; }
    public void setPendingDues(long fine) { this.pendingDues = fine; }

    public Member(String name, int age, String occupation, String email, String password, String phoneNumber) {
        // this generates users ID for a lifetime.
        this.memberID = generateID(occupation);
        this.setName(name);
        this.setAge(age);
        // throws exception if invalid email is provided
        this.setEmail(verifyEmail(email));
        // throws exception if an invalid password is provided
        this.setPassword(verifyPassword(password));

        // hashing the password for security this will be stored in SQL Table
        this.setHashedPassword(BCrypt.hashpw(this.getPassword(), BCrypt.gensalt()));
        this.setPassword(null); // doesn't store password

        joinDate = LocalDate.now();

        // we define the number of books that can be borrowed by each types of member later we can also add premium
        if (memberID.contains("STU")) {
            setMaxBorrowLimit(3);
        } else if (memberID.contains("TCH")) {
            setMaxBorrowLimit(5);
        }
    }

    // to change ones name
    protected void changeName(String name) {
        // will add a password verifier so that only after verifying your password you can change your username
        this.setName(name);
    }

//    private void changePassword(String memberID) {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("Enter your previous Password: ");
//        String password =
//    }

    // to check if email provided is correct or not, I will also add more functionalities later like verify email by clicking the link etc
    private String verifyEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Please enter an email address");
        }
        // if keeping email field blank or using fake emails or junk emails
        if ((!email.matches("^[A-Za-z0-9+_.-]+@(gmail\\.com|yahoo\\.com|outlook\\.com|icloud\\.com|protonmail\\.com|zoho\\.com)$"))) {
            // currently only added popular email domains to prevent usage of temp or junk email addresses
            throw new IllegalArgumentException("Invalid email! Use domains: @gmail.com, @yahoo.com, etc.");
        } return email;
    }

    // to check if a strong password is created or not
    private String verifyPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password field cannot be empty!!\n Please create a robust password");
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*[!@#$%^&*])(?=.*[a-z]).{6,}$")) {
            throw new IllegalArgumentException("Password must have: 1 uppercase, 1 lowercase, 1 special char and more than 6 characters.");
        }
        return password;
    }

    // to generate memberID based on user's occupation
    private String generateID(String occupation) {
        return switch (occupation.toLowerCase()) {
            // starts counting in 3 digits e.g. 001 (will change the limit if required)
            case "student" -> "STU_" + String.format("%03d", ++studentCounter);
            case "teacher" -> "TCH_" + String.format("%03d", ++teacherCounter);
            default -> throw new IllegalArgumentException("Invalid occupation!");
        };
    }

    public void payFine(String memberID, long amount) {
        if (amount > getPendingDues()) {
            throw new IllegalArgumentException("Amount is greater than dues");
        }
        this.setPendingDues(this.getPendingDues() - amount);

        if (this.getPendingDues() == 0) {
            System.out.println("Your dues have been cleared!!");
        }
        System.out.println("Your Current pending dues is: ₹ " + this.getPendingDues());
    }

}
