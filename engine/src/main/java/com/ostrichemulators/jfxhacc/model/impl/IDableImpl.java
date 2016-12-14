/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.IDable;
import com.ostrichemulators.jfxhacc.utility.UriUtil;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class IDableImpl implements IDable {

	private final ObjectProperty<URI> id = new SimpleObjectProperty<>();
	private final URI type;

	protected IDableImpl( URI type, URI id ) {
		this.id.setValue( id );
		this.type = type;
	}

	protected IDableImpl( URI type ) {
		this( type, UriUtil.randomUri( type ) );
	}

	@Override
	public URI getId() {
		return id.getValue();
	}

	@Override
	public Property<URI> getIdProperty() {
		return id;
	}

	@Override
	public void setId( URI id ) {
		this.id.setValue( id );
	}

	@Override
	public URI getType() {
		return type;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + Objects.hashCode( this.id.get() );
		hash = 53 * hash + Objects.hashCode( this.type );
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
		final IDableImpl other = (IDableImpl) obj;
		if ( !Objects.equals( this.id.get(), other.id.get() ) ) {
			return false;
		}
		if ( !Objects.equals( this.type, other.type ) ) {
			return false;
		}
		return true;
	}
}
