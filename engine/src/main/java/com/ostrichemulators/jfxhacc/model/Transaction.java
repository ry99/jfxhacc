/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import java.util.Date;
import java.util.Set;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.property.StringProperty;

/**
 * A transaction is a complex class that brings together splits, accounts, and
 * payees
 *
 * @author ryan
 */
public interface Transaction extends IDable, Comparable<Transaction> {

	public Date getDate();

	public void setDate( Date date );

	public Property<Date> getDateProperty();

	public void setPayee( Payee payee );

	public Payee getPayee();

	public Property<Payee> getPayeeProperty();

	public Set<Split> getSplits();

	/**
	 * Gets the split for the given account, or null
	 * @param a
	 * @return
	 */
	public Split getSplit( Account a );

	public void setSplits( Set<Split> splits );

	public SetProperty<Split> getSplitsProperty();

	public void addSplit( Split s );

	public String getNumber();

	public void setNumber( String s );

	public StringProperty getNumberProperty();
}
