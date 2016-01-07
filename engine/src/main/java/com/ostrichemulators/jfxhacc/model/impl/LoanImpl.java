/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Loan;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.vocabulary.Loans;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class LoanImpl extends IDableImpl implements Loan {

	private static final Logger log = Logger.getLogger( LoanImpl.class );
	private final ObjectProperty<Money> value = new SimpleObjectProperty<>();
	private final DoubleProperty apr = new SimpleDoubleProperty();
	private final IntegerProperty numpays = new SimpleIntegerProperty();

	public LoanImpl() {
		super( Loans.TYPE );
		value.set( new Money() );
		numpays.set( 0 );
		apr.set( 0 );
	}

	public LoanImpl( URI id ) {
		this( id, 0, new Money(), 0 );
	}

	public LoanImpl( URI id, double rate, Money val, int numpayments ) {
		super( Loans.TYPE, id );
		value.set( val.abs() );
		numpays.set( numpayments );
		apr.set( rate );
	}

	public LoanImpl( Loan t ) {
		this( t.getId(), t.getApr(), t.getValue(), t.getNumberOfPayments() );
	}

	@Override
	public void setApr( double rate ) {
		apr.set( rate );
	}

	@Override
	public double getApr() {
		return apr.get();
	}

	@Override
	public DoubleProperty getAprProperty() {
		return apr;
	}

	@Override
	public int getNumberOfPayments() {
		return numpays.get();
	}

	@Override
	public void setNumberOfPayments( int num ) {
		numpays.setValue( num );
	}

	@Override
	public IntegerProperty getNumberOfPaymentsProperty() {
		return numpays;
	}

	@Override
	public Money getValue() {
		return value.get();
	}

	@Override
	public void setValue( Money m ) {
		value.set( m.abs() );
	}

	@Override
	public Property<Money> getValueProperty() {
		return value;
	}

	@Override
	public String toString() {
		return value.get() + " apr: " + apr.get();
	}
}
