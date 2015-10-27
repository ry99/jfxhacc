/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.JournalMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.impl.JournalImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class JournalMapperImpl extends RdfMapper<Journal> implements JournalMapper {

	public JournalMapperImpl( RepositoryConnection rc ) {
		super( rc, JfxHacc.ACCOUNT_TYPE );
	}

	@Override
	protected void icreate( Journal a, ValueFactory vf ) throws RepositoryException {
		getConnection().add( new StatementImpl( a.getId(), RDFS.LABEL,
				vf.createLiteral( a.getName() ) ) );
	}

	@Override
	public Journal get( URI id ) throws MapperException {
		Value label = oneval( id, RDFS.LABEL );
		return new JournalImpl( id, label.stringValue() );
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

	@Override
	public void update( Journal t ) throws MapperException {

	}
}
