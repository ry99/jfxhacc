/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import java.util.Collection;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public enum AccountType {

	ASSET( true, JfxHacc.ASSET ), EXPENSE( true, JfxHacc.EXPENSE ),
	LIABILITY( false, JfxHacc.LIABILITY ), EQUITY( false, JfxHacc.EQUITY ),
	REVENUE( false, JfxHacc.REVENUE );

	private final boolean debitPlus;
	private final URI uri;

	AccountType( boolean debplus, URI typeuri ) {
		debitPlus = debplus;
		uri = typeuri;
	}

	public boolean isDebitPlus() {
		return debitPlus;
	}

	public URI getUri() {
		return uri;
	}

	/**
	 * Returns a sum that can be used for determining an account's net
	 * positive/negative sum
	 *
	 * @param <T>
	 * @param s
	 * @return
	 */
	public <T extends SplitBase> int rawvalue( T s ) {
		int cents = s.getValue().value(); // cents is always positive at this point
		return ( debitPlus == s.isDebit() ? cents : -cents );
	}

	/**
	 * Returns a positive Money if the split would increase the account's value
	 *
	 * @param <T>
	 * @param s
	 * @return
	 */
	public <T extends SplitBase> Money value( T s ) {
		return new Money( rawvalue( s ) );
	}

	/**
	 * Set the money so that it decreases the value of the account
	 *
	 * @param m
	 * @return
	 */
	public Money decrease( Money m ) {
		return ( isPositive( m ) ? m.opposite() : m );
	}

	/**
	 * Sets the money so that it increases the value of the account
	 *
	 * @param m
	 * @return
	 */
	public Money increase( Money m ) {
		return ( isPositive( m ) ? m : m.opposite() );
	}

	public Money sum( Collection<? extends SplitBase> splits ) {
		int cents = 0;
		for ( SplitBase s : splits ) {
			cents += rawvalue( s );
		}

		return new Money( cents );
	}

	public boolean isPositive( Money m ) {
		int cents = m.value(); // negative cents is a DEBIT amount
		if ( debitPlus ) {
			return ( cents < 0 );
		}
		else {
			return ( cents > 0 );
		}
	}

	public <T extends SplitBase> boolean isPositive( T s ) {
		return ( debitPlus == s.isDebit() );
	}

	public static AccountType valueOf( URI uri ) {
		for ( AccountType at : values() ) {
			if ( at.getUri().equals( uri ) ) {
				return at;
			}
		}

		throw new IllegalArgumentException( "Unknown URI: " + uri );
	}
}
