# banking-project

This was made for Java B2B Ironhack's bootcamp

## Requirements

1. The system must have 4 types of accounts: StudentChecking, Checking, Savings, and CreditCard

#### Checking

Checking Accounts should have

* a balance
* a secretKey
* a PrimaryOwner
* an optional SecondaryOwner
* a minimumBalance
* a penaltyFee
* a monthlyMaintenanceFee
* a status (FROZEN, ACTIVE)

##### StudentChecking

Student Checking Accounts are identical to Checking Accounts except that they do NOT have

* a monthlyMaintenanceFee
* a minimumBalance

##### Savings

Savings are identical to Checking accounts except that they

* do NOT have a monthlyMaintenanceFee
* do have an interestRate

##### CreditCard
CreditCard Accounts have

* a balance
* a PrimaryOwner
* an optional SecondaryOwner
* a creditLimit
* an interestRate
* a penaltyFee

2. The system must have 3 types of Users: Admins and AccountHolders.

##### AccountHolders
AccountHolders should be able to login, logout, and access their own account. AccountHolders have:

* a name
* a date of birth
* a primaryAddress (which should be a separate address class)
* an optional mailingAddress

##### Admins

Admins only have a name

##### ThirdParty

Third Party Accounts have a hashed key and a name.

3. Admins can create new accounts. When creating a new account they can create Checking, Savings, or CreditCard Accounts.

##### Savings
* savings accounts have a default interest rate of 0.0025
* savings accounts may be instantiated with an interest rate other than the default, with a maximum interest rate of 0.5
* savings accounts should have a default minimumBalance of 1000
* savings accounts may be instantiated with a mimimum balance of less than 1000 but no lower than 100

##### CreditCards
* creditCard accounts have a default creditLimit of 100
* creditCards may be instantiated with a creditLimit higher than 100 but not higher than 100000
* creditCards have a default interestRate of 0.2
* creditCards may be instantiated with an interestRate less than 0.2 but not lower than 0.1

##### CheckingAcounts
When creating a new Checking account, if the primaryOwner is less than 24, a StudentChecking account should be created otherwise a regular Checking Account should be created.
* checking accounts should have a minimumBalance of 250 and a monthlyMaintenanceFee of 12

4. Interest and Fees should be applied appropriately

##### PenaltyFee
* The penaltyFee for all accounts should be 40.
* If any account drops below the minimumBalance, the penaltyFee should be deducted from the balance automatically

##### Interest Rates
* Interest on savings accounts is added to the account annually at the rate of specified interestRate per year. That means that if I have 1000000 in a savings account with a 0.01 interest rate, 1% of 1 Million is added to my account after 1 year. When a savings Account balance is accessed, you must determine if it has been 1 year or more since the either the account was created or since interest was added to the account, and add the appropriate interest to the balance if necessary.
* Interest on credit cards is added to the balance monthly. If you have a 12% interest rate (0.12) then 1% interest will be added to the account monthly. When the balance of a credit card is accessed, check to determine if it has been 1 month or more since the account was created or since interested was added, and if so, add the appropriate interest to the balance.

5. Account Access

##### Admins
* Admins should be able to access the balance for any account, to debit the balance, and to credit the balance.

##### AccountHolders
* AccountHolders should be able to access their own account balance
* Account holders should be able to transfer money from any of their accounts to any other account (regardless of owner). The transfer should only be processed if the account has sufficient funds. The user must provide the Primary or Secondary owner name and the id of the account that should receive the transfer.

##### Third Party Users
* There must be a way for third party users to debit and credit accounts.
* Third party accounts must be added to the database by an admin.
* Third Party users can debit or credit accounts of any type. To do so the must provide their hashed key in the header of the HTTP request. They also must provide the amount, the Account id and the account secret key.

6. Fraud Detection

The application must recognize patterns that indicate fraud and Freeze the account status when potential fraud is detected.

Patterns that indicate fraud include:

* Transactions made in 24 hours that total to more than 150% of the customers highest daily total transactions in any other 24 hour period.
* More than 2 transactions occuring on a single account within a 1 second period.

7. Logging

All account access and transactions must be logged in a mongo database with user ids for auditing purposes.

8. Good Practices

* You must include thorough unit and integration tests.
* You must include robust error handling.
* You must use the Money class for all currency and BigDecimal for any other decimal or large number math.
* You must provide robust logs.

## Project

To check the functionalities better I've made a postman <a href="https://documenter.getpostman.com/view/10352687/T17AkWme?version=latest">documentation</a> to check out the endpoints and better understand how this project works!


### Entities 

You can take a look at the entity-relationship diagram shown below:

<img alt="entity-relationship-model" src="https://i.imgur.com/73TfPYE.jpg" />

### Service

Al transactions and account access are made by the ```AccountService```, protecting routes and methods as convinient and checking for ```Account Holder``` log status.

### Fraud

Every fraud detected will result on getting the bank account in question ```FROZEN```, as for now there are no ways to active accounts except for messing with the DB directly

### Take into account

All the logs are stores on a mongo database, separated on 2 different collections for auditing and app managing.

Before running the project remember to create a schema on MySQL and enter your server's password and schema created on the ```application.properties```

To get up and running the project will provide an initial Admin User registered with the following credentials:

````
username: admin
password: admin
````

### Things to improve

Given more time this project could improve on different aspects, some things I've come up with are:

* Make more integration tests
* Adding a better way to handle login
* Making logs more robust on exceptions 

If you've come all the way here, remember to let me know your thoughts!🤓



