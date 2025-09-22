package com.se310.ledger;

public class AccountCopy {
    
    public Account accountCopy(Account account) {
        return new Account(account.getAddress(), account.getBalance());
    }
}
