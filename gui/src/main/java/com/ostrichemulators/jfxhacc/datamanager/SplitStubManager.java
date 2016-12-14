/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.MainApp;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.DataMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.model.impl.SplitStubImpl;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class SplitStubManager extends AbstractDataManager<SplitStub> {

	private final static Logger log = Logger.getLogger( SplitStubManager.class );

	public SplitStubManager( DataEngine de ) {
		super( new SplitStubMapper( de ) );
	}

	/**
	 * Gets the splitstubs for this account, plus all the related splitstubs
	 *
	 * @param a
	 * @return
	 */
	public ObservableList<SplitStub> getAllSplitStubs( Account a ) {

		ObservableList<SplitStub> returner = FXCollections.observableArrayList();

		ObservableList<SplitStub> alllist = getAll();
		ObservableList<SplitStub> acctstubs = alllist.filtered( MainApp.PF.account( a ) );

		ObservableSet<URI> transids = FXCollections.observableSet( new HashSet<>() );
		for ( SplitStub s : acctstubs ) {
			transids.add( s.getTransactionId() );
			
			returner.addAll( alllist.filtered( MainApp.PF.filter( Transactions.TYPE,
					s.getTransactionId() ) ) );
		}

		acctstubs.addListener( new ListChangeListener<SplitStub>() {
			// we have a new transaction that matched our account, so add the new
			// transaction to our set of transaction ids
			@Override
			public void onChanged( ListChangeListener.Change<? extends SplitStub> c ) {
				while ( c.next() ) {
					if ( c.wasAdded() ) {
						for ( SplitStub s : c.getAddedSubList() ) {
							transids.add( s.getTransactionId() );
						}
					}
					else {
						for ( SplitStub s : c.getRemoved() ) {
							transids.remove( s.getTransactionId() );
						}
					}
				}
			}
		} );

		transids.addListener( new SetChangeListener<URI>() {
			@Override
			public void onChanged( SetChangeListener.Change<? extends URI> change ) {
				URI u = change.getElementAdded();
				if ( null == u ) {
					returner.removeIf( MainApp.PF.filter( Transactions.TYPE, u ) );
				}
				else {
					List<SplitStub> newstubs = alllist.filtered( MainApp.PF.filter( Transactions.TYPE, u ) );
					returner.addAll( newstubs );
				}
			}
		} );

		log.debug( "max splits has " + returner.size() + " elements" );
		return FXCollections.unmodifiableObservableList( returner );
	}

	public ObservableList<SplitStub> getSplitStubs( Collection<Predicate<SplitStub>> filters ) {
		ObservableList<SplitStub> alllist = getAll();

		if ( filters.isEmpty() ) {
			return alllist;
		}

		Iterator<Predicate<SplitStub>> li = filters.iterator();
		Predicate<SplitStub> filter = li.next();
		while ( li.hasNext() ) {
			filter = filter.and( li.next() );
		}

		for ( Predicate<SplitStub> p : filters ) {
			log.debug( "filtering splitstub: " + p );
		}

		return FXCollections.unmodifiableObservableList( alllist.filtered( filter ) );
	}

	@SafeVarargs
	public final ObservableList<SplitStub> getSplitStubs( Predicate<SplitStub>... filters ) {
		return getSplitStubs( Arrays.asList( filters ) );
	}

	@Override
	protected void update( SplitStub inlist, SplitStub newvals ) {
		inlist.setTransactionId( newvals.getTransactionId() );
		inlist.setAccountId( newvals.getAccountId() );
		inlist.setDate( newvals.getDate() );
		inlist.setJournalId( newvals.getJournalId() );
		inlist.setMemo( newvals.getMemo() );
		inlist.setNumber( newvals.getNumber() );
		inlist.setPayee( newvals.getPayee() );
		inlist.setReconciled( newvals.getReconciled() );
		inlist.setValue( newvals.getValue() );
	}

	@Override
	protected void remove( URI id, ObservableList<SplitStub> list ) {
		ListIterator<SplitStub> li = list.listIterator();
		while ( li.hasNext() ) {
			SplitStub t = li.next();
			if ( t.getTransactionId().equals( id ) ) {
				li.remove();
			}
		}
	}

	public static Split toSplit( SplitStub stub, AccountManager aman ) {
		Account acct = aman.get( stub.getAccountId() );

		Split split = new SplitImpl( acct, stub.getValue(), stub.getMemo(),
				stub.getReconciled() );
		split.setId( stub.getId() );
		return split;
	}

	public static Transaction toTransaction( Collection<SplitStub> stubs,
			AccountManager aman, JournalManager jman, PayeeManager pman ) {

		Transaction trans = new TransactionImpl();
		for ( SplitStub s : stubs ) {
			trans.setId( s.getTransactionId() );
			trans.setDate( s.getDate() );
			trans.setPayee( pman.get( s.getPayee() ) );
			trans.setJournal( jman.get( s.getJournalId() ) );
			trans.setNumber( s.getNumber() );

			trans.addSplit( SplitStubManager.toSplit( s, aman ) );
		}

		return trans;
	}

	private static class SplitStubMapper implements DataMapper<SplitStub> {

		private final List<MapperListener<SplitStub>> listenees = new ArrayList<>();
		private final TransactionMapper tmap;

		public SplitStubMapper( DataEngine de ) {
			tmap = de.getTransactionMapper();
			tmap.addMapperListener( new MapperListener<Transaction>() {
				@Override
				public void added( Transaction t ) {
					for ( Split s : t.getSplits() ) {
						SplitStub ss = new SplitStubImpl( t, s );
						for ( MapperListener<SplitStub> ml : listenees ) {
							ml.added( ss );
						}
					}
				}

				@Override
				public void updated( Transaction t ) {
					for ( Split s : t.getSplits() ) {
						SplitStub ss = new SplitStubImpl( t, s );
						for ( MapperListener<SplitStub> ml : listenees ) {
							ml.updated( ss );
						}
					}
				}

				@Override
				public void removed( URI uri ) {
					for ( MapperListener ml : listenees ) {
						ml.removed( uri );
					}
				}
			} );
		}

		@Override
		public Collection<SplitStub> getAll() throws MapperException {
			return tmap.getSplitStubs();
		}

		@Override
		public SplitStub get( URI id ) throws MapperException {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void remove( SplitStub t ) throws MapperException {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void remove( URI id ) throws MapperException {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void update( SplitStub t ) throws MapperException {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void release() {
		}

		@Override
		public void addMapperListener( MapperListener<SplitStub> l ) {
			listenees.add( l );
		}

		@Override
		public void removeMapperListener( MapperListener<SplitStub> l ) {
			listenees.remove( l );
		}
	}
}
