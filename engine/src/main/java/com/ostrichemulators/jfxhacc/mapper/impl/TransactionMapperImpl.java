/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.mapper.TransactionListener;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import com.ostrichemulators.jfxhacc.utility.UriUtil;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class TransactionMapperImpl extends RdfMapper<Transaction>
		implements TransactionMapper {

	private static final Logger log = Logger.getLogger( TransactionMapperImpl.class );
	private final PayeeMapper pmap;
	private final AccountMapper amap;
	private final List<TransactionListener> listenees = new ArrayList<>();

	public TransactionMapperImpl( RepositoryConnection repoc, AccountMapper amap,
			PayeeMapper pmap ) {
		this( repoc, amap, pmap, Transactions.TYPE );
	}

	public TransactionMapperImpl( RepositoryConnection repoc, AccountMapper amap,
			PayeeMapper pmap, URI type ) {
		super( repoc, type );
		this.pmap = pmap;
		this.amap = amap;
	}

	@Override
	public void addMapperListener( TransactionListener tl ) {
		super.addMapperListener( tl );
		listenees.add( tl );
	}

	@Override
	public void removeMapperListener( TransactionListener tl ) {
		super.removeMapperListener( tl );
		listenees.remove( tl );
	}

	private Split create( Split s, URI id, boolean dotrans ) throws RepositoryException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();

		if ( dotrans ) {
			rc.begin();
		}

		if ( null == id ) {
			id = UriUtil.randomUri( Splits.TYPE );
		}
		s.setId( id );
		rc.add( id, RDF.TYPE, Splits.TYPE );
		rc.add( id, Splits.ACCOUNT_PRED, s.getAccount().getId() );
		if ( null != s.getMemo() ) {
			rc.add( id, Splits.MEMO_PRED, vf.createLiteral( s.getMemo() ) );
		}

		Money m = s.getValue();
		if ( s.isDebit() ) {
			m = m.opposite();
		}

		rc.add( id, Splits.VALUE_PRED, vf.createLiteral( m.value() ) );
		rc.add( id, Splits.RECO_PRED, vf.createLiteral( s.getReconciled().toString() ) );

		if ( dotrans ) {
			rc.commit();
		}

		return new SplitImpl( s );
	}

	@Override
	public Transaction create( Date d, Payee p, String number, Collection<Split> splits,
			Journal journal ) throws MapperException {
		Transaction transaction = new TransactionImpl();
		transaction.setJournal( journal );
		transaction.setDate( d );
		transaction.setPayee( p );
		transaction.setNumber( number );
		transaction.setSplits( new HashSet<>( splits ) );
		return create( transaction );
	}

	@Override
	public Transaction create( Transaction t ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			if ( !rc.isActive() ) {
				rc.begin();
			}

			Map<Split, URI> realsplits = new HashMap<>();
			for ( Split sp : t.getSplits() ) {
				Split s = create( sp, null, false );
				realsplits.put( s, s.getId() );
			}

			URI id = createBaseEntity();

			rc.add( id, Transactions.PAYEE_PRED, t.getPayee().getId() );
			rc.add( id, Transactions.DATE_PRED, vf.createLiteral( t.getDate() ) );
			if ( null != t.getNumber() ) {
				rc.add( id, Transactions.NUMBER_PRED, vf.createLiteral( t.getNumber() ) );
			}

			rc.add( id, Transactions.JOURNAL_PRED, t.getJournal().getId() );
			for ( URI splitid : realsplits.values() ) {
				rc.add( id, Transactions.SPLIT_PRED, splitid );
			}

			if ( !rc.isActive() ) {
				rc.commit();
			}

			TransactionImpl trans
					= new TransactionImpl( id, t.getDate(), t.getNumber(), t.getPayee() );
			trans.setSplits( realsplits.keySet() );
			notifyAdded( trans );
			return trans;
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public void remove( URI id ) throws MapperException {
		RepositoryConnection rc = getConnection();
		try {
			rc.begin();
			rc.remove( id, null, null );
			for ( Statement s : Iterations.asList( rc.getStatements( id,
					Transactions.SPLIT_PRED, null, false ) ) ) {
				rc.remove( URI.class.cast( s.getObject() ), null, null );
			}

			rc.commit();
			notifyRemoved( id );
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
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
							setPayee( trans, URI.class.cast( set.getValue( "o" ) ) );
						}
						else if ( Transactions.DATE_PRED.equals( uri ) ) {
							final Literal literal = Literal.class.cast( set.getValue( "o" ) );
							trans.setDate( DbUtil.toDate( literal ) );
						}
						else if ( Transactions.NUMBER_PRED.equals( uri ) ) {
							trans.setNumber( set.getValue( "o" ).stringValue() );
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
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();

		try {
			rc.begin();

			Set<Split> realsplits = updateSplits( getSplitMap( t.getId() ),
					new HashSet<>( t.getSplits() ), rc );

			URI id = t.getId();
			rc.remove( id, Transactions.PAYEE_PRED, null );
			rc.remove( id, Transactions.DATE_PRED, null );
			rc.remove( id, Transactions.NUMBER_PRED, null );
			rc.remove( id, Transactions.SPLIT_PRED, null );

			rc.add( id, Transactions.PAYEE_PRED, t.getPayee().getId() );
			rc.add( id, Transactions.DATE_PRED, vf.createLiteral( t.getDate() ) );
			if ( null != t.getNumber() ) {
				rc.add( id, Transactions.NUMBER_PRED, vf.createLiteral( t.getNumber() ) );
			}

			for ( Split split : realsplits ) {
				rc.add( id, Transactions.SPLIT_PRED, split.getId() );
			}
			rc.commit();

			t.setSplits( realsplits );
			notifyUpdated( t );
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Transaction getTransaction( Split s ) throws MapperException {
		try {
			List<Statement> stmts = Iterations.asList( getConnection().
					getStatements( null, Transactions.SPLIT_PRED, s.getId(), false ) );
			if ( stmts.isEmpty() ) {
				log.warn( "split " + s.getId() + " is not part of any transaction" );
				return null;
			}

			Transaction trans = get( URI.class.cast( stmts.get( 0 ).getSubject() ) );
			trans.setSplits( getSplitMap( trans.getId() ) );
			return trans;
		}
		catch ( RepositoryException re ) {
			throw new MapperException( re );
		}
	}

	/**
	 * Gets a mapping of split-to-accountid
	 *
	 * @param transid
	 * @return
	 * @throws MapperException
	 */
	private Map<Split, URI> getSplits( URI transid ) throws MapperException {
		return query( "SELECT ?s ?memo ?reco ?val ?aid WHERE {"
				+ "  ?t trans:entry ?s."
				+ "  ?s splits:account ?aid ."
				+ "  OPTIONAL { ?s splits:memo ?memo } ."
				+ "  ?s splits:value ?val ."
				+ "  OPTIONAL { ?s splits:reconciled ?reco } ."
				+ "} ORDER BY ?t", bindmap( "t", transid ), new QueryHandler<Map<Split, URI>>() {
					Map<Split, URI> lkp = new HashMap<>();

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						URI acctid = URI.class.cast( set.getValue( "aid" ) );
						URI splitid = URI.class.cast( set.getValue( "s" ) );

						Value val = set.getValue( "memo" );
						String memo = ( null == val ? "" : val.stringValue() );
						val = set.getValue( "reco" );
						ReconcileState rs = ( null == val
								? ReconcileState.NOT_RECONCILED
								: ReconcileState.valueOf( val.stringValue() ) );

						val = set.getValue( "val" );
						int value = ( null == val ? 0
								: Literal.class.cast( val ).intValue() );

						Split split = new SplitImpl( splitid, new Money( value ) );
						split.setMemo( memo );
						split.setReconciled( rs );

						lkp.put( split, acctid );
					}

					@Override
					public Map<Split, URI> getResult() {
						return lkp;
					}
				} );
	}

	protected Set<Split> getSplitMap( URI transid ) throws MapperException {
		Map<URI, Account> accounts = new HashMap<>();
		Map<Split, URI> splitos = getSplits( transid );
		Set<Split> splits = new HashSet<>();

		for ( Map.Entry<Split, URI> en : splitos.entrySet() ) {
			URI acctid = en.getValue();
			if ( !accounts.containsKey( acctid ) ) {
				accounts.put( acctid, amap.get( acctid ) );
			}

			Split s = en.getKey();
			s.setAccount( accounts.get( acctid ) );
			splits.add( s );
		}

		return splits;
	}

	@Override
	public void reconcile( ReconcileState rs, Account acct, Split... splits ) throws MapperException {
		RepositoryConnection rc = getConnection();
		try {
			rc.begin();
			for ( Split s : splits ) {
				rc.remove( s.getId(), Splits.RECO_PRED, null );
				rc.add( s.getId(), Splits.RECO_PRED, new LiteralImpl( rs.toString() ) );
				s.setReconciled( rs );
			}
			rc.commit();

			List<Split> list = Arrays.asList( splits );
			for ( TransactionListener tl : listenees ) {
				tl.reconciled( acct, list );
			}
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public List<Transaction> getAll( Account acct, Journal journal ) throws MapperException {

		Map<String, Value> bindings = bindmap( "acct", acct.getId() );
		bindings.put( "jnl", journal.getId() );

		// don't include recurring transactions
		List<Transaction> transactions = query( "SELECT ?t ?p ?o WHERE {"
				+ "  ?t trans:entry ?s . FILTER NOT EXISTS { ?t recur:recurrence ?z } ."
				+ "  ?t a jfxhacc:transaction ."
				+ "  ?s splits:account ?acct ."
				+ "  ?t trans:journal ?jnl ."
				+ "  ?t ?p ?o "
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
							setPayee( last, URI.class.cast( set.getValue( "o" ) ) );
						}
						else if ( Transactions.DATE_PRED.equals( uri ) ) {
							final Literal literal = Literal.class.cast( set.getValue( "o" ) );
							last.setDate( DbUtil.toDate( literal ) );
						}
						else if ( Transactions.NUMBER_PRED.equals( uri ) ) {
							last.setNumber( set.getValue( "o" ).stringValue() );
						}
					}

					@Override
					public List<Transaction> getResult() {
						return tlist;
					}
				} );

		Map<URI, Account> accounts = new HashMap<>();
		for ( Transaction t : transactions ) {
			Map<Split, URI> splitos = getSplits( t.getId() );

			for ( Map.Entry<Split, URI> en : splitos.entrySet() ) {
				URI acctid = en.getValue();
				if ( !accounts.containsKey( acctid ) ) {
					accounts.put( acctid, amap.get( acctid ) );
				}

				Split s = en.getKey();
				s.setAccount( accounts.get( en.getValue() ) );
				t.addSplit( s );
			}
		}

		return transactions;
	}

	@Override
	public List<Transaction> getUnreconciled( Account acct, Journal journal, Date asof )
			throws MapperException {

		Map<String, Value> bindings = bindmap( "acct", acct.getId() );
		bindings.put( "jnl", journal.getId() );
		bindings.put( "asof", new ValueFactoryImpl().createLiteral( asof ) );
		bindings.put( "recostate", new LiteralImpl( ReconcileState.RECONCILED.toString() ) );

		List<Transaction> transactions = query( "SELECT ?t ?p ?o WHERE {"
				+ "  ?t trans:entry ?s . FILTER NOT EXISTS { ?t recur:recurrence ?z } ."
				+ "  ?s splits:account ?acct ."
				+ "  ?t trans:journal ?jnl ."
				+ "  ?s splits:reconciled ?reco FILTER( ?reco != ?recostate ) ."
				+ "  ?t dcterms:created ?date FILTER( xsd:dateTime( ?date ) < ?asof ) ."
				+ "  ?t ?p ?o "
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
							setPayee( last, URI.class.cast( set.getValue( "o" ) ) );
						}
						else if ( Transactions.DATE_PRED.equals( uri ) ) {
							final Literal literal = Literal.class.cast( set.getValue( "o" ) );
							last.setDate( DbUtil.toDate( literal ) );
						}
						else if ( Transactions.NUMBER_PRED.equals( uri ) ) {
							last.setNumber( set.getValue( "o" ).stringValue() );
						}
					}

					@Override
					public List<Transaction> getResult() {
						return tlist;
					}
				} );

		Map<URI, Account> accounts = new HashMap<>();
		for ( Transaction t : transactions ) {
			Map<Split, URI> splitos = getSplits( t.getId() );

			for ( Map.Entry<Split, URI> en : splitos.entrySet() ) {
				URI acctid = en.getValue();
				if ( !accounts.containsKey( acctid ) ) {
					accounts.put( acctid, amap.get( acctid ) );
				}

				Split s = en.getKey();
				s.setAccount( accounts.get( en.getValue() ) );
				t.addSplit( s );
			}
		}

		return transactions;
	}

	private void setPayee( Transaction t, URI payeeid ) {
		try {
			t.setPayee( pmap.get( payeeid ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	private Set<Split> updateSplits( Set<Split> oldsplits, Set<Split> newsplits,
			RepositoryConnection rc ) throws RepositoryException {
		// if a split is in both maps, update it
		// if it's in the old but not the new, remove it
		// if it's in the new but not the old, add it

		Set<Split> realsplits = new HashSet<>();

		Map<Account, Split> newmap = new HashMap<>();
		for ( Split s : newsplits ) {
			newmap.put( s.getAccount(), s );
		}

		for ( Split oldsplit : oldsplits ) {
			Account oldacct = oldsplit.getAccount();
			rc.remove( oldsplit.getId(), null, null );

			if ( newmap.containsKey( oldacct ) ) {
				Split newsplit = newmap.get( oldacct );
				URI newid = newsplit.getId();
				rc.remove( newid, null, null );

				newsplit = create( newsplit, newid, false );
				realsplits.add( newsplit );
				newmap.remove( oldacct );
			}
		}

		// anything left in this map is a new transaction to add
		for ( Split s : newmap.values() ) {
			Split newsplit = create( s, null, false );
			realsplits.add( newsplit );
		}

		return realsplits;
	}
}
