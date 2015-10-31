/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import java.util.Date;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class TransactionImpl extends IDableImpl implements Transaction {

	private Date date;
	private URI payee;

	public TransactionImpl() {
		super( JfxHacc.TRANSACTION_TYPE );
	}

	public TransactionImpl( URI id ) {
		super( JfxHacc.TRANSACTION_TYPE, id );
	}

	public TransactionImpl( URI id, URI payee ) {
		this( id, payee, new Date() );
	}

	public TransactionImpl( URI id, URI payee, Date date ) {
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
	public URI getPayee() {
		return payee;
	}

	@Override
	public void setPayee( URI payee ) {
		this.payee = payee;
	}

}
