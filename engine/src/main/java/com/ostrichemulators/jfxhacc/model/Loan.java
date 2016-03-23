/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

/**
 * A transaction is a complex class that brings together splits, accounts, and
 * payees
 *
 * @author ryan
 */
public interface Loan extends IDable {

	public void setApr( double apr );

	public double getApr();

	public DoubleProperty getAprProperty();

	public int getNumberOfPayments();

	public void setNumberOfPayments( int numpays );

	public IntegerProperty getNumberOfPaymentsProperty();

	public Money getInitialValue();

	public ReadOnlyProperty<Money> getPaymentAmount();

	public void setInitialValue( Money m );

	public Property<Money> getInitialValueProperty();

	public Account getPrincipalAccount();

	public Property<Account> getPrincipalAccountProperty();

	public void setPrincipalAccount( Account a );

	public Property<Account> getInterestAccountProperty();

	public Account getInterestAccount();

	public void setInterestAccount( Account a );

	public Property<Account> getSourceAccountProperty();

	public Account getSourceAccount();

	public void setSourceAccount( Account a );

	public void setJournal( Journal j );

	public Journal getJournal();

	public ObjectProperty<Journal> getJournalProperty();
}
