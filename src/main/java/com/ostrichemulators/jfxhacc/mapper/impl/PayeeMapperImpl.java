/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.PayeeImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class PayeeMapperImpl extends SimpleEntityRdfMapper<Payee> implements PayeeMapper {

	public PayeeMapperImpl( RepositoryConnection rc ) {
		super( rc, JfxHacc.ACCOUNT_TYPE );
	}

	@Override
	protected void icreate( Payee a, URI id, RepositoryConnection rc,
			ValueFactory vf ) throws RepositoryException {
		rc.add( new StatementImpl( id, RDFS.LABEL, vf.createLiteral( a.getName() ) ) );
	}

	@Override
	public Payee get( URI id ) throws MapperException {
		Value label = oneval( id, RDFS.LABEL );
		return new PayeeImpl( id, label.stringValue() );
	}

	@Override
	public void update( Payee t ) throws MapperException {

	}

	@Override
	public Map<URI, String> getPayees() throws MapperException {
		return query( "SELECT ?id ?label WHERE { ?id a ?payee . ?id rdfs:label ?label }",
				super.bindmap( "payee", JfxHacc.PAYEE_TYPE ),
				new QueryHandler<Map<URI, String>>() {
					Map<URI, String> map = new HashMap<>();

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						map.put( URI.class.cast( set.getValue( "id" ) ),
								set.getValue( "label" ).stringValue() );
					}

					@Override
					public Map<URI, String> getResult() {
						return map;
					}
				} );
	}
}
