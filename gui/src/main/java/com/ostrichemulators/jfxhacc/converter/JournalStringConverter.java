/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.converter;

import com.ostrichemulators.jfxhacc.datamanager.JournalManager;
import com.ostrichemulators.jfxhacc.model.Journal;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class JournalStringConverter extends StringConverter<Journal> {

	public static final Logger log = Logger.getLogger( JournalStringConverter.class );
	private final JournalManager jman;

	public JournalStringConverter( JournalManager jm ) {
		jman = jm;
	}

	@Override
	public String toString( Journal t ) {
		if ( null == t ) {
			return null;
		}
		return t.getName();
	}

	@Override
	public Journal fromString( String string ) {
		for ( Journal j : jman.getAll() ) {
			if ( j.getName().equals( string ) ) {
				return j;
			}
		}

		return null; // should never get here
	}
}
