/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitBase;
import com.ostrichemulators.jfxhacc.model.SplitBase.ReconcileState;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ryan
 */
public interface TransactionMapper extends DataMapper<Transaction> {

	public static enum SplitOp {
		ADDED, REMOVED, UPDATED
	};

	public Transaction getTransaction( Split s ) throws MapperException;

	/**
	 * Gets transactions that have a split belonging to the given account.
	 * Transactions that are not included in balance calculations are not included
	 * in this list
	 *
	 * @param acct
	 * @return
	 * @throws MapperException
	 */
	public List<Transaction> getAll( Account acct ) throws MapperException;

	/**
	 * Gets transactions that have a split belonging to the given account.
	 * Transactions that are not included in balance calculations are not included
	 * in this list
	 *
	 * @param acct
	 * @param from date of the earliest possible split. if null, get all splits
	 * @param to date of the first split to exclude. if null, there is no upper
	 * limit
	 * @return
	 * @throws MapperException
	 */
	public List<Transaction> getAll( Account acct, Date from, Date to ) throws MapperException;

	public List<SplitStub> getSplitStubs() throws MapperException;

	/**
	 * Gets splits belonging to the given account between from(inclusive) and
	 * to(exclusive). Splits that are not included in balance calculations are not
	 * included in this list.
	 *
	 * @param acct
	 * @param from date of the earliest possible split. if null, get all splits
	 * @param to date of the first split to exclude. if null, there is no upper
	 * limit
	 * @return
	 * @throws MapperException
	 */
	public Map<LocalDate, List<Split>> getSplits( Account acct, Date from, Date to ) throws MapperException;

	/**
	 * Gets all transactions for the given account who have a split that is
	 * {@link ReconcileState#CLEARED} or {@link ReconcileState#NOT_RECONCILED}
	 *
	 * @param acct
	 * @param asof the cut-off dates
	 * @return
	 * @throws MapperException
	 */
	public List<Transaction> getUnreconciled( Account acct, Date asof ) throws MapperException;

	public Transaction create( Date d, Payee p, String number, Collection<Split> splits,
			Journal journal ) throws MapperException;

	public Transaction create( Transaction t ) throws MapperException;

	/**
	 * Sets the persisted state of the given splits to the given reconcile state,
	 * and (for convenience) calls
	 * {@link Split#setReconciled(com.ostrichemulators.jfxhacc.model.Split.ReconcileState) }
	 *
	 * @param rs the new state
	 * @param splits the splits to change
	 * @throws MapperException
	 */
	public void reconcile( ReconcileState rs, SplitBase... splits ) throws MapperException;

	public void addSplitListener( SplitListener tl );

	public void removeSplitListener( SplitListener tl );

	public Transaction get( Recurrence r ) throws MapperException;
}
