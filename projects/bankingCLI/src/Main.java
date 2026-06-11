import java.security.SecureRandom;
import java.util.Objects;
import java.util.Scanner;
import java.util.Arrays;
import java.util.stream.*;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final User[] EMPLOYEES = {
            new User("adyut", ROLE.MANAGER),
            new User("bhaskar", ROLE.CASHIER),
            new User("maitreya", ROLE.CASHIER),
            new User("medha", ROLE.PO)
    };

    public static void main(String[] args) throws InterruptedException {
        System.out.println("loading...");

        Arrays.stream(EMPLOYEES).forEach(emp -> emp.isPresent = true);
        Banking system = new Banking();

        Thread.sleep(2000);
        System.out.println("Welcome to thesime banking system");
        while (true) {
            System.out.println("What you want to do:");
            System.out.println("0. register customer");
            System.out.println("1. withdraw cash");
            System.out.println("2. submit cash");
            System.out.println("3. account enquiry");
            System.out.println("4. complain");
            System.out.println("5. transfer money");
            System.out.println("6. exit");

            System.out.print("enter your choice: ");

            try {
                if (!sc.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a number.");
                    sc.next();
                    continue;
                }
                int choice = sc.nextInt();

                switch (choice) {
                    case 0:
                        try {
                            system.registerCustomer();
                        } catch (Exception e) {
                            System.out.println("Error registering customer: " + e.getMessage());
                        }
                        System.out.println();
                        break;
                    case 1:
                        try {
                            system.withdrawMoney();
                            System.out.println("withdraw money successfully");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Account error: " + e.getMessage());
                        } catch (RuntimeException e) {
                            System.out.println("Withdraw failed: " + e.getMessage());
                        }
                        break;
                    case 2:
                        try {
                            system.submitMoney();
                            System.out.println("submitted money successfully");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Account error: " + e.getMessage());
                        } catch (RuntimeException e) {
                            System.out.println("Submit failed: " + e.getMessage());
                        }
                        break;
                    case 3:
                        try {
                            system.accountEnquiry();
                        } catch (IllegalArgumentException e) {
                            System.out.println("Account error: " + e.getMessage());
                        } catch (RuntimeException e) {
                            System.out.println("Enquiry failed: " + e.getMessage());
                        }
                        System.out.println();
                        break;
                    case 4:
                        try {
                            system.complain();
                        } catch (RuntimeException e) {
                            System.out.println("Complaint error: " + e.getMessage());
                        }
                        break;
                    case 5:
                        try {
                            system.transferMoney();
                        } catch (IllegalArgumentException e) {
                            System.out.println("Account error: " + e.getMessage());
                        } catch (RuntimeException e) {
                            System.out.println("Transfer failed: " + e.getMessage());
                        }
                        break;
                    case 6:
                        try {
                            system.store();
                        } catch (Exception e) {
                            System.out.println("Error during exit: " + e.getMessage());
                        }
                        System.out.println("Thank you for banking with us. Goodbye!");
                        sc.close();
                        return;
                    default:
                        System.out.println("thanks for your time");
                }
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                sc.nextLine();
            }
        }
    }
}

interface Global {
    Scanner in = new Scanner(System.in);
    int MAXACC_FORMS = 50;
    int MAXCOM_FORMS = 50;
    double MAX_AMOUNT = 10_0000.00;
    Account[] ACCOUNTS = new Account[50];

    static String getFormInput(String message) {
        System.out.print(message);
        if (in.hasNextLine()) {
            return in.next();
        }
        throw new RuntimeException("No input available");
    }

