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
	public static boolean isBalanced( Map<Account, Split> splits ) {
		// make sure that debits = credits and
		// ASSETS = LIABILITIES + EQUITY (we'll say lefts = rights)
		int debits = 0;
		int credits = 0;
		int lefts = 0;
		int rights = 0;

		for ( Map.Entry<Account, Split> en : splits.entrySet() ) {
			Account acct = en.getKey();
			Split split = en.getValue();

			AccountType atype = acct.getAccountType();
			int value = split.getValue().value();

			if ( split.isCredit() ) {
				credits += value;
			}
			else {
				debits += value;
			}

			if ( atype.isDebitPlus() ) {
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

	/**
	 * Gets all transactions for the given account who have a split that is
	 * {@link ReconcileState#CLEARED} or {@link ReconcileState#NOT_RECONCILED}
	 *
	 * @param acct
	 * @param jnl
	 * @param asof the cut-off dates
	 * @return
	 * @throws MapperException
	 */
	public List<Transaction> getUnreconciled( Account acct, Journal jnl, Date asof ) throws MapperException;

	public Transaction create( Date d, Payee p, String number, Map<Account, Split> splits,
			Journal journal ) throws MapperException;

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

	/**
	 * Sets the persisted state of the given splits to the given reconcile state,
	 * and (for convenience) calls
	 * {@link Split#setReconciled(com.ostrichemulators.jfxhacc.model.Split.ReconcileState) }
	 *
	 * @param rs the splits to change
	 * @param splits the new state
	 * @param acct the account containing all the splits
	 * @throws MapperException
	 */
	public void reconcile( ReconcileState rs, Account acct, Split... splits ) throws MapperException;

	public void addMapperListener( TransactionListener tl );

	public void removeMapperListener( TransactionListener tl );
}
