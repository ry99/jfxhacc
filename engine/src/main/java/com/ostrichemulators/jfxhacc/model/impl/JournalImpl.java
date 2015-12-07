/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.vocabulary.Journals;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class JournalImpl extends IDableImpl implements Journal {

	private String name;

	public JournalImpl( String name ) {
		super( Journals.TYPE );
	}

	public JournalImpl( URI id ) {
		super( Journals.TYPE, id );
	}

	public JournalImpl( URI id, String name ) {
		this( id );
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName( String name ) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
