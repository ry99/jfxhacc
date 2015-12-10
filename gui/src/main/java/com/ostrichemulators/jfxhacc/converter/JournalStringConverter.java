/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.converter;

import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class JournalStringConverter extends StringConverter<Journal> {

	public static final Logger log = Logger.getLogger(JournalStringConverter.class );
	private final Set<Journal> journals = new HashSet<>();

	public JournalStringConverter( Collection<Journal> js ){
		journals.addAll( js );
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
		for( Journal j : journals ){
			if( j.getName().equals(  string ) ){
				return j;
			}
		}
		
		return null; // should never get here
	}
}
