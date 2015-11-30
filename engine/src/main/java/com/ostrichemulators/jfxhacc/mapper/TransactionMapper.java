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
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ryan
 */
public interface TransactionMapper extends DataMapper<Transaction> {

	/**
	 * Calculates the credits, debits, lefts, rights values. ASSETS = LIABILITIES
	 * + EQUITY (we say lefts = rights)
	 *
	 * @param splits
	 * @return an 4-index array (CREDITS, DEBITS, LEFTS, RIGHTS)
	 */
	static int[] calcBalance( Collection<Split> splits ) {
		// make sure that debits = credits and
		// ASSETS = LIABILITIES + EQUITY (we'll say lefts = rights)
		int debits = 0;
		int credits = 0;
		int lefts = 0;
		int rights = 0;

		for ( Split split : splits ) {
			Account acct = split.getAccount();
			AccountType atype = acct.getAccountType();
			int value = split.getValue().value();
			boolean credit = split.isCredit();

			if ( credit ) {
				credits += value;
			}
			else {
				debits += value;
			}

			if ( atype.isDebitPlus() ) {
				if ( credit ) {
					lefts -= value;
				}
				else {
					lefts += value;
				}
			}
			else {
				if ( credit ) {
					rights += value;
				}
				else {
					rights -= value;
				}
			}
		}

		return new int[]{ credits, debits, lefts, rights };
	}

	/**
	 * Checks if the given set of splits and their accounts satisfy the equations:
	 * credits = debits and Assets = Liabilities + Equity.
	 *
	 * @param splits the splits to check
	 * @return true, if both equations are true
	 */
	public static boolean isBalanced( Collection<Split> splits ) {
		int[] bals = calcBalance( splits );
		return ( bals[0] == bals[1] && bals[2] == bals[3] );
	}

	/**
	 * Calculates the amount of a split to the given account that would balance
	 * the given set of splits
	 *
	 * @param splits the splits that are out of balance
	 * @param mainacct the account to balance against
	 * @return
	 */
	public static Money balancingValue( Collection<Split> splits, Account mainacct ) {
		int[] bals = calcBalance( splits );
		int debits = bals[0];
		int credits = bals[1];
		int lefts = bals[2];
		int rights = bals[3];

		int balval = credits - debits;
//		if ( ( mainacct.getAccountType().isDebitPlus() && rights > lefts )
//				|| ( !mainacct.getAccountType().isDebitPlus() && lefts > rights ) ) {
//			balval = 0 - balval;
//		}

		return new Money( balval );
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

	public Transaction create( Date d, Payee p, String number, Collection<Split> splits,
			Journal journal ) throws MapperException;

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