    static String generateAccountNumber() {
        String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom RANDOM = new SecureRandom();
        return IntStream.range(0, 16)
                .map(i -> RANDOM.nextInt(CHARSET.length()))
                .mapToObj(CHARSET::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}

class User {
    public String name;
    public ROLE role;
    public boolean isPresent;

    public User(String name, ROLE role) {
        this.name = name;
        this.role = role;
    }

    @Override
    public String toString() {
        return "{name:"
                + this.name +
                ", role:"
                + this.role +
                ", isPresent:"
                + this.isPresent +
                "}";
    }
}

class Account {
    public String accountNumber;
    public String name;
    public double balance;
    public ROLE role;

    public Account(String name, String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.balance = balance;
        this.role = ROLE.CUSTOMER;
    }

    public int credit(double amount) {
        if (amount <= 0) {
            return -2;
        }
        if (this.balance - amount >= 0) {
            this.balance -= amount;
            return 0;
        }
        return -1;
    }

    public int debit(double amount) {
        if (amount <= 0) {
            return -2;
        }
        if (this.balance + amount <= Global.MAX_AMOUNT) {
            this.balance += amount;
            return 0;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", role=" + role +
                '}';
    }
}

enum ROLE {
    MANAGER, PO, CASHIER, CUSTOMER
}

class Banking {
    private int accFormCounter = 50;
    private int comFormCounter = 50;
    private int accountCounter = 50;
    private AccountForm[] accForms = new AccountForm[Global.MAXACC_FORMS];
    private ComplainForm[] comForms = new ComplainForm[Global.MAXCOM_FORMS];

    public void withdrawMoney() {
        if (accFormCounter == 0) {
            throw new RuntimeException("no more forms available, see you tommorow");
        }
        String accountNumber = Global.getFormInput("Enter the Account Number: ");
        accountNumber = accountNumber.strip();
        String accountHolderName = Global.getFormInput("Enter the Account AccountHolder Name: ");
        accountHolderName = accountHolderName.strip();

        System.out.print("Enter the amount to withdraw: ");
        if (!Global.in.hasNextDouble()) {
            Global.in.next();
            throw new RuntimeException("Invalid amount entered");
        }
        double amount = Global.in.nextDouble();

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        AccountForm form = new AccountForm(accountNumber, accountHolderName, amount);
        Account foundAccount = Arrays.stream(Global.ACCOUNTS)
                .filter(Objects::nonNull)
                .filter(
                        account ->
                                account.accountNumber.equals(form.accountNumber)
                                        && account.name.equals(form.accountHolderName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("account not found"));

        int returnCode = foundAccount.credit(amount);
        if (returnCode == -2) {
            throw new RuntimeException("Invalid amount");
        }
        if (returnCode != 0) {
            throw new RuntimeException("Insufficient balance. Current balance: " + foundAccount.balance);
        }
        accForms[--accFormCounter] = form;
    }

    public void submitMoney() {
        if (accFormCounter == 0) {
            throw new RuntimeException("no more forms available, see you tommorow");
        }
        System.out.println();
        String accountNumber = Global.getFormInput("Enter the Account Number:");
        accountNumber = accountNumber.strip();
        String accountHolderName = Global.getFormInput("Enter the Account AccountHolder Name: ");
        accountHolderName = accountHolderName.strip();

        System.out.print("Enter the Amount: ");
        if (!Global.in.hasNextDouble()) {
            Global.in.next();
            throw new RuntimeException("Invalid amount entered");
        }
        double amount = Global.in.nextDouble();

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        AccountForm form = new AccountForm(accountNumber, accountHolderName, amount);

        Account foundAccount = Arrays.stream(Global.ACCOUNTS)
                .filter(Objects::nonNull)
                .filter(
                        account ->
                                account.accountNumber.equals(form.accountNumber)
                                        && account.name.equals(form.accountHolderName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("account not found"));

        int returnCode = foundAccount.debit(amount);
        if (returnCode == -2) {
            throw new RuntimeException("Invalid amount");
        }
        if (returnCode != 0) {
            throw new RuntimeException("Deposit failed. Amount would exceed max balance limit of " + Global.MAX_AMOUNT);
        }
        accForms[--accFormCounter] = form;
    }

    public void transferMoney() {
        if (accFormCounter == 0) {
            throw new RuntimeException("no more forms available, see you tommorow");
        }

        System.out.println();
        String fromAccountNumber = Global.getFormInput("Enter your Account Number: ");
        fromAccountNumber = fromAccountNumber.strip();
        String fromAccountHolderName = Global.getFormInput("Enter your Account Holder Name: ");
        fromAccountHolderName = fromAccountHolderName.strip();

        String toAccountNumber = Global.getFormInput("Enter recipient Account Number: ");
        toAccountNumber = toAccountNumber.strip();
        String toAccountHolderName = Global.getFormInput("Enter recipient Account Holder Name: ");
        toAccountHolderName = toAccountHolderName.strip();

        System.out.print("Enter the Amount to Transfer: ");
        if (!Global.in.hasNextDouble()) {
            Global.in.next();
            throw new RuntimeException("Invalid amount entered");
        }
        double amount = Global.in.nextDouble();

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        String finalFromAccNo = fromAccountNumber;
        String finalFromName = fromAccountHolderName;
        Account fromAccount = Arrays.stream(Global.ACCOUNTS)
                .filter(Objects::nonNull)
                .filter(account -> account.accountNumber.equals(finalFromAccNo)
                        && account.name.equals(finalFromName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

        String finalToAccNo = toAccountNumber;
        String finalToName = toAccountHolderName;
        Account toAccount = Arrays.stream(Global.ACCOUNTS)
                .filter(Objects::nonNull)
                .filter(account -> account.accountNumber.equals(finalToAccNo)
                        && account.name.equals(finalToName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Recipient account not found"));

        int debitCode = fromAccount.credit(amount);
        if (debitCode == -2) {
            throw new RuntimeException("Invalid amount");
        }
        if (debitCode != 0) {
            throw new RuntimeException("Insufficient balance in sender account. Current balance: " + fromAccount.balance);
        }

        int creditCode = toAccount.debit(amount);
        if (creditCode != 0) {
            // Rollback the debit from sender
            fromAccount.debit(amount);
            throw new RuntimeException("Transfer failed. Recipient account would exceed max balance limit.");
        }

        accForms[--accFormCounter] = new AccountForm(fromAccountNumber, fromAccountHolderName, amount);
        System.out.println("Transfer successful! Rs." + amount + " transferred to " + toAccount.name);
    }

    public void complain() {
        if (comFormCounter == 0) {
            throw new RuntimeException("no more complaint forms available, see you tomorrow");
        }

        System.out.println();
        String name = Global.getFormInput("Enter your name: ");
        name = name.strip();
        String description = Global.getFormInput("Enter your complaint description: ");
        description = description.strip();

        if (name.isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }
        if (description.isEmpty()) {
            throw new RuntimeException("Complaint description cannot be empty");
        }

        ComplainForm form = new ComplainForm();
        form.name = name;
        form.description = description;
        comForms[--comFormCounter] = form;

        System.out.println("Complaint registered successfully. We will get back to you soon.");
    }

    public void accountEnquiry() {
        if (accFormCounter == 0) {
            throw new RuntimeException("no more forms available, see you tommorow");
        }
        System.out.println();
        String accountNumber = Global.getFormInput("Enter the Account Number:");
        accountNumber = accountNumber.strip();
        String accountHolderName = Global.getFormInput("Enter the Account AccountHolder Name: ");
        accountHolderName = accountHolderName.strip();

        String finalAccountNumber = accountNumber;
        String finalAccountHolderName = accountHolderName;
        Account foundAccount = Arrays.stream(Global.ACCOUNTS)
                .filter(Objects::nonNull)
                .filter(
                        account ->
                                account.accountNumber.equals(finalAccountNumber)
                                        && account.name.equals(finalAccountHolderName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("account not found"));
        System.out.println("your account balance is: " + foundAccount.balance);
    }

    public void registerCustomer() {
        if (accountCounter == 0) {
            throw new RuntimeException("no more account slots available, see you tomorrow");
        }
        System.out.println();
        String name = Global.getFormInput("Enter the customer name: ");
        name = name.strip();

        if (name.isEmpty()) {
            throw new RuntimeException("Customer name cannot be empty");
        }

        String accountNumber = Global.generateAccountNumber();
        Account customer = new Account(name, accountNumber, 0.0);
        System.out.println("registered customer successfully");
        System.out.println("Account Holder Name: " + customer.name);
        System.out.println("Account Number: " + customer.accountNumber);
        System.out.println("Account Balance: " + customer.balance);
        Global.ACCOUNTS[--accountCounter] = customer;
    }

    public void store() {
        System.out.println("\n--- End of Day Summary ---");
        System.out.println("Total accounts registered: " + (50 - accountCounter));
        System.out.println("Total transactions processed: " + (50 - accFormCounter));
        System.out.println("Total complaints filed: " + (50 - comFormCounter));
        System.out.println("Data stored successfully.");
    }
}

class AccountForm {
    public String accountNumber;
    public String accountHolderName;
    public double amount;

    public AccountForm(
            String accountNumber,
            String accountHolderName,
            double amount
    ) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.amount = amount;
    }
}

class ComplainForm {
    public String name;
    public String description;
}