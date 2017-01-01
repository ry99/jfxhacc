/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

/**
 *
 * @author ryan
 */
public interface Account extends IDable {

	public String getName();

	public void setName( String n );

	public AccountType getAccountType();

	public void setOpeningBalance( Money money );

	public Money getOpeningBalance();

	public void setNotes( String notes );

	public String getNotes();

	public StringProperty getNotesProperty();

	public void setNumber( String num );

	public String getNumber();

	public StringProperty getNumberProperty();

	public Property<Money> getOpeningBalanceProperty();

	public StringProperty getNameProperty();

	public boolean isType( AccountType t );
}
