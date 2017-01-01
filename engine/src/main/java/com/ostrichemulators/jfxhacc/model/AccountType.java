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
	public <T extends SplitBase> int sumValue( T s ) {
		Money m = s.getValue(); // m is always positive at this point
		return ( isPositive( s ) ? m : m.opposite() ).value();
	}

	/**
	 * Gets the net positive/negative value of this list of splits
	 *
	 * @param splits
	 * @return
	 */
	public Money sum( Collection<? extends SplitBase> splits ) {
		int cents = 0;
		for ( SplitBase s : splits ) {
			cents += sumValue( s );
		}

		return new Money( cents );
	}

	/**
	 * Sets the split's value so that it increases the split account's value
	 *
	 * @param <T>
	 * @param split
	 * @param m
	 */
	public <T extends SplitBase> void increase( T split, Money m ) {
		if ( debitPlus ) {
			split.setDebit( m.abs() );
		}
		else {
			split.setCredit( m.abs() );
		}
	}

	/**
	 * Sets the split's value so that it decreases the split account's value
	 *
	 * @param <T>
	 * @param split
	 * @param m
	 */
	public <T extends SplitBase> void decrease( T split, Money m ) {
		if ( debitPlus ) {
			split.setCredit( m.abs() );
		}
		else {
			split.setDebit( m.abs() );
		}
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
