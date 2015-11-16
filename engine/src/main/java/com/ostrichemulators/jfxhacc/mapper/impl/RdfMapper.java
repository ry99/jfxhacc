/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.DataMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.model.IDable;
import com.ostrichemulators.jfxhacc.utility.UriUtil;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public abstract class RdfMapper<T extends IDable> implements DataMapper<T> {

	private static final Logger log = Logger.getLogger( RdfMapper.class );
	private final List<MapperListener<T>> listenees = new ArrayList<>();
	private final RepositoryConnection rc;
	private final URI type;

	protected RdfMapper( RepositoryConnection repoc, URI type ) {
		this.type = type;
		this.rc = repoc;
	}

	protected RepositoryConnection getConnection() {
		return rc;
	}

	/**
	 * Creates a rdf statement that creates a new ID and assigns this mapper's
	 * type
	 *
	 * @return the new id
	 * @throws RepositoryException
	 */
	protected URI createBaseEntity() throws RepositoryException {
		URI id = UriUtil.randomUri( type );
		rc.add( new StatementImpl( id, RDF.TYPE, type ) );
		return id;
	}

	@Override
	public Collection<T> getAll() throws MapperException {
		List<T> list = new ArrayList<>();
		try {
			List<Statement> stmts = Iterations.asList( rc.getStatements( null,
					RDF.TYPE, type, false ) );
			for ( Statement s : stmts ) {
				URI id = URI.class.cast( s.getSubject() );
				list.add( get( id ) );
			}
		}
		catch ( RepositoryException re ) {
			throw new MapperException( re );
		}

		return list;
	}

	@Override
	public void remove( T obj ) throws MapperException {
		remove( obj.getId() );
	}

	@Override
	public void release() {
	}

	protected <X> X query( String sparql, Map<String, Value> bindings, QueryHandler<X> handler )
			throws MapperException {
		try {
			// Ugh: why isn't the repositoryconnection handling the namespaces for us?
			StringBuilder sb = new StringBuilder();
			for ( Namespace ns : Iterations.asList( rc.getNamespaces() ) ) {
				sb.append( "PREFIX " ).append( ns.getPrefix() ).
						append( ": <" ).append( ns.getName() ).append( ">\n" );
			}
			sb.append( sparql );

			TupleQuery tq = rc.prepareTupleQuery( QueryLanguage.SPARQL, sb.toString() );
			if ( null != bindings ) {
				for ( Map.Entry<String, Value> en : bindings.entrySet() ) {
					tq.setBinding( en.getKey(), en.getValue() );
				}
			}
			TupleQueryResult tqr = tq.evaluate();
			while ( tqr.hasNext() ) {
				handler.handleTuple( tqr.next(), rc.getValueFactory() );
			}
			tqr.close();
		}
		catch ( MalformedQueryException | QueryEvaluationException re ) {
			log.error( "BUG: invalid sparql in mapper?: " + sparql );
		}
		catch ( RepositoryException re ) {
			throw new MapperException( re );
		}

		return handler.getResult();
	}

	protected Value oneval( String sparql, Map<String, Value> bindings ) throws MapperException {
		return query( sparql, bindings, new QueryHandler<Value>() {
			Value val;

			@Override
			public void handleTuple( BindingSet set, ValueFactory vf ) {
				val = set.iterator().next().getValue();
			}

			@Override
			public Value getResult() {
				return val;
			}
		} );
	}

	protected Value oneval( URI id, URI predicate ) throws MapperException {
		try {
			List<Statement> stmts
					= Iterations.asList( rc.getStatements( id, predicate, null, false ) );
			return ( stmts.isEmpty() ? null : stmts.get( 0 ).getObject() );
		}
		catch ( RepositoryException x ) {
			throw new MapperException( x );
		}
	}

	protected void exec( String sparql, Map<String, Value> bindings ) throws MapperException {
		try {
			Update upd = rc.prepareUpdate( QueryLanguage.SPARQL, sparql );
			if ( null != bindings ) {
				for ( Map.Entry<String, Value> en : bindings.entrySet() ) {
					upd.setBinding( en.getKey(), en.getValue() );
				}
			}

			upd.execute();
		}
		catch ( MalformedQueryException | UpdateExecutionException re ) {
			log.error( "BUG: invalid sparql in mapper?: " + sparql );
		}
		catch ( RepositoryException re ) {
			throw new MapperException( re );
		}
	}

	protected Map<String, Value> bindmap( String bind, String val ) {
		Map<String, Value> map = new HashMap<>();
		map.put( bind, rc.getValueFactory().createLiteral( val ) );
		return map;
	}

	protected Map<String, Value> bindmap( String bind, URI val ) {
		Map<String, Value> map = new HashMap<>();
		map.put( bind, val );
		return map;
	}

	protected Map<String, Value> bindmap( String bind, int val ) {
		Map<String, Value> map = new HashMap<>();
		map.put( bind, rc.getValueFactory().createLiteral( val ) );
		return map;
	}

	protected void rollback( RepositoryConnection rc ) {
		try {
			rc.rollback();
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
		}
	}

	@Override
	public void addMapperListener( MapperListener<T> l ) {
		listenees.add( l );
	}

	@Override
	public void removeMapperListener( MapperListener<T> l ) {
		listenees.remove( l );
	}

	protected void notifyRemoved( URI u ) {
		for ( MapperListener ml : listenees ) {
			ml.removed( u );
		}
	}

	protected void notifyAdded( T t ) {
		for ( MapperListener<T> ml : listenees ) {
			ml.added( t );
		}
	}

	protected void notifyUpdated( T t ) {
		for ( MapperListener<T> ml : listenees ) {
			ml.updated( t );
		}
	}
}
