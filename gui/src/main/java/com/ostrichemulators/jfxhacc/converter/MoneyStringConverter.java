/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.converter;

import com.ostrichemulators.jfxhacc.model.Money;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class MoneyStringConverter extends StringConverter<Money> {

	public static final Logger log = Logger.getLogger( MoneyStringConverter.class );

	@Override
	public String toString( Money t ) {
		if ( null == t ) {
			return null;
		}
		return t.toString();
	}

	@Override
	public Money fromString( String string ) {
		return Money.valueOf( string );
	}
}
