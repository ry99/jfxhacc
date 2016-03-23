/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Loan;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.vocabulary.Loans;
import java.util.concurrent.Callable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
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
	private final ObjectProperty<Account> principalA = new SimpleObjectProperty<>();
	private final ObjectProperty<Account> interestA = new SimpleObjectProperty<>();
	private final ObjectProperty<Account> sourceA = new SimpleObjectProperty<>();
	private final ObjectProperty<Journal> journal = new SimpleObjectProperty<>();

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

	public LoanImpl( URI id, double rate, Money val, int numpayments,
			Account principal, Account interest, Account payee ) {
		super( Loans.TYPE, id );
		value.set( val.abs() );
		numpays.set( numpayments );
		apr.set( rate );
		principalA.set( principal );
		interestA.set( interest );
		sourceA.set( payee );
	}

	public LoanImpl( Loan t ) {
		this( t.getId(), t.getApr(), t.getInitialValue(), t.getNumberOfPayments(),
				t.getPrincipalAccount(), t.getInterestAccount(), t.getSourceAccount() );
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
	public ReadOnlyProperty<Money> getPaymentAmount() {
		SimpleObjectProperty<Money> val = new SimpleObjectProperty<>();
		val.bind( Bindings.createObjectBinding( new Callable<Money>() {

			@Override
			public Money call() {
				double rate = apr.get() / 12;
				double numerator = rate * value.getValue().toDouble();
				double denominator = 1 - Math.pow( 1 + rate, numpays.negate().get() );

				return Money.valueOf( numerator / denominator );
			}
		}, apr, value, numpays ) );

		return val;
	}

	@Override
	public Money getInitialValue() {
		return value.get();
	}

	@Override
	public void setInitialValue( Money m ) {
		value.set( m.abs() );
	}

	@Override
	public Property<Money> getInitialValueProperty() {
		return value;
	}

	@Override
	public String toString() {
		return value.get() + " apr: " + apr.get();
	}

	@Override
	public Account getPrincipalAccount() {
		return principalA.get();
	}

	@Override
	public Property<Account> getPrincipalAccountProperty() {
		return principalA;
	}

	@Override
	public void setPrincipalAccount( Account a ) {
		principalA.set( a );
	}

	@Override
	public Property<Account> getInterestAccountProperty() {
		return interestA;
	}

	@Override
	public Account getInterestAccount() {
		return interestA.get();
	}

	@Override
	public void setInterestAccount( Account a ) {
		interestA.set( a );
	}

	@Override
	public Property<Account> getSourceAccountProperty() {
		return sourceA;
	}

	@Override
	public Account getSourceAccount() {
		return sourceA.get();
	}

	@Override
	public void setSourceAccount( Account a ) {
		sourceA.set( a );
	}

	@Override
	public void setJournal( Journal j ) {
		journal.set( j );
	}

	@Override
	public Journal getJournal() {
		return journal.get();
	}

	@Override
	public ObjectProperty<Journal> getJournalProperty() {
		return journal;
	}
}
