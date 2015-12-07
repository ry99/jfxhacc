/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Recurring;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Date;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class RecurringImpl extends TransactionImpl implements Recurring {

	private static final Logger log = Logger.getLogger( RecurringImpl.class );
	private final ObjectProperty<Frequency> freq
			= new SimpleObjectProperty<>( Frequency.MONTHLY );
	private final ObjectProperty<Date> next = new SimpleObjectProperty<>();

	public RecurringImpl() {
	}

	public RecurringImpl( URI id ) {
		super( id );
	}

	public RecurringImpl( Recurring t ) {
		RecurringImpl r = new RecurringImpl( t.getId() );
		r.setDate( t.getDate() );
		r.setNumber( t.getNumber() );
		r.setPayee( t.getPayee() );
		r.setJournal( t.getJournal() );

		for ( Split s : t.getSplits() ) {
			r.addSplit( new SplitImpl( s ) );
		}

		freq.set( t.getFrequency() );
		next.set( t.getNextRun() );
	}

	/**
	 * Deep-copies the given transaction into a new recurring transaction (with
	 * the same id)
	 *
	 * @param t
	 */
	public static RecurringImpl fromTrans( Transaction t ) {
		RecurringImpl r = new RecurringImpl( t.getId() );
		r.setDate( t.getDate() );
		r.setNumber( t.getNumber() );
		r.setPayee( t.getPayee() );
		r.setJournal( t.getJournal() );

		for ( Split s : t.getSplits() ) {
			r.addSplit( new SplitImpl( s ) );
		}
		return r;
	}

	@Override
	public void setFrequency( Frequency f ) {
		freq.set( f );
	}

	@Override
	public Frequency getFrequency() {
		return freq.get();
	}

	@Override
	public Property<Frequency> getFrequencyProperty() {
		return freq;
	}

	@Override
	public void setNextRun( Date d ) {
		next.set( d );
	}

	@Override
	public Date getNextRun() {
		return next.get();
	}

	@Override
	public Property<Date> getNextRunProperty() {
		return next;
	}
}
