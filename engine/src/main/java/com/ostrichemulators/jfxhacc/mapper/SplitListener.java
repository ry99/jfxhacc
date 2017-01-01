/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.mapper.TransactionMapper.SplitOp;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitBase;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author ryan
 */
public interface SplitListener {

	/**
	 * Called whenever a set of splits has had their reconcile state updated
	 *
	 * @param splits
	 */
	public void reconciled( Collection<? extends SplitBase> splits );

	/**
	 * Called whenever a transaction has updated its splits (that is,only during a
	 * call to
	 * {@link TransactionMapper#update(com.ostrichemulators.jfxhacc.model.IDable)}
	 *
	 * @param t the transaction
	 * @param updates a mapping of the transaction's splits, and what happened to
	 * them
	 */
	public void updated( Transaction t, Map<Split, SplitOp> updates );
}
