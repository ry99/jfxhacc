/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.IDable;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

/**
 * A class to create entities that don't depend on something else (e.g., splits
 * depend on an Account; Accounts don't depend on anything).
 *
 * @author ryan
 * @param <T>
 */
public abstract class SimpleEntityRdfMapper<T extends IDable> extends RdfMapper<T> {

	private static final Logger log = Logger.getLogger( SimpleEntityRdfMapper.class );

	public SimpleEntityRdfMapper( RepositoryConnection repoc, URI type ) {
		super( repoc, type );
	}

	@Override
	public void remove( URI id ) throws MapperException {
		try {
			getConnection().remove( id, null, null );
			notifyRemoved( id );
		}
		catch ( Exception e ) {
			throw new MapperException( e );
		}
	}
}
