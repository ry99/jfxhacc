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
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import com.ostrichemulators.jfxhacc.utility.UriUtil;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
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
		super( repoc, JfxHacc.TRANSACTION_TYPE );
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

	@Override
	public Split create( Money m, String memo, ReconcileState rs ) {
		Split s = new SplitImpl( m );
		s.setMemo( memo );
		s.setReconciled( rs );
		return s;
	}

	private Split create( Account a, Split s, URI id, boolean dotrans ) throws RepositoryException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();

		if ( dotrans ) {
			rc.begin();
		}

		if ( null == id ) {
			id = UriUtil.randomUri( JfxHacc.SPLIT_TYPE );
		}
		s.setId( id );
		rc.add( id, RDF.TYPE, JfxHacc.SPLIT_TYPE );
		rc.add( id, Splits.ACCOUNT_PRED, a.getId() );
		rc.add( id, Splits.MEMO_PRED, vf.createLiteral( s.getMemo() ) );

		Money m = s.getValue();
		if ( s.isDebit() ) {
			m = m.opposite();
		}

		rc.add( id, Splits.VALUE_PRED, vf.createLiteral( m.value() ) );
		rc.add( id, Splits.RECO_PRED, vf.createLiteral( s.getReconciled().toString() ) );

		if ( dotrans ) {
			rc.commit();
		}

		return s;
	}

	@Override
	public Transaction create( Date d, Payee p, String number, Map<Account, Split> splits,
			Journal journal ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();

			Map<Account, Split> realsplits = new HashMap<>();
			List<URI> splitids = new ArrayList<>();
			for ( Map.Entry<Account, Split> en : splits.entrySet() ) {
				Account a = en.getKey();
				Split s = create( a, en.getValue(), null, false );
				splitids.add( s.getId() );
				realsplits.put( a, s );
			}

			URI id = createBaseEntity();

			rc.add( id, Transactions.PAYEE_PRED, p.getId() );
			rc.add( id, Transactions.DATE_PRED, vf.createLiteral( d ) );
			if ( null != number ) {
				rc.add( id, Transactions.NUMBER_PRED, vf.createLiteral( number ) );
			}

			rc.add( id, Transactions.JOURNAL_PRED, journal.getId() );
			for ( URI splitid : splitids ) {
				rc.add( id, Transactions.SPLIT_PRED, splitid );
			}
			rc.commit();

			TransactionImpl trans = new TransactionImpl( id, d, number, p );
			trans.setSplits( realsplits );
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

			Map<Account, Split> realsplits = updateSplits( getSplitMap( t.getId() ),
					new HashMap<>( t.getSplits() ), rc );

			URI id = t.getId();
			rc.remove( id, Transactions.PAYEE_PRED, null );
			rc.remove( id, Transactions.DATE_PRED, null );
			rc.remove( id, Transactions.NUMBER_PRED, null );

			rc.add( id, Transactions.PAYEE_PRED, t.getPayee().getId() );
			rc.add( id, Transactions.DATE_PRED, vf.createLiteral( t.getDate() ) );
			if ( null != t.getNumber() ) {
				rc.add( id, Transactions.NUMBER_PRED, vf.createLiteral( t.getNumber() ) );
			}

			rc.remove( id, Transactions.SPLIT_PRED, null );
			for ( Split split : realsplits.values() ) {
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

			return get( URI.class.cast( stmts.get( 0 ).getSubject() ) );
		}
		catch ( RepositoryException re ) {
			throw new MapperException( re );
		}
	}

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

	protected Map<Account, Split> getSplitMap( URI transid ) throws MapperException {
		Map<URI, Account> accounts = new HashMap<>();
		Map<Split, URI> splitos = getSplits( transid );
		Map<Account, Split> splits = new HashMap<>();

		for ( Map.Entry<Split, URI> en : splitos.entrySet() ) {
			URI acctid = en.getValue();
			if ( !accounts.containsKey( acctid ) ) {
				accounts.put( acctid, amap.get( acctid ) );
			}
			splits.put( accounts.get( acctid ), en.getKey() );
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

		List<Transaction> transactions = query( "SELECT ?t ?p ?o WHERE {"
				+ "  ?t trans:entry ?s."
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

				t.addSplit( accounts.get( en.getValue() ), en.getKey() );
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

	private Map<Account, Split> updateSplits( Map<Account, Split> oldsplits,
			Map<Account, Split> newsplits, RepositoryConnection rc ) throws RepositoryException {
		// if a split is in both maps, update it
		// if it's in the old but not the new, remove it
		// if it's in the new but not the old, add it

		Map<Account, Split> realsplits = new HashMap<>();

		for ( Map.Entry<Account, Split> en : oldsplits.entrySet() ) {
			Split oldsplit = en.getValue();
			Account oldacct = en.getKey();
			if ( newsplits.containsKey( oldacct ) ) {
				Split newsplit = newsplits.get( oldacct );
				newsplit = create( oldacct, newsplit, newsplit.getId(), false );
				realsplits.put( oldacct, newsplit );
				newsplits.remove( oldacct );
			}
			else {
				rc.remove( oldsplit.getId(), null, null );
			}
		}

		// anything left in this map is a new transaction to add
		for ( Map.Entry<Account, Split> en : newsplits.entrySet() ) {
			Account acct = en.getKey();
			Split newsplit = create( acct, en.getValue(), null, false );
			realsplits.put( acct, newsplit );
		}

		return realsplits;
	}
}
