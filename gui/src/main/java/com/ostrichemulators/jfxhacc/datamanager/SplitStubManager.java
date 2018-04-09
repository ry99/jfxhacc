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
import com.ostrichemulators.jfxhacc.mapper.SplitListener;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper.SplitOp;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitBase;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.model.impl.SplitStubImpl;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class SplitStubManager extends AbstractDataManager<SplitStub> {

	private final static Logger log = Logger.getLogger( SplitStubManager.class );

	public SplitStubManager( DataEngine de ) {
		super( new SplitStubMapper( de ), ( SplitStub param ) -> {
			return new Observable[]{
				param.getAccountIdProperty(),
				param.getDateProperty(),
				param.getJournalIdProperty(),
				param.getNumberProperty(),
				param.getPayeeProperty(),
				param.getValueProperty(),
				param.getReconciledProperty(),
				param.getMemoProperty(),
				param.getTransactionIdProperty()
			};
		} );
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

		return FXCollections.unmodifiableObservableList( alllist.filtered( filter ) );
	}

	@SafeVarargs
	public final ObservableList<SplitStub> getSplitStubs( Predicate<SplitStub>... filters ) {
		return getSplitStubs( Arrays.asList( filters ) );
	}

	public SplitStub getOtherStub( SplitStub s ) {
		List<SplitStub> stubs = getOtherStubs( s );
		return ( 1 == stubs.size() ? stubs.get( 0 ) : null );
	}

	public ObservableList<SplitStub> getOtherStubs( SplitStub s ) {
		return getAll().filtered(
				MainApp.PF.filter( Transactions.TYPE, s.getTransactionId() )
						.and( MainApp.PF.filter( Accounts.TYPE, s.getAccountId() ).negate() ) );
	}

	@Override
	protected void update( SplitStub inlist, SplitStub newvals ) {
		log.debug( "inlist: " + inlist );
		log.debug( "  newvals: " + newvals );

		if ( !newvals.getTransactionId().equals( inlist.getTransactionId() ) ) {
			inlist.setTransactionId( newvals.getTransactionId() );
		}

		if ( !newvals.getAccountId().equals( inlist.getAccountId() ) ) {
			inlist.setAccountId( newvals.getAccountId() );
		}

		if ( !newvals.getDate().equals( inlist.getDate() ) ) {
			inlist.setDate( newvals.getDate() );
		}

		if ( !newvals.getJournalId().equals( inlist.getJournalId() ) ) {
			inlist.setJournalId( newvals.getJournalId() );
		}

		if ( !Objects.equals( newvals.getMemo(), inlist.getMemo() ) ) {
			inlist.setMemo( newvals.getMemo() );
		}

		if ( !Objects.equals( newvals.getNumber(), inlist.getNumber() ) ) {
			inlist.setNumber( newvals.getNumber() );
		}

		if ( !newvals.getPayee().equals( inlist.getPayee() ) ) {
			inlist.setPayee( newvals.getPayee() );
		}

		if ( !newvals.getReconciled().equals( inlist.getReconciled() ) ) {
			inlist.setReconciled( newvals.getReconciled() );
		}

		if ( !newvals.getRawValueProperty().getValue().equals( inlist.getRawValueProperty().getValue() ) ) {
			if ( newvals.isCredit() ) {
				inlist.setCredit( newvals.getValue() );
			}
			else {
				inlist.setDebit( newvals.getValue() );
			}
		}
	}

	@Override
	protected void remove( URI id, ObservableList<SplitStub> list ) {
		// if id is a transaction id, remove all the splits associated with it
		ObservableList<SplitStub> tsplits = list.filtered( new Predicate<SplitStub>() {
			@Override
			public boolean test( SplitStub t ) {
				return t.getTransactionId().equals( id );
			}
		} );

		if ( tsplits.isEmpty() ) {
			// id must be a splitstub id, so just remove the one splitstub
			ListIterator<SplitStub> li = list.listIterator();
			while ( li.hasNext() ) {
				SplitStub t = li.next();
				if ( t.getId().equals( id ) ) {
					li.remove();
				}
			}
		}
		else {
			// remove all the tsplits
			list.removeAll( tsplits );
		}
	}

	public static Split toSplit( SplitStub stub, AccountManager aman ) {
		Account acct = aman.get( stub.getAccountId() );

		Split split = new SplitImpl( acct, stub.getRawValueProperty().getValue(),
				stub.getMemo(), stub.getReconciled() );
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

			tmap.addSplitListener( new SplitListener() {
				@Override
				public void reconciled( Collection<? extends SplitBase> splits ) {
				}

				@Override
				public void updated( Transaction t, Map<Split, TransactionMapper.SplitOp> updates ) {
					for ( Map.Entry<Split, SplitOp> en : updates.entrySet() ) {
						Split s = en.getKey();
						SplitStub ss = new SplitStubImpl( t, s );

						switch ( en.getValue() ) {
							case REMOVED:
								for ( MapperListener<SplitStub> ml : listenees ) {
									ml.removed( s.getId() );
								}
								break;
							case UPDATED:
								for ( MapperListener<SplitStub> ml : listenees ) {
									ml.updated( ss );
								}
								break;
							case ADDED:
								for ( MapperListener<SplitStub> ml : listenees ) {
									ml.added( ss );
								}
								break;
							default:
								throw new RuntimeException( "unknown SplitOp: " + en.getValue() );
						}
					}
				}
			} );

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
				}

				@Override
				public void removed( URI uri ) {
					for ( MapperListener ml : listenees ) {
						ml.removed( uri ); // remember: this is a transaction id, not a split id
					}
				}
			} );
		}

		public TransactionMapper getTmap() {
			return tmap;
		}

		@Override
		public Collection<SplitStub> getAll() throws MapperException {
			return tmap.getSplitStubs();
		}

		@Override
		public SplitStub get( URI id ) throws MapperException {
			throw new UnsupportedOperationException( "Not supported yet." );
		}

		@Override
		public void remove( SplitStub t ) throws MapperException {
			throw new UnsupportedOperationException( "Not supported yet." );
		}

		@Override
		public void remove( URI id ) throws MapperException {
			throw new UnsupportedOperationException( "Not supported yet." );
		}

		@Override
		public void update( SplitStub t ) throws MapperException {
			throw new UnsupportedOperationException( "Not supported yet." );
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
