/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.mapper.SplitMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class SplitMapperImpl extends RdfMapper<Split> implements SplitMapper {

	public static final Logger log = Logger.getLogger( SplitMapperImpl.class );

	public SplitMapperImpl( RepositoryConnection repoc ) {
		super( repoc, JfxHacc.SPLIT_TYPE );
	}

	@Override
	public Split get( URI id ) throws MapperException {
		Map<String, Value> bindings = new HashMap<>();
		bindings.put( "id", id );

		return query( "SELECT ?p ?o WHERE { ?id ?p ?o . FILTER( isLiteral( o ) }",
				bindings, new QueryHandler<Split>() {
					SplitImpl split = new SplitImpl();

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						final URI uri = URI.class.cast( set.getValue( "p" ) );
						final Literal literal = Literal.class.cast( set.getValue( "o" ) );

						if ( Splits.MEMO_PRED.equals( uri ) ) {
							split.setMemo( literal.getLabel() );
						}
						else if ( Splits.RECO_PRED.equals( uri ) ) {
							split.setReconciled( ReconcileState.valueOf( literal.stringValue() ) );
						}
						else if ( Splits.VALUE_PRED.equals( uri ) ) {
							split.setValue( new Money( literal.intValue() ) );
						}
					}

					@Override
					public Split getResult() {
						return split;
					}
				} );
	}

	@Override
	public void remove( URI id ) throws MapperException {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void update( Split t ) throws MapperException {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Map<Transaction, Map<Split, Account>> getSplits( List<Transaction> trans ) throws MapperException {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Split create( Split s, Account a ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();
			URI id = createBaseEntity( s );
			rc.add( new StatementImpl( id, Splits.ACCOUNT_PRED, a.getId() ) );
			rc.add( new StatementImpl( id, Splits.MEMO_PRED,
					vf.createLiteral( s.getMemo() ) ) );
			rc.add( new StatementImpl( id, Splits.VALUE_PRED,
					vf.createLiteral( s.getValue().value() ) ) );
			rc.add( new StatementImpl( id, Splits.RECO_PRED,
					vf.createLiteral( s.getReconciled().toString() ) ) );
			rc.commit();
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}

		return s;
	}

}
