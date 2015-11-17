/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public enum AccountType {

	ASSET( false, JfxHacc.ASSET ), EXPENSE( false, JfxHacc.EXPENSE ),
	LIABILITY( true, JfxHacc.LIABILITY ), EQUITY( true, JfxHacc.EQUITY ),
	REVENUE( true, JfxHacc.REVENUE );

	private final boolean debitPlus;
	private final URI uri;

	AccountType( boolean rightplus, URI typeuri ) {
		debitPlus = rightplus;
		uri = typeuri;
	}

	public boolean isDebitPlus() {
		return debitPlus;
	}

	public URI getUri() {
		return uri;
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
