/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.mapper.NamedIDable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class NamedIDableImpl extends IDableImpl implements NamedIDable {

	private static final Logger log = Logger.getLogger( NamedIDableImpl.class );
	private final StringProperty name = new SimpleStringProperty();

	public NamedIDableImpl( URI type, URI id, String name ) {
		super( type, id );
		this.name.setValue( name );
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
