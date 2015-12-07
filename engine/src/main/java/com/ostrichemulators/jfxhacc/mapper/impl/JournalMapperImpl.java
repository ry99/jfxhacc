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
import com.ostrichemulators.jfxhacc.model.vocabulary.Journals;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class JournalMapperImpl extends SimpleEntityRdfMapper<Journal> implements JournalMapper {

	public JournalMapperImpl( RepositoryConnection rc ) {
		super( rc, Journals.TYPE );
	}

	@Override
	public Journal create( String name ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();

		try {
			rc.begin();
			URI id = createBaseEntity();
			rc.add( id, RDFS.LABEL, vf.createLiteral( name ) );
			rc.commit();
			Journal j = new JournalImpl( id, name );
			notifyAdded( j );
			return j;
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Journal get( URI id ) throws MapperException {
		Value label = oneval( id, RDFS.LABEL );
		return new JournalImpl( id, label.stringValue() );
	}

	@Override
	public void update( Journal t ) throws MapperException {
		notifyUpdated( t );
	}
}
