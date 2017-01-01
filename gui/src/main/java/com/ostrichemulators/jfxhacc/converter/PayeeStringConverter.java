/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.converter;

import com.ostrichemulators.jfxhacc.datamanager.PayeeManager;
import com.ostrichemulators.jfxhacc.model.Payee;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class PayeeStringConverter extends StringConverter<Payee> {

	public static final Logger log = Logger.getLogger( PayeeStringConverter.class );
	private final PayeeManager pman;

	public PayeeStringConverter( PayeeManager pm ) {
		pman = pm;
	}

	@Override
	public String toString( Payee t ) {
		if ( null == t ) {
			return null;
		}
		return t.getName();
	}

	@Override
	public Payee fromString( String string ) {
		for ( Payee j : pman.getAll() ) {
			if ( j.getName().equals( string ) ) {
				return j;
			}
		}

		return null; // should never get here
	}
}
