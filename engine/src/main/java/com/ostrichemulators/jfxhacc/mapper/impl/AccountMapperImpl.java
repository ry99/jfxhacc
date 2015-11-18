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
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.impl.AccountImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import com.ostrichemulators.jfxhacc.utility.TreeNode;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
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
	public Account create( String name, AccountType type, Money obal, Account parent )
			throws MapperException {

		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();
			URI id = createBaseEntity();
			rc.add( id, RDFS.LABEL, vf.createLiteral( name ) );
			rc.add( id, Accounts.TYPE_PRED, type.getUri() );
			rc.add( id, Accounts.OBAL_PRED, vf.createLiteral( obal.value() ) );
			if ( null != parent ) {
				rc.add( id, Accounts.PARENT_PRED, parent.getId() );
			}
			rc.commit();

			AccountImpl ai = new AccountImpl( id, name, type, obal );
			notifyAdded( ai );
			return ai;
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Account get( URI id ) throws MapperException {
		Map<String, Value> bindings = new HashMap<>();
		bindings.put( "id", id );
		Value typeuri = oneval( id, Accounts.TYPE_PRED );
		AccountType type = AccountType.valueOf( URI.class.cast( typeuri ) );
		Account acct = new AccountImpl( type, id );

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

		notifyUpdated( t );
	}

	@Override
	public Account getParent( Account a ) throws MapperException {
		Value pid = oneval( a.getId(), Accounts.PARENT_PRED );
		return ( null == pid ? null : get( URI.class.cast( pid ) ) );
	}

	@Override
	public TreeNode<Account> getAccounts( AccountType type ) throws MapperException {

		Map<URI, URI> childparent = query( "SELECT ?child ?parent WHERE {"
				+ "  ?child a jfxhacc:account . "
				+ "  ?child accounts:accountType ?type . "
				+ "  OPTIONAL { ?parent a jfxhacc:account . ?child accounts:parent ?parent }"
				+ "} ORDER BY DESC( ?parent )",
				bindmap( "type", type.getUri() ),
				new QueryHandler<Map<URI, URI>>() {
					Map<URI, URI> map = new LinkedHashMap<>();

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						URI child = URI.class.cast( set.getValue( "child" ) );
						URI parent = URI.class.cast( set.getValue( "parent" ) );
						map.put( child, parent );
					}

					@Override
					public Map<URI, URI> getResult() {
						return map;
					}
				} );

		Map<URI, Account> accts = new HashMap<>();
		for ( Account acct : getAll() ) {
			accts.put( acct.getId(), acct );
		}

		TreeNode<Account> root = new TreeNode<>();
		Map<URI, TreeNode<Account>> tree = new HashMap<>();
		for ( Map.Entry<URI, URI> en : childparent.entrySet() ) {
			URI childid = en.getKey();
			URI parentid = en.getValue();

			Account acct = accts.get( childid );
			if ( !tree.containsKey( childid ) ) {
				tree.put( childid, new TreeNode<>( acct ) );
			}
			TreeNode<Account> child = tree.get( childid );

			if ( null == parentid ) {
				root.addChild( child );
			}
			else {

				if ( !tree.containsKey( parentid ) ) {
					tree.put( parentid, new TreeNode<>( accts.get( parentid ) ) );
				}

				TreeNode<Account> pnode = tree.get( parentid );
				pnode.addChild( child );
			}
		}

		if ( log.isTraceEnabled() ) {
			TreeNode.dump( root,
					new PrintWriter( new OutputStreamWriter( System.out ) ), 0 );
		}

		return root;
	}

	@Override
	public Money getBalance( Account a, BalanceType type ) {
		if ( BalanceType.OPENING == type ) {
			return a.getOpeningBalance();
		}

		String sparql = "SELECT ( SUM( ?val ) AS ?sum ) WHERE {"
				+ "  ?split ?sval ?val . "
				+ "  ?split ?sreco ?reco ."
				+ "  ?split ?sacct ?accountid "
				+ "}";
		Map<String, Value> map = bindmap( "accountid", a.getId() );
		map.put( "sval", Splits.VALUE_PRED );
		map.put( "sreco", Splits.RECO_PRED );
		map.put( "sacct", Splits.ACCOUNT_PRED );
		if ( BalanceType.RECONCILED == type ) {
			map.put( "reco", new LiteralImpl( ReconcileState.RECONCILED.toString() ) );
		}

		try {
			Value val = oneval( sparql, map );
			int balance = ( null == val ? 0 : Literal.class.cast( val ).intValue() );
			if ( a.getAccountType().isDebitPlus() ) {
				balance = 0 - balance;
			}

			return a.getOpeningBalance().add( new Money( balance ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		log.warn( "using opening balance instead of " + type );
		return a.getOpeningBalance();
	}

	@Override
	public Money getBalance( Account a, BalanceType type, Date asof ) {
		if ( BalanceType.OPENING == type ) {
			return a.getOpeningBalance();
		}

		String sparql = "SELECT ( SUM( ?val ) AS ?sum ) WHERE {"
				+ "  ?split ?sval ?val . "
				+ "  ?split ?sreco ?reco ."
				+ "  ?split ?sacct ?accountid ."
				+ "  ?trans ?entry ?split ."
				+ "  ?trans ?tdate ?date ."
				+ "  FILTER ( ?date < ?asof )"
				+ "}";
		Map<String, Value> map = bindmap( "accountid", a.getId() );
		map.put( "sval", Splits.VALUE_PRED );
		map.put( "sreco", Splits.RECO_PRED );
		map.put( "sacct", Splits.ACCOUNT_PRED );
		map.put( "entry", Transactions.SPLIT_PRED );
		map.put( "tdate", Transactions.DATE_PRED );
		map.put( "asof", new ValueFactoryImpl().createLiteral( asof ) );
		if ( BalanceType.RECONCILED == type ) {
			map.put( "reco", new LiteralImpl( ReconcileState.RECONCILED.toString() ) );
		}

		try {
			Value val = oneval( sparql, map );
			int balance = ( null == val ? 0 : Literal.class.cast( val ).intValue() );
			if ( a.getAccountType().isDebitPlus() ) {
				balance = 0 - balance;
			}

			return a.getOpeningBalance().add( new Money( balance ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		log.warn( "using opening balance instead of " + type );
		return a.getOpeningBalance();
	}

	@Override
	public List<Account> getParents( Account a ) throws MapperException {
		List<Account> parents = new ArrayList<>();
		Account par = getParent( a );
		if ( null != par ) {
			parents.addAll( getParents( par ) );
			parents.add( par );
		}

		return parents;
	}

	@Override
	public Map<Account, Account> getParentMap() throws MapperException {
		Map<Account, Account> accts = new HashMap<>();

		for ( Account a : getAll() ) {
			accts.put( a, getParent( a ) );
		}

		return accts;
	}
}
