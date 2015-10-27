/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class AccountImpl extends IDableImpl implements Account {

	private String name;
	private final AccountType type;
	private Money openingbal;

	public AccountImpl( AccountType atype, URI id ) {
		super( JfxHacc.ACCOUNT_TYPE, id );
		type = atype;
		openingbal = new Money( 0 );
	}

	public AccountImpl( AccountType atype ) {
		super( JfxHacc.ACCOUNT_TYPE );
		type = atype;
		openingbal = new Money( 0 );
	}

	public AccountImpl( URI atype ) {
		this( AccountType.valueOf( atype ) );
	}

	public AccountImpl( AccountType atype, String name ) {
		this( atype );
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName( String n ) {
		name = n;
	}

	@Override
	public AccountType getAccountType() {
		return type;
	}

	@Override
	public void setOpeningBalance( Money money ) {
		openingbal = money;
	}

	@Override
	public Money getOpeningBalance() {
		return openingbal;
	}
}
