import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * SmartBank - a beginner-friendly, file-based Java banking project.
 * Educational demo only: do not use for real banking or sensitive data.
 */
public class SmartBank {
    private static final Scanner SC = new Scanner(System.in);
    private static final Path DATA_DIR = Paths.get("smartbank-data");
    private static final Path ACCOUNTS_FILE = DATA_DIR.resolve("accounts.txt");
    private static final Path TRANSACTIONS_FILE = DATA_DIR.resolve("transactions.txt");
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<Account> ACCOUNTS = new ArrayList<Account>();
    private static final List<String> TRANSACTIONS = new ArrayList<String>();
    private static int nextAccountNumber = 100001;

    public static void main(String[] args) {
        loadData();
        printWelcome();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ", 0, 3);

            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    printProjectInfo();
                    break;
                case 0:
                    saveData();
                    System.out.println("\nThank you for using SmartBank. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
        SC.close();
    }

    private static void printWelcome() {
        System.out.println("==================================================");
        System.out.println("                 WELCOME TO SMARTBANK             ");
        System.out.println("          Java Banking Management System          ");
        System.out.println("==================================================");
        System.out.println("Your data is stored locally in the project folder.");
        System.out.println("Educational demo only - not for real banking.");
    }

    private static void printMainMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1. Create a new account");
        System.out.println("2. Login to an existing account");
        System.out.println("3. About this project");
        System.out.println("0. Save and exit");
        System.out.println("--------------------------------------------");
    }

    private static void createAccount() {
        System.out.println("\n============== CREATE ACCOUNT ==============");
        String name = readNonEmpty("Full name: ");
        String pin = readPin("Create a 4-digit PIN: ");

        BigDecimal openingDeposit = readMoney("Opening deposit (Rs. 0 or more): ", true);
        Account account = new Account(nextAccountNumber++, name, pin, openingDeposit);
        ACCOUNTS.add(account);

        addTransaction(account.accountNumber, "ACCOUNT CREATED", openingDeposit,
                "Opening balance");
        saveData();

        System.out.println("\nAccount created successfully!");
        printAccountSummary(account);
        System.out.println("Remember your account number and PIN.");
    }

    private static void login() {
        System.out.println("\n================ ACCOUNT LOGIN ================");
        int accountNumber = readInt("Account number: ", 100001, Integer.MAX_VALUE);
        Account account = findAccount(accountNumber);

        if (account == null) {
            System.out.println("No account found with that number.");
            return;
        }

        String pin = readPin("4-digit PIN: ");
        if (!account.pin.equals(pin)) {
            System.out.println("Incorrect PIN. Login cancelled.");
            return;
        }

        System.out.println("\nWelcome, " + account.name + "!");
        accountMenu(account);
    }

    private static void accountMenu(Account account) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n============== ACCOUNT MENU ==============");
            System.out.println("Account: " + account.accountNumber + " | Name: " + account.name);
            System.out.println("1. View account details");
            System.out.println("2. Check balance");
            System.out.println("3. Deposit money");
            System.out.println("4. Withdraw money");
            System.out.println("5. Transfer money");
            System.out.println("6. View transaction history");
            System.out.println("7. Change PIN");
            System.out.println("8. Close account");
            System.out.println("0. Logout");
            System.out.println("===========================================");
            int choice = readInt("Choose an option: ", 0, 8);

