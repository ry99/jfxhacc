/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class AccountImpl extends NamedIDableImpl implements Account {

	private final StringProperty notes = new SimpleStringProperty();
	private final StringProperty number = new SimpleStringProperty();
	private final ObjectProperty<Money> openingbal = new SimpleObjectProperty<>();
	private final AccountType type;

	public AccountImpl( AccountType atype, URI id ) {
		super( Accounts.TYPE, id );
		type = atype;
		openingbal.set( new Money( 0 ) );
	}

	public AccountImpl( URI id, String name, AccountType atype, Money m ) {
		super( Accounts.TYPE, id, name );
		type = atype;
		openingbal.set( m );
	}

	public AccountImpl( Account acct ) {
		this( acct.getId(), acct.getName(), acct.getAccountType(), acct.getOpeningBalance() );
		notes.set( acct.getNotes() );
		number.set( acct.getNumber() );
	}

	@Override
	public AccountType getAccountType() {
		return type;
	}

	@Override
	public void setOpeningBalance( Money money ) {
		openingbal.set( money );
	}

	@Override
	public Money getOpeningBalance() {
		return openingbal.get();
	}

	@Override
	public Property<Money> getOpeningBalanceProperty() {
		return openingbal;
	}

	@Override
	public String toString() {
		return getName() + " (" + type + ( type.isDebitPlus() ? " debit+"
				: " credit+" ) + ")";
	}

	@Override
	public void setNotes( String note ) {
		notes.set( note );
	}

	@Override
	public String getNotes() {
		return notes.get();
	}

	@Override
	public StringProperty getNotesProperty() {
		return notes;
	}

	@Override
	public void setNumber( String num ) {
		number.set( num );
	}

	@Override
	public String getNumber() {
		return number.get();
	}

	@Override
	public StringProperty getNumberProperty() {
		return number;
	}

	@Override
	public boolean isType( AccountType t ) {
		return ( t == type );
	}
}
