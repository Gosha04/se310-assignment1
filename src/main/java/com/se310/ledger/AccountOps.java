package com.se310.ledger;

public class AccountOps {
    
    public Account accountCopy(Account account) {
        return new Account(account.getAddress(), account.getBalance());
    }

    
}
