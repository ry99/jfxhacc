/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ryan
 */
public interface TransactionMapper extends DataMapper<Transaction> {

	/**
	 * Checks if the given set of splits and their accounts satisfy the equations:
	 * credits = debits and Assets = Liabilities + Equity.
	 *
	 * @param splits a mapping of splits to their accounts
	 * @return true, if both equations are true
	 */
	public static boolean isBalanced( Map<Split, Account> splits ) {
		// make sure that debits = credits and
		// ASSETS = LIABILITIES + EQUITY (we'll say lefts = rights)
		int debits = 0;
		int credits = 0;
		int lefts = 0;
		int rights = 0;

		for ( Map.Entry<Split, Account> en : splits.entrySet() ) {
			Account acct = en.getValue();
			Split split = en.getKey();

			AccountType atype = acct.getAccountType();
			int value = split.getValue().value();

			if ( split.isCredit() ) {
				credits += value;
			}
			else {
				debits += value;
			}

			if ( atype.isRightPlus() ) {
				rights += value;
			}
			else {
				lefts += value;
			}
		}

		return ( debits == credits && lefts == rights );
	}

	public Transaction getTransaction( Split s ) throws MapperException;

	/**
	 * Gets all transactions that have a split belonging to the given account
	 *
	 * @param acct
	 * @return
	 * @throws MapperException
	 */
	public List<Transaction> getAll( Account acct, Journal jnl ) throws MapperException;

	public Transaction create( Date d, Payee p, String number, Map<Split, Account> splits,
			Journal journal )	throws MapperException;

	/**
	 * Creates a split with the given data <strong>but does not add it to the data
	 * store</strong>. This function is useful for creating splits to be used in a
	 * call to {@link #create(java.util.Date,
	 * com.ostrichemulators.jfxhacc.model.Payee, java.util.Map) }
	 *
	 * @param m
	 * @param memo
	 * @param rs
	 * @return
	 */
	public Split create( Money m, String memo, ReconcileState rs );
}
