/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import java.util.Date;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

/**
 * A transaction is a complex class that brings together splits, accounts, and
 * payees
 *
 * @author ryan
 */
public interface Recurrence extends IDable {

	public static enum Frequency {

		NEVER, ONCE, DAILY, WEEKLY, BIWEEKLY, MONTHLY, END_OF_MONTH,
		BIMONTHLY, QUARTERLY, SEMIYEARLY, YEARLY
	}

	public void setFrequency( Frequency f );

	public Frequency getFrequency();

	public Property<Frequency> getFrequencyProperty();

	public void setNextRun( Date d );

	public Date getNextRun();

	public Property<Date> getNextRunProperty();
	
	public String getName();
	
	public void setName( String n );

	public StringProperty getNameProperty();

	public boolean isDue( Date d );
}
