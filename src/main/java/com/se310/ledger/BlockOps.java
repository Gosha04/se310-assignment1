package com.se310.ledger;

public class BlockOps {

    public void addAccount(Block block, String address, Account account)  {
        block.getAccountBalanceMap().put(address, account);
    }   

    public Account getAccount(Block block, String address) {
        return block.getAccountBalanceMap().get(address);
    }
}