package library.models;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class Member {
    private static int studentCounter = 0;
    private static int teacherCounter = 0;

    // starts with STU(for students) and TCH(for teachers)
    public final String memberID;
    private String occupation;// final because this id cant be changed later on
    private String name;
    private int age;// for age related books (this feature will be added later)
    private String phoneNumber;
    private String email;
    private String password;
    private String hashedPassword;
    private final LocalDate joinDate;
    private int maxBorrowLimit; // for Students its 3 and for Teachers its 5
    long pendingDues = 0;

    // this list stores currently borrowed books of members
    ArrayList<Book> borrowedBooks = new ArrayList<>();

    public String getOccupation() { return occupation; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getHashedPassword() { return hashedPassword; }
    public LocalDate getJoinDate() { return joinDate; }
    public int getMaxBorrowLimit() { return maxBorrowLimit; }

    public Member(String name, int age, String occupation, String email, String password, String phoneNumber) {
        // this generates users ID for a lifetime.
        this.memberID = generateID(occupation);
        this.name = name;
        this.age = age;
        // throws exception if invalid email is provided
        this.email = verifyEmail(email);
        // throws exception if an invalid password is provided
        this.password = verifyPassword(password);

        // hashing the password for security this will be stored in SQL Table
        this.hashedPassword = BCrypt.hashpw(this.password, BCrypt.gensalt());
        this.password = null; // doesn't store password

        joinDate = LocalDate.now();

        // we define the number of books that can be borrowed by each types of member later we can also add premium
        if (memberID.contains("STU")) {
            maxBorrowLimit = 3;
        } else if (memberID.contains("TCH")) {
            maxBorrowLimit = 5;
        }
    }

    // to change ones name
    protected void changeName(String name) {
        this.name = name;
    }

//    private void changePassword(String memberID) {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("Enter your previous Password: ");
//        String password =
//    }

    // to check if email provided is correct or not, I will also add more functionalities later like verify email by clicking the link etc
    private String verifyEmail(String email) {
        // if keeping email field blank or using fake emails or junk emails
        if (email == null || (!email.matches("^[A-Za-z0-9+_.-]+@(gmail\\.com|yahoo\\.com|outlook\\.com|icloud\\.com|protonmail\\.com|zoho\\.com)$"))) {
            throw new IllegalArgumentException("Invalid email! Use domains: @gmail.com, @yahoo.com, etc.");
        } return email;
    }

    // to check if a strong password is created or not
    private String verifyPassword(String password) {
        if (password == null || !password.matches("^(?=.*[A-Z])(?=.*[!@#$%^&*])(?=.*[a-z]).{6,}$")) {
            throw new IllegalArgumentException("Password must have: 1 uppercase, 1 lowercase, 1 special char and more than 6 characters.");
        }
        return password;
    }

    // to generate memberID
    private String generateID(String occupation) {
        return switch (occupation.toLowerCase()) {
            case "student" -> "STU_" + String.format("%03d", ++studentCounter);
            case "teacher" -> "TCH_" + String.format("%03d", ++teacherCounter);
            default -> throw new IllegalArgumentException("Invalid occupation!");
        };
    }

    void setPendingDues(long fine) {
        this.pendingDues = fine;
    }

    public void payFine(String memberID, long amount) {
        if (amount > pendingDues) {
            throw new IllegalArgumentException("Amount is greater than dues");
        }
        this.pendingDues -= amount;

        if (this.pendingDues == 0) {
            System.out.println("Your dues have been cleared!!");
        }
        System.out.println("Your Current pending dues is: ₹ " + this.pendingDues);
    }
}
