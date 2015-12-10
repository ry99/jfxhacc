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

	public Transaction create( Transaction t ) throws MapperException;

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

	public Transaction get( Recurrence r ) throws MapperException;
}
