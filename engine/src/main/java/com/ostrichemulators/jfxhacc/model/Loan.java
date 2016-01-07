/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;

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

	public Money getValue();

	public void setValue( Money m );

	public Property<Money> getValueProperty();
}
