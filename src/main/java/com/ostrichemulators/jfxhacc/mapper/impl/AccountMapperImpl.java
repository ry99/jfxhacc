/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.impl.AccountImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class AccountMapperImpl extends SimpleEntityRdfMapper<Account> implements AccountMapper {

	private static final Logger log = Logger.getLogger( AccountMapperImpl.class );

	public AccountMapperImpl( RepositoryConnection rc ) {
		super( rc, JfxHacc.ACCOUNT_TYPE );
	}

	@Override
	protected void icreate( Account a, URI id, RepositoryConnection rc,
			ValueFactory vf ) throws RepositoryException {
		rc.add( new StatementImpl( id, RDFS.LABEL, vf.createLiteral( a.getName() ) ) );
		rc.add( new StatementImpl( id, Accounts.TYPE_PRED, a.getAccountType().getUri() ) );
		rc.add( new StatementImpl( id, Accounts.OBAL_PRED,
				vf.createLiteral( a.getOpeningBalance().value() ) ) );
	}

	@Override
	public Account get( URI id ) throws MapperException {
		Map<String, Value> bindings = new HashMap<>();
		bindings.put( "id", id );
		Value typeuri = oneval( id, Accounts.TYPE_PRED );
		Account acct = new AccountImpl( URI.class.cast( typeuri ) );

		return query( "SELECT ?p ?o WHERE { ?id ?p ?o . FILTER isLiteral( ?o ) }",
				bindings, new QueryHandler<Account>() {

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						final URI uri = URI.class.cast( set.getValue( "p" ) );
						final Literal literal = Literal.class.cast( set.getValue( "o" ) );

						if ( RDFS.LABEL.equals( uri ) ) {
							acct.setName( literal.stringValue() );
						}
						else if ( Accounts.OBAL_PRED.equals( uri ) ) {
							acct.setOpeningBalance( new Money( literal.intValue() ) );
						}
					}

					@Override
					public Account getResult() {
						return acct;
					}
				} );
	}


	@Override
	public void update( Account t ) throws MapperException {
	}

	@Override
	public Money getBalance( Account a, BalanceType type ) {
		if ( BalanceType.OPENING == type ) {
			return a.getOpeningBalance();
		}

		String sparql = "SELECT ?val WHERE {"
				+ "  ?split splits:value ?val . "
				+ "  ?split splits:reconciled ?reco ."
				+ "  ?split splits:account ?accountid "
				+ "}";
		Map<String, Value> map = bindmap( "accountid", a.getId() );
		if ( BalanceType.RECONCILED == type ) {
			map.put( "reco", new LiteralImpl( ReconcileState.RECONCILED.toString() ) );
		}

		try {
			Value val = oneval( sparql, map );
			int balance = ( null == val ? 0 : Literal.class.cast( val ).intValue() );
			return a.getOpeningBalance().add( new Money( balance ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		log.warn( "using opening balance instead of " + type );
		return a.getOpeningBalance();
	}
}
