/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Collection;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class TransactionHelper {

	public static final Logger log = Logger.getLogger( TransactionHelper.class );

	private TransactionHelper() {
	}

	/**
	 * Gets the "other" split when {@link Transaction#getSplits() t.getSplits} has
	 * exactly two splits, and one is from the given account
	 *
	 * @param t
	 * @param a
	 * @return the other split, or null
	 */
	public static Split getOther( Transaction t, Account a ) {
		Split other = null;
		boolean toomany = ( t.getSplits().size() != 2 );
		if ( !toomany ) {
			for ( Split s : t.getSplits() ) {
				if ( !a.equals( s.getAccount() ) ) {
					if ( null == other ) {
						other = s;
					}
					else {
						toomany = true;
					}
				}
			}
		}

		return ( toomany ? null : other );
	}

	/**
	 * Calculates the credits, debits, lefts, rights values. ASSETS = LIABILITIES
	 * + EQUITY (we say lefts = rights)
	 *
	 * @param splits
	 * @return an 4-index array (CREDITS, DEBITS, LEFTS, RIGHTS)
	 */
	private static int[] calcBalance( Collection<Split> splits ) {
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

		if ( !( 0 == balval || mainacct.getAccountType().isDebitPlus() ) ){
			balval = 0 - balval;
		}

		return new Money( balval );
	}

}
