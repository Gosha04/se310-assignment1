package com.se310.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Ledger Class representing simple implementation of Blockchain
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 */
public class Ledger {
    private String name;
    private String description;
    private String seed;
    private static NavigableMap <Integer,Block> blockMap;
    private static Block uncommittedBlock;

    private static Ledger ledger;

    // Initialize genesis block and the account list
    static {
        blockMap = new TreeMap<>();
        uncommittedBlock = new Block(1, "");
        uncommittedBlock.addAccount("master", new Account("master", Integer.MAX_VALUE));
    }

    /**
     * Create singleton of the Ledger
     * @param name
     * @param description
     * @param seed
     * @return
     */
    public static synchronized Ledger getInstance(String name, String description, String seed) {
        if (ledger == null) {
            ledger = new Ledger(name, description, seed);
        }
        return ledger;
    }

    /**
     * Private Ledger Constructor
     * @param name
     * @param description
     * @param seed
     */
    private Ledger(String name, String description, String seed) {
        this.name = name;
        this.description = description;
        this.seed = seed;
    }

    /**
     * Getter method for the name of the Ledger
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Setter Method for the name of the Ledger
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter Method for Ledger description
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter Method for Description
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter Method for the seed
     * @return String
     */
    public String getSeed() {
        return seed;
    }

    /**
     * Setter Method for the seed
     * @param seed
     */
    public void setSeed(String seed) {
        this.seed = seed;
    }

    // Directly handled by Account Constructor
    // /**
    //  * Method for creating accounts in the blockchain
    //  * @param address
    //  * @return Account representing account in the Blockchain
    //  */
    // public Account createAccount(String address) throws LedgerException {

    //     if(uncommittedBlock.getAccount(address) != null){
    //         throw new LedgerException("Create Account", "Account Already Exists");
    //     }

    //     Account account = new Account(address, 0);
    //     uncommittedBlock.addAccount(address, account);
    //     return account;
    // }

    /**
     * Method for adding account to the Blockchain
     * @param account
     * @throws LedgerException
     */
    public void addToLedger(Account account) throws LedgerException {

        if(uncommittedBlock.getAccount(account.getAddress()) != null){
            throw new LedgerException("Add To Ledger", "Account Already Exists");
        }

        uncommittedBlock.addAccount(account.getAddress(), account);
    }

    // Already implement by Account.getBalance()
    // /**
    //  * Get Account balance by address
    //  * @param address
    //  * @return Integer representing balance of the Account
    //  * @throws LedgerException
    //  */
    // public Integer getAccountBalance(String address) throws LedgerException {

    //     if(blockMap.isEmpty()){
    //         throw new LedgerException("Get Account Balance", "Account Is Not Committed to a Block");
    //     }

    //     Block block = blockMap.lastEntry().getValue();
    //     Account account = block.getAccount(address);

    //     if (account == null)
    //         throw new LedgerException("Get Account Balance", "Account Does Not Exist");
    //     else
    //         return account.getBalance();
    // }

    /**
     * Get Block by id
     * @param blockNumber
     * @return Block or Null
     */
    public Block getBlock (Integer blockNumber) throws LedgerException {
        Block block = blockMap.get(blockNumber);
        if(block == null){
            throw new LedgerException("Get Block", "Block Does Not Exist");
        }
        return block;
    }

    public NavigableMap<Integer, Block> getBlockMap() throws LedgerException{
        if (blockMap.isEmpty()){
            throw new LedgerException("Get Block Map", "No Block Has Been Committed");
        }
        return blockMap;
    }

    /**
     * Get latest Block in the Blockchain
     * - Added so we can avoid using Ledger.getAccountBalance() 
     * @return
     * @throws LedgerException
     */
    public Block getLatestBlock() throws LedgerException {
        if(blockMap.isEmpty()){
            throw new LedgerException("Get Latest Block", "No Block Has Been Committed");
        }
        return blockMap.lastEntry().getValue();
    }

    /**
     * Get Transaction by id
     * @param transactionId
     * @return Transaction or Null
     */
    public Transaction getTransaction (String transactionId){

        for (Entry mapElement : blockMap.entrySet()) {

            // Finding specific transactions in the committed blocks
            Block tempBlock = (Block) mapElement.getValue();
            for (Transaction transaction : tempBlock.getTransactionList()){
                if(transaction.getTransactionId().equals(transactionId)){
                    return transaction;
                }
            }
        }
        // Finding specific transactions in the uncommitted block
        for (Transaction transaction : uncommittedBlock.getTransactionList()){
            if(transaction.getTransactionId().equals(transactionId)){
                return transaction;
            }
        }
        return null;
    }

    /**
     * Get number of Blocks in the Blockchain
     * @return int representing number of blocks committed to Blockchain
     */
    public int getNumberOfBlocks(){
        return blockMap.size();
    }

    /**
     * Method for validating Blockchain.
     * Check each block for Hash consistency
     * Check each block for Transaction count
     * Check account balances against the total
     */
    public void validate() throws LedgerException {

        if(blockMap.isEmpty()){
            throw new LedgerException("Validate", "No Block Has Been Committed");
        }

        Block committedBlock = blockMap.lastEntry().getValue();
        Map<String,Account> accountMap = committedBlock.getAccountBalanceMap();
        List<Account> accountList = new ArrayList<>(accountMap.values());

        int totalBalance = 0;
        for (Account account : accountList) {
            totalBalance += account.getBalance();
        }

        int fees = 0;
        // String hash;
        for(Integer key : blockMap.keySet()){
            Block block = blockMap.get(key);

            //Check for Hash Consistency
            if(block.getBlockNumber() != 1)
                if(!block.getPreviousHash().equals(block.getPreviousBlock().getHash())){
                    throw new LedgerException("Validate", "Hash Is Inconsistent: "
                            + block.getBlockNumber());
            }

            //Check for Transaction Count
            if(block.getTransactionList().size() != 10){
                throw new LedgerException("Validate", "Transaction Count Is Not 10 In Block: "
                        + block.getBlockNumber());
            }

            for(Transaction transaction : block.getTransactionList()){
                fees += transaction.getFee();
            }
        }

        int adjustedBalance = totalBalance + fees;

        //Check for account balances against the total
        if(adjustedBalance != Integer.MAX_VALUE){
            throw new LedgerException("Validate", "Balance Does Not Add Up");
        }

    }

    /**
     * Helper method for CommandProcessor
     * @return current block we are working with
     */
    public Block getUncommittedBlock(){
        return uncommittedBlock;
    }

    /**
     * Helper method allowing reset the state of the Ledger
     */
    public synchronized void reset(){
        blockMap = new TreeMap<>();
        uncommittedBlock = new Block(1, "");
        uncommittedBlock.addAccount("master", new Account("master", Integer.MAX_VALUE));
    }
}
