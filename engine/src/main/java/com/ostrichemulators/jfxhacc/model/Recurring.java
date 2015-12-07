/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import java.util.Date;
import javafx.beans.property.Property;

/**
 * A transaction is a complex class that brings together splits, accounts, and
 * payees
 *
 * @author ryan
 */
public interface Recurring extends Transaction {

	public static enum Frequency {

		NEVER, DAILY, WEEKLY, BIWEEKLY, SEMIMONTLY, MONTHLY,
		BIMONTHLY, QUARTERLY, SEMIYEARLY, YEARLY
	}

	public void setFrequency( Frequency f );

	public Frequency getFrequency();

	public Property<Frequency> getFrequencyProperty();

	public void setNextRun( Date d );

	public Date getNextRun();

	public Property<Date> getNextRunProperty();

}
