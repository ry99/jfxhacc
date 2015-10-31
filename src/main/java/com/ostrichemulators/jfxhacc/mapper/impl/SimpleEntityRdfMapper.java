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
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * A class to create entities that don't depend on something else (e.g., splits
 * depend on an Account; Accounts don't depend on anything).
 *
 * @author ryan
 */
public abstract class SimpleEntityRdfMapper<T extends IDable> extends RdfMapper<T> {

	private static final Logger log = Logger.getLogger( SimpleEntityRdfMapper.class );

	public SimpleEntityRdfMapper( RepositoryConnection repoc, URI type ) {
		super( repoc, type );
	}

	public T create( T a ) throws MapperException {
		RepositoryConnection rc = getConnection();

		try {
			rc.begin();
			URI id = createBaseEntity( a );
			icreate( a, id, rc, rc.getValueFactory() );
			rc.commit();
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			log.error( re, re );
		}

		return a;
	}

	@Override
	public void remove( URI id ) throws MapperException {
		try {
			getConnection().remove( id, null, null );
		}
		catch ( Exception e ) {
			throw new MapperException( e );
		}
	}

	/**
	 * Adds the details of this entity to the repository
	 *
	 * @param a
	 * @param id the ID of the given entity
	 * @param rc
	 * @param vf
	 * @throws RepositoryException
	 */
	protected abstract void icreate( T a, URI id, RepositoryConnection rc,
			ValueFactory vf ) throws RepositoryException;
}
