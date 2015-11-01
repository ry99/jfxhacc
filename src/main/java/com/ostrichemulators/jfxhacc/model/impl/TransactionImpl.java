/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class TransactionImpl extends IDableImpl implements Transaction {

	private Date date;
	private Payee payee;
	private final Map<Split, Account> splits = new HashMap<>();

	public TransactionImpl() {
		super( JfxHacc.TRANSACTION_TYPE );
	}

	public TransactionImpl( URI id ) {
		super( JfxHacc.TRANSACTION_TYPE, id );
	}

	public TransactionImpl( URI id, Payee payee ) {
		this( id, payee, new Date() );
	}

	public TransactionImpl( URI id, Payee payee, Date date ) {
		super( JfxHacc.TRANSACTION_TYPE, id );
		this.payee = payee;
		this.date = date;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public void setDate( Date date ) {
		this.date = date;
	}

	@Override
	public Payee getPayee() {
		return payee;
	}

	@Override
	public void setPayee( Payee payee ) {
		this.payee = payee;
	}

	@Override
	public Map<Split, Account> getSplits() {
		return new HashMap<>( splits );
	}

	@Override
	public void setSplits( Map<Split, Account> splts ) {
		splits.clear();
		splits.putAll( splts );
	}

	@Override
	public void addSplit( Split s, Account a ) {
		splits.put( s, a );
	}
}
