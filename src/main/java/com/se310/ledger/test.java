package com.se310.ledger;

public class test {
    public static void main(String[] args) {
        Block genesisBlock = new Block(0, "0");
        Block secondBlock = new Block(1, genesisBlock.getHash());
        System.out.println("Latest Block Number: " + secondBlock.getBlockNumber());
    }
}
