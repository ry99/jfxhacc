/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An immutable class for a single amount of money
 *
 * @author ryan
 */
public final class Money implements Comparable<Money> {

	private static final Pattern PATTERN
			= Pattern.compile( "^(-)?([0-9]+)?(\\W)?([0-9]+)?$" );
	private static final int DECIMALS = 2;
	private static final int ROLLOVER = (int) Math.pow( 10, DECIMALS );
	private final int cents;

	public Money( int cents ) {
		this.cents = cents;
	}

	public Money() {
		this( 0 );
	}

	public Money add( Money m ) {
		return new Money( cents + m.cents );
	}

	public static Money valueOf( String val ) {
		Money money;
		Matcher m = PATTERN.matcher( val );
		if ( m.matches() ) {
			String dollarval = m.group( 2 );
			int dollars = Integer.parseInt( null == dollarval ? "0" : dollarval );

			String centval = m.group( 4 );
			int cents = Integer.parseInt( null == centval ? "0" : centval );
			money = new Money( dollars * ROLLOVER + cents );
		}
		else {
			money = new Money( 0 );
		}

		return money;
	}

	public static Money valueOf( double dbl ) {
		String dval = String.format( "%4.2f", dbl );
		return valueOf( dval );
	}

	public int value() {
		return cents;
	}

	@Override
	public String toString() {
		return NumberFormat.getCurrencyInstance().format( (double) cents / 100d );
	}

	public String toPositiveString() {
		return NumberFormat.getCurrencyInstance().
				format( (double) ( cents < 0 ? -cents : cents ) / 100d );
	}

	public double toDouble() {
		return (double) cents / (double) ROLLOVER;
	}

	public boolean isNegative() {
		return cents < 0;
	}

	public boolean isPositive() {
		return cents > 0;
	}

	public boolean isZero() {
		return 0 == cents;
	}

	public boolean isNonZero() {
		return !isZero();
	}

	public Money opposite() {
		return new Money( -cents );
	}

	public Money abs() {
		return new Money( cents < 0 ? -cents : cents );
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + this.cents;
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		final Money other = (Money) obj;

		return this.cents == other.cents;
	}

	@Override
	public int compareTo( Money o ) {
		return cents - o.cents;
	}
}
