/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class SplitImpl extends AbstractSplitBase implements Split {

	private final Property<Account> acct = new SimpleObjectProperty<>();

	public SplitImpl() {
		super();
	}

	public SplitImpl( URI id ) {
		super( id );
	}

	public SplitImpl( Money m ) {
		super( m );
	}

	public SplitImpl( URI id, Money m ) {
		super( id, m );
	}

	public SplitImpl( Account a, Money m, String memo, ReconcileState rs ) {
		super( m, memo, rs );
		this.acct.setValue( a );
	}

	public SplitImpl( Split s ) {
		this( s.getAccount(), s.getRawValueProperty().getValue(),
				s.getMemo(), s.getReconciled() );
		super.setId( s.getId() );
	}

	@Override
	public String toString() {
		return getId().getLocalName() + " " + ( isDebit() ? "debit" : "credit" )
				+ ": " + getValue() + "; memo: " + getMemo() + " [" + acct.getValue() + "] {"
				+ getReconciled().toString().charAt( 0 ) + "}";
	}

	@Override
	public void setAccount( Account a ) {
		acct.setValue( a );
	}

	@Override
	public Account getAccount() {
		return acct.getValue();
	}

	@Override
	public Property<Account> getAccountProperty() {
		return acct;
	}
}
