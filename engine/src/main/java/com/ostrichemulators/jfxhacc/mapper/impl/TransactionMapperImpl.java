/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.JournalMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitBase;
import com.ostrichemulators.jfxhacc.model.SplitBase.ReconcileState;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.model.impl.SplitStubImpl;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import com.ostrichemulators.jfxhacc.utility.UriUtil;
import info.aduna.iteration.Iterations;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import com.ostrichemulators.jfxhacc.mapper.SplitListener;

/**
 *
 * @author ryan
 */
public class TransactionMapperImpl extends RdfMapper<Transaction>
		implements TransactionMapper {

	private static final Logger log = Logger.getLogger( TransactionMapperImpl.class );
	private final PayeeMapper pmap;
	private final AccountMapper amap;
	private final JournalMapper jmap;
	private final List<SplitListener> listenees = new ArrayList<>();
	private final List<SplitStub> allstubs = new ArrayList<>();

	public TransactionMapperImpl( RepositoryConnection repoc, AccountMapper amap,
			PayeeMapper pmap, JournalMapper jmap ) {
		this( repoc, amap, pmap, jmap, Transactions.TYPE );
	}

	public TransactionMapperImpl( RepositoryConnection repoc, AccountMapper amap,
			PayeeMapper pmap, JournalMapper jmap, URI type ) {
		super( repoc, type );
		this.pmap = pmap;
		this.amap = amap;
		this.jmap = jmap;
	}

	@Override
	public void addSplitListener( SplitListener tl ) {
		listenees.add( tl );
	}

	@Override
	public void removeSplitListener( SplitListener tl ) {
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

		Money m = s.getRawValueProperty().getValue();
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
		if ( null != splits ) {
			transaction.setSplits( new HashSet<>( splits ) );
		}
		return create( transaction );
	}

	@Override
	public Transaction create( Transaction t ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		boolean active = false;
		try {
			active = rc.isActive();

			if ( !active ) {
				rc.begin();
			}

			Map<Split, URI> realsplits = new HashMap<>();
			for ( Split sp : t.getSplits() ) {
				Split s = create( sp, null, false );
				realsplits.put( s, s.getId() );
			}

			URI id = createBaseEntity();

			rc.add( id, Transactions.JOURNAL_PRED, t.getJournal().getId() );

			if ( null != t.getPayee() ) {
				rc.add( id, Transactions.PAYEE_PRED, t.getPayee().getId() );
			}
			if ( null != t.getDate() ) {
				rc.add( id, Transactions.DATE_PRED, vf.createLiteral( t.getDate() ) );
			}
			if ( null != t.getNumber() ) {
				rc.add( id, Transactions.NUMBER_PRED, vf.createLiteral( t.getNumber() ) );
			}

			for ( URI splitid : realsplits.values() ) {
				rc.add( id, Transactions.SPLIT_PRED, splitid );
			}

			if ( !active ) {
				rc.commit();
			}

			TransactionImpl trans
					= new TransactionImpl( id, t.getDate(), t.getNumber(), t.getPayee() );
			trans.setSplits( realsplits.keySet() );
			trans.setJournal( t.getJournal() );

			for ( Split s : trans.getSplits() ) {
				allstubs.add( new SplitStubImpl( trans, s ) );
			}

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
				else if ( Transactions.JOURNAL_PRED.equals( uri ) ) {
					try {
						trans.setJournal( jmap.get( URI.class.cast( set.getValue( "o" ) ) ) );
					}
					catch ( MapperException me ) {
						log.error( me, me );
					}
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

			Map<Split, SplitOp> realsplits = updateSplits( getSplitSet( t.getId() ),
					new HashSet<>( t.getSplits() ), rc );

			URI id = t.getId();
			rc.remove( id, Transactions.PAYEE_PRED, null );
			rc.remove( id, Transactions.DATE_PRED, null );
			rc.remove( id, Transactions.NUMBER_PRED, null );
			rc.remove( id, Transactions.SPLIT_PRED, null );
			rc.remove( id, Transactions.JOURNAL_PRED, null );

			rc.add( id, Transactions.JOURNAL_PRED, t.getJournal().getId() );

			if ( null != t.getPayee() ) {
				rc.add( id, Transactions.PAYEE_PRED, t.getPayee().getId() );
			}
			if ( null != t.getDate() ) {
				rc.add( id, Transactions.DATE_PRED, vf.createLiteral( t.getDate() ) );
			}
			if ( null != t.getNumber() ) {
				rc.add( id, Transactions.NUMBER_PRED, vf.createLiteral( t.getNumber() ) );
			}

			Set<Split> newsplits = new HashSet<>();
			for ( Map.Entry<Split, SplitOp> en : realsplits.entrySet() ) {
				if ( SplitOp.REMOVED != en.getValue() ) {
					rc.add( id, Transactions.SPLIT_PRED, en.getKey().getId() );
					newsplits.add( en.getKey() );
				}
			}
			rc.commit();

			t.setSplits( newsplits );

			// now update our stub cache
			for ( Map.Entry<Split, SplitOp> en : realsplits.entrySet() ) {
				Split s = en.getKey();
				SplitStub ss = new SplitStubImpl( t, s );

				//log.debug( "splitop: " + s + "  " + en.getValue() );
				//log.debug( "  ss: " + ss );

				allstubs.remove( ss );
				if ( SplitOp.REMOVED != en.getValue() ) {
					allstubs.add( ss );
				}
			}

			notifyUpdated( t );

			for ( SplitListener sl : listenees ) {
				sl.updated( t, realsplits );
			}
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
			trans.setSplits( getSplitSet( trans.getId() ) );
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
				+ "  ?s splits:value ?val ."
				+ "  OPTIONAL { ?s splits:memo ?memo } ."
				+ "  OPTIONAL { ?s splits:reconciled ?reco } ."
				+ "} ORDER BY ?s", bindmap( "t", transid ), new QueryHandler<Map<Split, URI>>() {
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

	protected Set<Split> getSplitSet( URI transid ) throws MapperException {
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
	public void reconcile( ReconcileState rs, SplitBase... splits ) throws MapperException {
		RepositoryConnection rc = getConnection();
		try {
			rc.begin();
			for ( SplitBase s : splits ) {
				rc.remove( s.getId(), Splits.RECO_PRED, null );
				rc.add( s.getId(), Splits.RECO_PRED, new LiteralImpl( rs.toString() ) );
				s.setReconciled( rs );
			}
			rc.commit();

			List<SplitBase> list = Arrays.asList( splits );
			for ( SplitListener tl : listenees ) {
				tl.reconciled( list );
			}
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Map<LocalDate, List<Split>> getSplits( Account acct, Date since,
			Date until ) throws MapperException {
		if ( null == since ) {
			since = new Date( Long.MIN_VALUE );
		}
		if ( null == until ) {
			until = new Date( Long.MAX_VALUE );
		}

		Map<String, Value> binds = this.bindmap( "aid", acct.getId() );
		ValueFactory vf = getConnection().getValueFactory();
		binds.put( "since", vf.createLiteral( since ) );
		binds.put( "until", vf.createLiteral( until ) );

		String sparql = "SELECT ?s ?memo ?reco ?val ?aid ?date WHERE {"
				+ "  ?s splits:account ?aid ."
				+ "  ?s splits:value ?val ."
				+ "  ?t trans:entry ?s . FILTER NOT EXISTS { ?t jfxhacc:recurrence ?z } ."
				+ "  ?t dcterms:created ?date . "
				+ "  FILTER( ?date >= ?since && ?date < ?until ) ."
				+ "  OPTIONAL { ?s splits:memo ?memo } ."
				+ "  OPTIONAL { ?s splits:reconciled ?reco } ."
				+ "} ORDER BY ?date";

		return query( sparql, binds, new QueryHandler<Map<LocalDate, List<Split>>>() {

			private final Map<LocalDate, List<Split>> data = new LinkedHashMap<>();

			@Override
			public void handleTuple( BindingSet set, ValueFactory vf ) {
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
				split.setAccount( acct );

				final Literal literal = Literal.class.cast( set.getValue( "date" ) );
				Instant instant = Instant.ofEpochMilli( DbUtil.toDate( literal ).getTime() );
				LocalDate date = LocalDateTime.ofInstant( instant,
						ZoneId.systemDefault() ).toLocalDate();

				if ( !data.containsKey( date ) ) {
					data.put( date, new ArrayList<>() );
				}

				data.get( date ).add( split );
			}

			@Override
			public Map<LocalDate, List<Split>> getResult() {
				return data;
			}
		} );
	}

	@Override
	public List<Transaction> getAll( Account acct ) throws MapperException {
		return getAll( acct, null, null );
	}

	@Override
	public List<Transaction> getAll( Account acct, Date from, Date to ) throws MapperException {
		if ( null == from ) {
			from = new Date( Long.MIN_VALUE );
		}
		if ( null == to ) {
			to = new Date( Long.MAX_VALUE );
		}

		ValueFactory vf = new ValueFactoryImpl();
		Map<String, Value> bindings = bindmap( "acct", acct.getId() );
		bindings.put( "since", vf.createLiteral( from ) );
		bindings.put( "until", vf.createLiteral( to ) );

		// don't include recurring transactions
		List<Transaction> transactions = query( "SELECT ?t ?p ?o WHERE {"
				+ "  ?t trans:entry ?s . FILTER NOT EXISTS { ?t jfxhacc:recurrence ?z } ."
				+ "  ?t a jfxhacc:transaction ."
				+ "  ?s splits:account ?acct ."
				+ "  ?t trans:journal ?jnl ."
				+ "  ?t dcterms:created ?date . "
				+ "  FILTER( ?date >= ?since && ?date < ?until ) ."
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
				else if ( Transactions.JOURNAL_PRED.equals( uri ) ) {
					try {
						last.setJournal( jmap.get( URI.class.cast( set.getValue( "o" ) ) ) );
					}
					catch ( MapperException me ) {
						log.error( me, me );
					}
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
	public List<SplitStub> getSplitStubs() throws MapperException {
		if ( allstubs.isEmpty() ) {
			allstubs.addAll( fetchSplitStubs() );
		}
		return allstubs;
	}

	private List<SplitStub> fetchSplitStubs() throws MapperException {
		Date start = new Date();
		List<SplitStub> transactions = query( "SELECT ?j ?t ?s ?a ?memo ?reco ?val ?payee ?date ?num WHERE {"
				+ "  ?t trans:entry ?s ."
				+ "  ?t trans:journal ?j ."
				+ "  ?s splits:account ?a ."
				//				+ "  ?t a jfxhacc:transaction ."
				//				+ "  ?s a jfxhacc:split ."
				//				+ "  ?j a jfxhacc:journal ."
				//				+ "  ?a a jfxhacc:account ."
				+ "  ?s splits:value ?val ."
				+ "  ?s splits:reconciled ?reco ."
				+ "  ?t trans:payee ?payee ."
				+ "  OPTIONAL { ?s splits:memo ?memo } ."
				+ "  OPTIONAL { ?t trans:number ?num } ."
				+ "  OPTIONAL { ?t dcterms:created ?date } ."
				+ "} ORDER BY ?date", new HashMap<>(),
				new QueryHandler<List<SplitStub>>() {
			List<SplitStub> tlist = new ArrayList<>();

			@Override
			public void handleTuple( BindingSet set, ValueFactory vf ) {
				URI tid = URI.class.cast( set.getValue( "t" ) );
				URI sid = URI.class.cast( set.getValue( "s" ) );
				URI aid = URI.class.cast( set.getValue( "a" ) );
				URI jid = URI.class.cast( set.getValue( "j" ) );
				Literal l = Literal.class.cast( set.getValue( "val" ) );

				String memo = ( set.hasBinding( "memo" )
						? set.getValue( "memo" ).stringValue()
						: null );

				String num = ( set.hasBinding( "num" )
						? set.getValue( "num" ).stringValue()
						: null );

				URI payee = URI.class.cast( set.getValue( "payee" ) );

				Date date = ( set.hasBinding( "date" )
						? DbUtil.toDate( Literal.class.cast( set.getValue( "date" ) ) )
						: null );

				ReconcileState rs = ( set.hasBinding( "reco" )
						? ReconcileState.valueOf( set.getValue( "reco" ).stringValue() )
						: ReconcileState.NOT_RECONCILED );

				SplitStub ss = new SplitStubImpl( jid, tid, aid, sid, new Money( l.intValue() ),
						memo, payee, date, rs, num );

				tlist.add( ss );
			}

			@Override
			public List<SplitStub> getResult() {
				return tlist;
			}
		} );

		log.debug( transactions.size() + " splitstubs loaded in "
				+ ( new Date().getTime() - start.getTime() ) + "ms" );
		return transactions;
	}

	@Override
	public List<Transaction> getUnreconciled( Account acct, Date asof )
			throws MapperException {

		Map<String, Value> bindings = bindmap( "acct", acct.getId() );
		bindings.put( "asof", new ValueFactoryImpl().createLiteral( asof ) );
		bindings.put( "recostate", new LiteralImpl( ReconcileState.RECONCILED.toString() ) );

		List<Transaction> transactions = query( "SELECT ?t ?p ?o WHERE {"
				+ "  ?t trans:entry ?s . FILTER NOT EXISTS { ?t jfxhacc:recurrence ?z } ."
				+ "  ?s splits:account ?acct ."
				+ "  ?t trans:journal ?jnl ."
				+ "  ?s splits:reconciled ?reco FILTER( ?reco != ?recostate ) ."
				+ "  ?t dcterms:created ?date FILTER( ?date < ?asof ) ."
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
				else if ( Transactions.JOURNAL_PRED.equals( uri ) ) {
					try {
						last.setJournal( jmap.get( URI.class.cast( set.getValue( "o" ) ) ) );
					}
					catch ( MapperException me ) {
						log.error( me, me );
					}
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

	private Map<Split, SplitOp> updateSplits( Set<Split> oldsplits, Set<Split> newsplits,
			RepositoryConnection rc ) throws RepositoryException {
		// if a split is in both maps, update it
		// if it's in the old but not the new, remove it
		// if it's in the new but not the old, add it

		Map<Split, SplitOp> realsplits = new HashMap<>();

		Map<URI, Split> newmap = new HashMap<>();
		for ( Split s : newsplits ) {
			newmap.put( s.getAccount().getId(), s );
		}

		for ( Split oldsplit : oldsplits ) {
			// get rid of all the old splits from the database...
			Account oldacct = oldsplit.getAccount();
			rc.remove( oldsplit.getId(), null, null );
			realsplits.put( oldsplit, SplitOp.REMOVED );
			//log.debug( "upd old: " + oldsplit );

			if ( newmap.containsKey( oldacct.getId() ) ) {
				// ...if we have an old split in our new set,
				// then re-add it with its new values
				Split newsplit = newmap.get( oldacct.getId() );

				newsplit.setId( oldsplit.getId() );
				URI newid = newsplit.getId();
				rc.remove( newid, null, null );

				newsplit = create( newsplit, newid, false );
				//log.debug( "upd upd: " + newsplit );
				realsplits.remove( oldsplit );
				realsplits.put( newsplit, SplitOp.UPDATED );
				newmap.remove( oldacct.getId() );
			}
		}

		// anything left in the newmap is a new split to add
		for ( Split s : newmap.values() ) {
			Split newsplit = create( s, null, false );
			//log.debug( "upd new: " + newsplit );
			realsplits.put( newsplit, SplitOp.ADDED );
		}

		return realsplits;
	}

	@Override
	public Transaction get( Recurrence r ) throws MapperException {
		Value val = oneval( "SELECT ?id WHERE { ?id jfxhacc:recurrence ?rec }",
				bindmap( "rec", r.getId() ) );

		if ( null == val ) {
			return null;
		}

		Transaction t = get( URI.class.cast( val ) );
		t.setSplits( getSplitSet( t.getId() ) );
		return t;
	}
}
