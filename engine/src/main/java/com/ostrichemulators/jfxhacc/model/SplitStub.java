/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import java.util.Date;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public interface SplitStub extends SplitBase {

	public void setAccountId( URI a );

	public URI getAccountId();

	public Property<URI> getAccountIdProperty();

	public void setTransactionId( URI a );

	public URI getTransactionId();

	public Property<URI> getTransactionIdProperty();

	public void setJournalId( URI a );

	public URI getJournalId();

	public Property<URI> getJournalIdProperty();

	public void setPayee( URI a );

	public URI getPayee();

	public Property<URI> getPayeeProperty();

	public void setDate( Date a );

	public Date getDate();

	public Property<Date> getDateProperty();

	public String getNumber();

	public void setNumber( String num );

	public StringProperty getNumberProperty();

	/**
	 * A property that changes when any internal data changes
	 *
	 * @return
	 */
	public StringBinding getAnyChangeProperty();
}
