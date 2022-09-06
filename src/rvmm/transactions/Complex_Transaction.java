package rvmm.transactions;

import java.util.ArrayList;
import jtps.jTPS_Transaction;

public class Complex_Transaction implements jTPS_Transaction {
    ArrayList<jTPS_Transaction> transactions = new ArrayList();
    
    public Complex_Transaction(jTPS_Transaction ... initTransactions) {
        for (jTPS_Transaction transaction : initTransactions) {
            transactions.add(transaction);
        }
    }

    @Override
    public void doTransaction() {
        for (jTPS_Transaction transaction : transactions) {
            transaction.doTransaction();
        }
    }

    @Override
    public void undoTransaction() {
        for (jTPS_Transaction transaction : transactions) {
            transaction.undoTransaction();
        }    
    }
}