package com.se310.ledger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommandProcessor class implementation designed to process individual Blockchain commands
 *
 * @author  Sergey L. Sundukovskiy -> Joshua Vaysman
 * @version 1.2 
 */
public class CommandProcessor {

    private static Ledger ledger = null;
    private static FinancialOps finOps = new FinancialOps();

    public static void processCommand(String command) throws CommandProcessorException {

        List<String> tokens = new ArrayList<>();
        //Split the line into tokens between spaces and quotes
        Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
        while (matcher.find())
            tokens.add(matcher.group(1).replace("\"", ""));

        switch (tokens.get(0)) {
            case "create-ledger" -> {
                if(tokens.size() != 6)
                    throw new CommandProcessorException("create-ledger", "Missing Arguments");

                System.out.println("Creating Ledger: " + tokens.get(1) + " " + tokens.get(3) + " " + tokens.get(5));
                ledger = Ledger.getInstance(tokens.get(1), tokens.get(3), tokens.get(5));
            }
            case "create-account" -> {
                if(tokens.size() != 2)
                    throw new CommandProcessorException("create-account", "Missing Arguments");

                System.out.println("Creating Account: " + tokens.get(1));
                try {
                    // Refactored for SRP
                    Account newAcc = new Account(tokens.get(1), 0);
                    ledger.addToLedger(newAcc);
                    // ledger.createAccount(tokens.get(1));
                } catch (LedgerException e) {
                    System.out.println("Failed due to: " + e.getReason());
                }
            }
            case "get-account-balance" -> {
                if(tokens.size() != 2)
                    throw new CommandProcessorException("create-account", "Missing Arguments");

                System.out.println("Getting Balance for: " + tokens.get(1));
                try {
                    System.out.println("Account Balance for: " + tokens.get(1) + " is "
                    // Refactored to make use of Account method getBalance()
                            +  ledger.getLatestBlock().getAccount(tokens.get(1)).getBalance());
                            // + ledger.getAccountBalance(tokens.get(1)));

                } catch (LedgerException e) {
                    System.out.println("Failed due to: " + e.getReason());
                }
            }
            case "get-account-balances" -> {
                System.out.println("Getting All Balances");
                try {
                    Map<String,Integer> map = finOps.getAccountBalances(ledger);

                    if(map == null){
                        System.out.println("No Account Has Been Committed");
                        break;
                    }

                    Set<String> keys = new HashSet<>(map.keySet());

                    for (String key : keys) {
                        System.out.println("Account Balance for: " + key + " is " + map.get(key));
                    }
                } catch (LedgerException e) {
                    System.out.println("Failed due to: " + e.getReason());
                }
            }
            case "process-transaction" -> {

                if(tokens.size() != 12)
                    throw new CommandProcessorException("process-transaction", "Missing Arguments");

                System.out.println("Processing Transaction: " + tokens.get(1) + " "
                        + tokens.get(3) + " " + tokens.get(5) + " " + tokens.get(7) + " "
                        + tokens.get(9) + " " + tokens.get(11) + " ");

                Block block = ledger.getUncommittedBlock();

                Account payer = block.getAccount (tokens.get(9));
                Account receiver = block.getAccount(tokens.get(11));

                if(payer == null || receiver == null){
                    throw new CommandProcessorException("process-transaction", "Account Does Not Exist") ;
                }

                Transaction tempTransaction = new Transaction(tokens.get(1), Integer.parseInt(tokens.get(3)),
                        Integer.parseInt(tokens.get(5)), tokens.get(7), payer, receiver);
                try {
                    finOps.processTransaction(ledger, tempTransaction);
                } catch (LedgerException e) {
                    System.out.println("Failed due to: " + e.getReason());
                }
            }
            case "get-block" -> {

                if(tokens.size() != 2)
                    throw new CommandProcessorException("get-block", "Missing Arguments");

                System.out.println("Get Block: " + tokens.get(1));
                Block block = null;
                try {
                    block = ledger.getBlock(Integer.parseInt(tokens.get(1)));
                } catch (LedgerException e) {
                    System.out.println("Failed due to: " + e.getReason());
                    break;
                }

                System.out.println("Block Number: " + block.getBlockNumber() + " "
                        + "Hash: " + block.getHash() + " " + "Previous Hash: " + block.getPreviousHash()
                );

                for(Transaction transaction: block.getTransactionList()){
                    System.out.println(transaction.toString());
                }

            }
            case "get-transaction" -> {
                if(tokens.size() != 2)
                    throw new CommandProcessorException("get-transaction", "Missing Arguments");

                System.out.println("Get Transaction: " + tokens.get(1));
                try {
                    Transaction transaction = finOps.getTransaction(ledger, (tokens.get(1)));

                    System.out.println("Transaction ID: " + transaction.getTransactionId() + " "
                            + "Amount: " + transaction.getAmount() + " " + "Fee: "
                            + transaction.getFee() + " " + "Note: " + transaction.getNote() + " " + "Payer: "
                            + transaction.getPayer().getAddress() + " " + "Receiver: "
                            + transaction.getReceiver().getAddress()
                    );
                } catch (LedgerException e) {
                    System.out.println("Failed due to: " + e.getReason());
                }
            }
            case "validate" -> {
                System.out.print("Validate: ");
                try {
                    ledger.validate();
                    System.out.println("Valid");
                } catch (LedgerException e) {
                    System.out.println("Failed due to: " + e.getReason());
                }

            }
            default ->  {
                throw new CommandProcessorException(tokens.get(0), "Invalid Command");

            }

        }

    }
}
