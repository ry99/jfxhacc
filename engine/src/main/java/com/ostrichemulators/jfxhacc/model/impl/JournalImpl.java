/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.vocabulary.Journals;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class JournalImpl extends IDableImpl implements Journal {

	private final StringProperty name = new SimpleStringProperty();

	public JournalImpl( String name ) {
		super( Journals.TYPE );
	}

	public JournalImpl( URI id ) {
		super( Journals.TYPE, id );
	}

	public JournalImpl( URI id, String name ) {
		this( id );
		this.name.set( name );
	}

	@Override
	public String getName() {
		return name.get();
	}

	@Override
	public void setName( String name ) {
		this.name.set( name );
	}

	@Override
	public StringProperty getNameProperty() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