            switch (choice) {
                case 1:
                    printAccountSummary(account);
                    break;
                case 2:
                    System.out.println("Available balance: Rs. " + money(account.balance));
                    break;
                case 3:
                    deposit(account);
                    break;
                case 4:
                    withdraw(account);
                    break;
                case 5:
                    transfer(account);
                    break;
                case 6:
                    showTransactions(account.accountNumber);
                    break;
                case 7:
                    changePin(account);
                    break;
                case 8:
                    if (closeAccount(account)) {
                        loggedIn = false;
                    }
                    break;
                case 0:
                    System.out.println("Logged out successfully.");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void deposit(Account account) {
        System.out.println("\n---------------- DEPOSIT ----------------");
        BigDecimal amount = readMoney("Amount to deposit (Rs.): ", false);
        account.balance = account.balance.add(amount);
        addTransaction(account.accountNumber, "DEPOSIT", amount, "Cash deposit");
        saveData();
        System.out.println("Deposit successful. New balance: Rs. " + money(account.balance));
    }

    private static void withdraw(Account account) {
        System.out.println("\n---------------- WITHDRAW ----------------");
        BigDecimal amount = readMoney("Amount to withdraw (Rs.): ", false);
        if (amount.compareTo(account.balance) > 0) {
            System.out.println("Insufficient balance. No money was withdrawn.");
            return;
        }
        account.balance = account.balance.subtract(amount);
        addTransaction(account.accountNumber, "WITHDRAWAL", amount, "Cash withdrawal");
        saveData();
        System.out.println("Withdrawal successful. New balance: Rs. " + money(account.balance));
    }

    private static void transfer(Account sender) {
        System.out.println("\n---------------- TRANSFER ----------------");
        int recipientNumber = readInt("Recipient account number: ", 100001, Integer.MAX_VALUE);
        if (recipientNumber == sender.accountNumber) {
            System.out.println("You cannot transfer money to the same account.");
            return;
        }

        Account recipient = findAccount(recipientNumber);
        if (recipient == null) {
            System.out.println("Recipient account not found.");
            return;
        }

        BigDecimal amount = readMoney("Amount to transfer (Rs.): ", false);
        if (amount.compareTo(sender.balance) > 0) {
            System.out.println("Insufficient balance. Transfer cancelled.");
            return;
        }

        sender.balance = sender.balance.subtract(amount);
        recipient.balance = recipient.balance.add(amount);
        addTransaction(sender.accountNumber, "TRANSFER SENT", amount,
                "To account " + recipient.accountNumber);
        addTransaction(recipient.accountNumber, "TRANSFER RECEIVED", amount,
                "From account " + sender.accountNumber);
        saveData();

        System.out.println("Transfer successful.");
        System.out.println("Transferred: Rs. " + money(amount));
        System.out.println("New balance: Rs. " + money(sender.balance));
    }

    private static void showTransactions(int accountNumber) {
        System.out.println("\n-------------- TRANSACTION HISTORY --------------");
        boolean found = false;
        for (String transaction : TRANSACTIONS) {
            String[] fields = transaction.split("\\|", -1);
            if (fields.length >= 5 && fields[1].equals(String.valueOf(accountNumber))) {
                found = true;
                System.out.println(fields[0] + " | " + fields[2] + " | Rs. " +
                        fields[3] + " | " + decode(fields[4]));
            }
        }
        if (!found) {
            System.out.println("No transactions found for this account.");
        }
    }

    private static void changePin(Account account) {
        System.out.println("\n---------------- CHANGE PIN ----------------");
        String oldPin = readPin("Current 4-digit PIN: ");
        if (!account.pin.equals(oldPin)) {
            System.out.println("Incorrect current PIN. PIN was not changed.");
            return;
        }
        String newPin = readPin("New 4-digit PIN: ");
        if (newPin.equals(oldPin)) {
            System.out.println("New PIN must be different from the current PIN.");
            return;
        }
        account.pin = newPin;
        addTransaction(account.accountNumber, "PIN CHANGED", BigDecimal.ZERO,
                "Account security update");
        saveData();
        System.out.println("PIN changed successfully.");
    }

    private static boolean closeAccount(Account account) {
        System.out.println("\n---------------- CLOSE ACCOUNT ----------------");
        if (account.balance.compareTo(BigDecimal.ZERO) != 0) {
            System.out.println("Account cannot be closed while its balance is not zero.");
            System.out.println("Withdraw or transfer the remaining balance first.");
            return false;
        }

        String confirm = readNonEmpty("Type CLOSE to confirm account closure: ");
        if (!"CLOSE".equals(confirm)) {
            System.out.println("Account closure cancelled.");
            return false;
        }

        ACCOUNTS.remove(account);
        addTransaction(account.accountNumber, "ACCOUNT CLOSED", BigDecimal.ZERO,
                "Account closed by holder");
        saveData();
        System.out.println("Account closed successfully.");
        return true;
    }

    private static void printAccountSummary(Account account) {
        System.out.println("\n============== ACCOUNT DETAILS ==============");
        System.out.println("Account number : " + account.accountNumber);
        System.out.println("Account holder : " + account.name);
        System.out.println("Current balance: Rs. " + money(account.balance));
        System.out.println("=============================================");
    }

    private static void printProjectInfo() {
        System.out.println("\nSmartBank is a Java console project demonstrating:");
        System.out.println("- Classes and objects");
        System.out.println("- ArrayList collections");
        System.out.println("- Methods, loops, and switch statements");
        System.out.println("- Input validation and BigDecimal money values");
        System.out.println("- File handling and persistent data");
        System.out.println("- Basic account workflows and transaction records");
    }

    private static Account findAccount(int accountNumber) {
        for (Account account : ACCOUNTS) {
            if (account.accountNumber == accountNumber) {
                return account;
            }
        }
        return null;
    }

    private static void addTransaction(int accountNumber, String type,
                                       BigDecimal amount, String note) {
        String line = LocalDateTime.now().format(TIME_FORMAT) + "|" +
                accountNumber + "|" + type + "|" + money(amount) + "|" + encode(note);
        TRANSACTIONS.add(line);
    }

    private static void loadData() {
        try {
            Files.createDirectories(DATA_DIR);

            if (Files.exists(ACCOUNTS_FILE)) {
                List<String> lines = Files.readAllLines(ACCOUNTS_FILE, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String[] fields = line.split("\\|", -1);
                    if (fields.length != 4) continue;
                    try {
                        int number = Integer.parseInt(fields[0]);
                        String name = decode(fields[1]);
                        String pin = fields[2];
                        BigDecimal balance = new BigDecimal(fields[3]);
                        ACCOUNTS.add(new Account(number, name, pin, balance));
                        if (number >= nextAccountNumber) nextAccountNumber = number + 1;
                    } catch (NumberFormatException ignored) {
                        System.out.println("Skipped one invalid account record.");
                    }
                }
            }

            if (Files.exists(TRANSACTIONS_FILE)) {
                TRANSACTIONS.addAll(Files.readAllLines(TRANSACTIONS_FILE, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.out.println("Warning: saved data could not be fully loaded: " + e.getMessage());
        }
    }

    private static void saveData() {
        try {
            Files.createDirectories(DATA_DIR);
            List<String> accountLines = new ArrayList<String>();
            for (Account account : ACCOUNTS) {
                accountLines.add(account.accountNumber + "|" + encode(account.name) + "|" +
                        account.pin + "|" + money(account.balance));
            }
            Files.write(ACCOUNTS_FILE, accountLines, StandardCharsets.UTF_8);
            Files.write(TRANSACTIONS_FILE, TRANSACTIONS, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Warning: data could not be saved: " + e.getMessage());
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = SC.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("Enter a number between " + min + " and " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static BigDecimal readMoney(String prompt, boolean allowZero) {
        while (true) {
            System.out.print(prompt);
            String input = SC.nextLine().trim();
            try {
                BigDecimal amount = new BigDecimal(input).setScale(2, RoundingMode.UNNECESSARY);
                int comparison = amount.compareTo(BigDecimal.ZERO);
                if (comparison < 0 || (!allowZero && comparison == 0)) {
                    System.out.println(allowZero
                            ? "Amount cannot be negative."
                            : "Amount must be greater than zero.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException | ArithmeticException e) {
                System.out.println("Enter a valid amount with at most 2 decimal places, e.g. 250 or 250.50.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SC.nextLine().trim();
            if (!value.isEmpty() && !value.contains("|")) return value;
            System.out.println("Please enter a non-empty value without the | symbol.");
        }
    }

    private static String readPin(String prompt) {
        while (true) {
            System.out.print(prompt);
            String pin = SC.nextLine().trim();
            if (pin.matches("\\d{4}")) return pin;
            System.out.println("PIN must contain exactly 4 digits.");
        }
    }

    private static String money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String encode(String value) {
        return value.replace("%", "%25").replace("|", "%7C")
                .replace("\n", "%0A").replace("\r", "%0D");
    }

    private static String decode(String value) {
        return value.replace("%0D", "\r").replace("%0A", "\n")
                .replace("%7C", "|").replace("%25", "%");
    }

    private static class Account {
        private final int accountNumber;
        private final String name;
        private String pin;
        private BigDecimal balance;

        private Account(int accountNumber, String name, String pin, BigDecimal balance) {
            this.accountNumber = accountNumber;
            this.name = name;
            this.pin = pin;
            this.balance = balance;
        }
    }
}
