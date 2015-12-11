/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class TransactionImpl extends IDableImpl implements Transaction {

	private final Property<Date> date = new SimpleObjectProperty<>();
	private final Property<Payee> payee = new SimpleObjectProperty<>();
	private final StringProperty number = new SimpleStringProperty();
	private final SetProperty<Split> splits
			= new SimpleSetProperty<>( FXCollections.observableSet() );
	private final Property<Journal> jnl = new SimpleObjectProperty<>();

	public TransactionImpl() {
		super( Transactions.TYPE );
	}

	public TransactionImpl( URI id ) {
		super( Transactions.TYPE, id );
	}

	public TransactionImpl( URI id, Payee payee ) {
		this( id, new Date(), null, payee );
	}

	public TransactionImpl( URI id, Date date, String num, Payee payee ) {
		super( Transactions.TYPE, id );
		this.payee.setValue( payee );
		this.date.setValue( date );
		this.number.set( num );
	}

	public TransactionImpl( Date date, String num, Payee payee ) {
		this();
		this.payee.setValue( payee );
		this.date.setValue( date );
		this.number.set( num );
	}

	@Override
	public Date getDate() {
		return date.getValue();
	}

	@Override
	public void setDate( Date date ) {
		this.date.setValue( date );
	}

	@Override
	public Payee getPayee() {
		return payee.getValue();
	}

	@Override
	public void setPayee( Payee payee ) {
		this.payee.setValue( payee );
	}

	@Override
	public Set<Split> getSplits() {
		return Collections.unmodifiableSet( splits.getValue() );
	}

	@Override
	public void setSplits( Set<Split> splts ) {
		splits.setValue( FXCollections.observableSet( splts ) );
	}

	@Override
	public void addSplit( Split s ) {
		splits.add( s );
	}

	@Override
	public Split getSplit( Account a ) {
		for ( Split s : splits ) {
			if ( s.getAccount().equals( a ) ) {
				return s;
			}
		}

		return null;
	}

	@Override
	public String getNumber() {
		return number.get();
	}

	@Override
	public void setNumber( String number ) {
		this.number.set( number );
	}

	@Override
	public int compareTo( Transaction o ) {
		return getDate().compareTo( o.getDate() );
	}

	@Override
	public Property<Date> getDateProperty() {
		return date;
	}

	@Override
	public Property<Payee> getPayeeProperty() {
		return payee;
	}

	@Override
	public SetProperty<Split> getSplitsProperty() {
		return splits;
	}

	@Override
	public StringProperty getNumberProperty() {
		return number;
	}

	@Override
	public void setJournal( Journal j ) {
		jnl.setValue( j );
	}

	@Override
	public Journal getJournal() {
		return jnl.getValue();
	}

	@Override
	public Property<Journal> getJournalProperty() {
		return jnl;
	}
}
