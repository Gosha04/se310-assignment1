package com.se310.ledger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

/**
 * CommandProcessorException class implementation designed display errors to the user while
 * processing commands
 *
 * @author  Joshua Vaysman
 * @version 1.0
 */

public class FinancialOps{
     /**
     * Method implementing core functionality of the Blockchain by handling given transaction
     * @param transaction
     * @return String representing transaction id
     * @throws LedgerException
     */
    public synchronized String processTransaction(Ledger ledger, Transaction transaction) throws LedgerException {
        // BLOCK ISSUE HERE
        //Block uncommittedBlock = ledger.getUncommittedBlock();
        String seed = ledger.getSeed();

        //Check for transaction specification conditions
        if(transaction.getAmount() < 0 || transaction.getAmount() > Integer.MAX_VALUE ){
            throw new LedgerException("Process Transaction", "Transaction Amount Is Out of Range");
        } else if (transaction.getFee() < 10) {
            throw new LedgerException("Process Transaction", "Transaction Fee Must Be Greater Than 10");
        } else if (transaction.getNote().length() > 1024){
            throw new LedgerException("Process Transaction", "Note Length Must Be Less Than 1024 Chars");
        }

        if(getTransaction(ledger, transaction.getTransactionId()) != null){
            throw new LedgerException("Process Transaction", "Transaction Id Must Be Unique");
        }

        Account tempPayerAccount = transaction.getPayer();
        Account tempReceiverAccount = transaction.getReceiver();

        updateAccounts(tempPayerAccount, tempReceiverAccount, transaction.getFee(), transaction.getAmount());
        // if(transaction.getPayer().getBalance() < (transaction.getAmount() + transaction.getFee()))
        //     throw new LedgerException("Process Transaction", "Payer Does Not Have Required Funds");

        // //Deduct balance of the payer
        // tempPayerAccount.setBalance(tempPayerAccount.getBalance()
        //         - transaction.getAmount() - transaction.getFee());
        // //Increase balance of the receiver
        // tempReceiverAccount.setBalance(tempReceiverAccount.getBalance() + transaction.getAmount());

        ledger.getUncommittedBlock().getTransactionList().add(transaction);

        //Check to see if account blocked has reached max size
        if (ledger.getUncommittedBlock().getTransactionList().size() == 10){

            List<String> tempTxList = new ArrayList<>();
            tempTxList.add(seed);

            //Loop through the list of transaction to get the hash
            for( Transaction tempTx : ledger.getUncommittedBlock().getTransactionList()){
                tempTxList.add(tempTx.toString());
            }

            MerkleTrees merkleTrees = new MerkleTrees(tempTxList);
            merkleTrees.merkle_tree();
            ledger.getUncommittedBlock().setHash(merkleTrees.getRoot());

            //Commit uncommitted block
            ledger.getBlockMap().put(ledger.getUncommittedBlock().getBlockNumber(), ledger.getUncommittedBlock());

            //Get committed block
            Block committedBlock = ledger.getBlockMap().lastEntry().getValue();
            Map<String,Account> accountMap = committedBlock.getAccountBalanceMap();

            //Get all the accounts
            List<Account> accountList = new ArrayList<Account>(accountMap.values());

            //Create next block
            Block uncommittedBlock = new Block(ledger.getUncommittedBlock().getBlockNumber() + 1,
                    committedBlock.getHash());

            //Replicate accounts
            for (Account account : accountList) {
                Account tempAccount = (Account) account.clone();
                uncommittedBlock.addAccount(tempAccount.getAddress(), tempAccount);
            }

            //Link to previous block
            uncommittedBlock.setPreviousBlock(committedBlock);
        }

        return transaction.getTransactionId();
    }

     /**
     * Helper method for updating accounts when processing transaction
     * - Maintains SRP for process Transaction method
     * - The above should just handle transaction processing logic not the account updates
     * @param payer
     * @param receiver
     * @param fee
     * @param amount
     * @throws LedgerException
     */
    public synchronized void updateAccounts (Account payer, Account receiver, Integer fee, Integer amount) throws LedgerException {
         if(payer.getBalance() < (amount + fee))
            throw new LedgerException("Process Transaction", "Payer Does Not Have Required Funds");

        //Deduct balance of the payer
        payer.setBalance(payer.getBalance()
                - amount - fee);
        //Increase balance of the receiver
        receiver.setBalance(receiver.getBalance() + amount);
    }

    /**
     * Get all Account balances that are part of the Blockchain
     * @return Map representing Accounts and balances
     */
    public Map<String,Integer> getAccountBalances(Ledger ledger) throws LedgerException {
        if (ledger == null){
            throw new LedgerException("Get Account Balances", "Ledger is Null");
        }
        NavigableMap <Integer,Block> blockMap = ledger.getBlockMap();
        
        if(blockMap.isEmpty())
            return null;

        Block committedBlock = blockMap.lastEntry().getValue();
        Map<String,Account> accountMap = committedBlock.getAccountBalanceMap();

        Map<String, Integer> balances = new HashMap<>();
        List<Account> accountList = new ArrayList<>(accountMap.values());

        for (Account account : accountList) {
            balances.put(account.getAddress(), account.getBalance());
        }

        return balances;
    }

     /**
     * Get Transaction by id
     * @param transactionId
     * @return Transaction or Null
     */
    public Transaction getTransaction (Ledger ledger, String transactionId) throws LedgerException {
        if (ledger == null){
            throw new LedgerException("Get Transaction", "Ledger is Null");
        }

        for (Entry mapElement : ledger.getBlockMap().entrySet()) {

            // Finding specific transactions in the committed blocks
            Block tempBlock = (Block) mapElement.getValue();
            for (Transaction transaction : tempBlock.getTransactionList()){
                if(transaction.getTransactionId().equals(transactionId)){
                    return transaction;
                }
            }
        }
        // Finding specific transactions in the uncommitted block
        for (Transaction transaction : ledger.getUncommittedBlock().getTransactionList()){
            if(transaction.getTransactionId().equals(transactionId)){
                return transaction;
            }
        }
        return null;
    }
}
