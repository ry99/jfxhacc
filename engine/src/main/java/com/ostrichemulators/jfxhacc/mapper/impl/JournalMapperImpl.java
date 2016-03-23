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
import java.util.ListIterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
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

	private final ObservableList<Journal> list = FXCollections.observableArrayList();
	private static final Logger log = Logger.getLogger( JournalMapperImpl.class );

	public JournalMapperImpl( RepositoryConnection rc ) {
		super( rc, Journals.TYPE );

		try {
			list.addAll( this.getAll() );
		}
		catch ( MapperException me ) {
			log.error( "problem pre-caching journals", me );
		}
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
			list.add( j );
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
		log.debug( "updating journal: " + t );
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		URI id = t.getId();

		try {
			rc.begin();
			rc.remove( id, RDFS.LABEL, null );
			rc.add( id, RDFS.LABEL, vf.createLiteral( t.getName() ) );

			for ( Journal j : list ) {
				if ( j.getId().equals( id ) ) {
					j.setName( t.getName() );
				}
			}

			rc.commit();
			notifyUpdated( t );
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public void remove( URI id ) throws MapperException {
		super.remove( id );
		ListIterator<Journal> li = list.listIterator();
		while ( li.hasNext() ) {
			Journal j = li.next();
			if ( j.getId().equals( id ) ) {
				li.remove();
				break;
			}
		}
	}

	@Override
	public ObservableList<Journal> getObservable() {
		return list;
	}
}
