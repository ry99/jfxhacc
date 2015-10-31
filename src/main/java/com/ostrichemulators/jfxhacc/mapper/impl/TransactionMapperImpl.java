/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
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
public class TransactionMapperImpl extends SimpleEntityRdfMapper<Transaction>
		implements TransactionMapper {

	private static final Logger log = Logger.getLogger( TransactionMapperImpl.class );

	public TransactionMapperImpl( RepositoryConnection repoc ) {
		super( repoc, JfxHacc.TRANSACTION_TYPE );
	}

	@Override
	protected void icreate( Transaction a, URI id, RepositoryConnection rc,
			ValueFactory vf ) throws RepositoryException {
		rc.add( new StatementImpl( id, Transactions.PAYEE_PRED, a.getPayee() ) );
		rc.add( new StatementImpl( id, Transactions.DATE_PRED,
				vf.createLiteral( a.getDate() ) ) );
	}

	@Override
	public void remove( URI id ) throws MapperException {
		RepositoryConnection rc = getConnection();
		try {
			rc.begin();
			super.remove( id );
			for ( Statement s : Iterations.asList( rc.getStatements( id,
					Transactions.SPLIT_PRED, null, false ) ) ) {
				rc.remove( new StatementImpl( URI.class.cast( s.getObject() ), null, null ) );
			}

			rc.commit();
		}
		catch ( RepositoryException re ) {
			rollback( rc );
		}
	}

	@Override
	public Transaction get( URI id ) throws MapperException {
		Map<String, Value> bindings = new HashMap<>();
		bindings.put( "id", id );
		Transaction trans = new TransactionImpl( id );

		return query( "SELECT ?p ?o WHERE { ?id ?p ?o }",
				bindings, new QueryHandler<Transaction>() {

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						final URI uri = URI.class.cast( set.getValue( "p" ) );

						if ( Transactions.PAYEE_PRED.equals( uri ) ) {
							trans.setPayee( URI.class.cast( set.getValue( "o" ) ) );
						}
						else if ( Transactions.DATE_PRED.equals( uri ) ) {
							final Literal literal = Literal.class.cast( set.getValue( "o" ) );
							trans.setDate( DbUtil.toDate( literal ) );
						}
					}

					@Override
					public Transaction getResult() {
						return trans;
					}
				} );
	}

	@Override
	public void update( Transaction t ) throws MapperException {
	}

	@Override
	public Transaction getTransaction( Split s ) throws MapperException {
		Map<String, Value> bindings = new HashMap<>();
		bindings.put( "s", s.getId() );

		return query( "SELECT ?t ?p ?o WHERE { ?t trans:entry ?s. ?t p ?o }",
				bindings, new QueryHandler<Transaction>() {
					Transaction trans = new TransactionImpl();

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						trans.setId( URI.class.cast( set.getValue( "t" ) ) );

						final URI uri = URI.class.cast( set.getValue( "p" ) );
						if ( Transactions.PAYEE_PRED.equals( uri ) ) {
							trans.setPayee( URI.class.cast( set.getValue( "o" ) ) );
						}
						else if ( Transactions.DATE_PRED.equals( uri ) ) {
							final Literal literal = Literal.class.cast( set.getValue( "o" ) );
							trans.setDate( DbUtil.toDate( literal ) );
						}
					}

					@Override
					public Transaction getResult() {
						return trans;
					}
				} );
	}

	@Override
	public List<Transaction> getAll( Account acct ) throws MapperException {
		Map<String, Value> bindings = new HashMap<>();
		bindings.put( "acct", acct.getId() );

		return query( "SELECT ?t ?p ?o WHERE {"
				+ "  ?t trans:entry ?s."
				+ "  ?s splits:account ?acct ."
				+ "  ?t p ?o "
				+ "} ORDER BY ?t",
				bindings, new QueryHandler<List<Transaction>>() {
					List<Transaction> tlist = new ArrayList<>();
					TransactionImpl last = null;

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						URI id = URI.class.cast( set.getValue( "t" ) );
						if ( null == last || !last.getId().equals( id ) ) {
							last = new TransactionImpl( URI.class.cast( set.getValue( "t" ) ) );
							tlist.add( last );
						}

						final URI uri = URI.class.cast( set.getValue( "p" ) );
						if ( Transactions.PAYEE_PRED.equals( uri ) ) {
							last.setPayee( URI.class.cast( set.getValue( "o" ) ) );
						}
						else if ( Transactions.DATE_PRED.equals( uri ) ) {
							final Literal literal = Literal.class.cast( set.getValue( "o" ) );
							last.setDate( DbUtil.toDate( literal ) );
						}
					}

					@Override
					public List<Transaction> getResult() {
						return tlist;
					}
				} );
	}
}
