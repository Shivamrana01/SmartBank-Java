# SmartBank — Java Banking Management System

A beginner-friendly Java console project demonstrating OOP, collections, validation, money calculations with `BigDecimal`, and file handling.

## Features
- Create accounts with unique account numbers
- Login using account number and a 4-digit PIN
- Deposit and withdraw money
- Transfer money between accounts
- Check balance and view account details
- View transaction history
- Change PIN
- Close an account when its balance is zero
- Persist account and transaction records between runs
- Validate menu choices, amounts, and PIN format

## Requirements
- JDK 8 or newer
- Terminal / command prompt

## Run
From the folder containing `SmartBank.java`:

```bash
javac SmartBank.java
java SmartBank
```

The app creates a `smartbank-data` folder to store local account and transaction files.

## Important safety note
This is an educational portfolio project, not a production banking system. PINs are stored as plain text and local files are not protected. Use only fictional test data. Do not use real personal, financial, or banking information. Do not publish the generated `smartbank-data` folder to GitHub.
