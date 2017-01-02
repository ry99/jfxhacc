/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.MainApp;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.SplitBase.ReconcileState;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.vocabulary.Payees;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class AccountManager extends AbstractDataManager<Account> {

	private static final Logger log = Logger.getLogger( AccountManager.class );
	private final SplitStubManager stubman;
	private final ObservableMap<Account, Account> childparentlkp
			= FXCollections.observableHashMap();
	private final Map<Account, ObjectProperty<Money>> currbals = new HashMap<>();
	private final Map<Account, ObjectProperty<Money>> recbals = new HashMap<>();

	public AccountManager( DataEngine de, SplitStubManager stubman ) {
		super( de.getAccountMapper() );
		this.stubman = stubman;
		AccountMapper amap = de.getAccountMapper();
		try {
			// need to convert the mapper's map to use *our* list of accounts,
			// so listeners get updated on account changes
			Map<URI, Account> mymap = getMap();
			for ( Map.Entry<Account, Account> en : amap.getParentMap().entrySet() ) {
				URI k = en.getKey().getId();
				URI v = ( null == en.getValue() ? null : en.getValue().getId() );
				childparentlkp.put( mymap.get( k ), mymap.get( v ) );
			}

			amap.addMapperListener( new MapperListener<Account>() {
				@Override
				public void added( Account t ) {
					try {
						childparentlkp.put( t, mymap.get( amap.getParent( t ).getId() ) );
					}
					catch ( MapperException me ) {
						log.error( "could not fetch parent for account: " + t );
					}
				}

				@Override
				public void updated( Account t ) {
				}

				@Override
				public void removed( URI uri ) {
					childparentlkp.clear();
					try {
						for ( Map.Entry<Account, Account> en : amap.getParentMap().entrySet() ) {
							URI k = en.getKey().getId();
							URI v = ( null == en.getValue() ? null : en.getValue().getId() );
							childparentlkp.put( mymap.get( k ), mymap.get( v ) );
						}
					}
					catch ( MapperException me ) {
						log.error( "could not fetch parent relationships", me );
					}
				}
			} );
		}
		catch ( MapperException me ) {
			throw new RuntimeException( me );
		}
	}

	public AccountManager( DataEngine de ) throws MapperException {
		this( de, new SplitStubManager( de ) );
	}

	public ObjectProperty<Money> getCurrentProperty( Account acct ) {
		if ( !currbals.containsKey( acct ) ) {
			currbals.put( acct, getBalanceProperty( acct, stubman.getSplitStubs( MainApp.PF.account( acct ) ) ) );
		}
		return currbals.get( acct );
	}

	public ObjectProperty<Money> getRecoProperty( Account acct ) {
		if ( !recbals.containsKey( acct ) ) {
			recbals.put( acct, getBalanceProperty( acct, stubman.getSplitStubs( MainApp.PF.account( acct ),
					MainApp.PF.state( ReconcileState.RECONCILED ) ) ) );
		}
		return recbals.get( acct );
	}

	private ObjectProperty<Money> getBalanceProperty( Account acct,
			ObservableList<SplitStub> trans ) {
		Property<Money> open = acct.getOpeningBalanceProperty();

		ObjectProperty<Money> bal = new SimpleObjectProperty<>();
		bal.bind( Bindings.createObjectBinding( sumlist( acct, trans ), open, trans ) );
		return bal;
	}

	public Account getParent( Account acct ) {
		return childparentlkp.get( acct );
	}

	public ObservableMap<Account, Account> getParentMap() {
		return FXCollections.unmodifiableObservableMap( childparentlkp );
	}

	public List<Account> getParents( Account acct ) {
		List<Account> parents = new ArrayList<>();
		Account par = getParent( acct );
		if ( null != par ) {
			parents.addAll( getParents( par ) );
			parents.add( par );
		}

		return parents;
	}

	/**
	 * Gets the most popular accounts (by number of transactions) that are not
	 * {@link AccountType#EXPENSE} accounts
	 *
	 * @param maxsize
	 * @return
	 */
	public List<Account> getPopularAccounts( int maxsize ) {
		// ignore expenses,
		// but figure out how many transactions are present for each account
		Map<Account, Integer> countables = new HashMap<>();
		ObservableList<SplitStub> stubs = stubman.getAll();
		for ( Account a : getAll() ) {
			if ( AccountType.EXPENSE != a.getAccountType() ) {
				List<SplitStub> list = stubs.filtered( MainApp.PF.account( a ) );
				countables.put( a, list.size() );
			}
		}

		// sort the entries by size
		List<Map.Entry<Account, Integer>> entrylist = new ArrayList<>( countables.entrySet() );
		entrylist.sort( new Comparator<Map.Entry<Account, Integer>>() {
			@Override
			public int compare( Map.Entry<Account, Integer> o1, Map.Entry<Account, Integer> o2 ) {
				return o2.getValue() - o1.getValue();
			}
		} );

		// return the top X accounts
		List<Account> returner = new ArrayList<>();
		int max = Math.min( maxsize, entrylist.size() );
		for ( int i = 0; i < max; i++ ) {
			returner.add( entrylist.get( i ).getKey() );
		}

		return returner;
	}

	private Callable<Money> sumlist( Account a, Collection<SplitStub> splits ) {
		return new Callable<Money>() {
			@Override
			public Money call() throws Exception {
				Money open = a.getOpeningBalance();
				AccountType at = a.getAccountType();
				Money sum = at.sum( splits );

//				if ( "Serve Card".equals( a.getName() ) ) {
//					for ( SplitStub ss : splits ) {
//						log.debug( at.isPositive( ss ) + "  " + ss );
//					}
//				}
				Money ret = open.plus( sum );
				log.debug( a + "=> " + sum + "..." + ret );
				return ret;
			}
		};
	}

	@Override
	protected void update( Account inlist, Account newvals ) {
		inlist.setOpeningBalance( newvals.getOpeningBalance() );
		inlist.setName( newvals.getName() );
		inlist.setNotes( newvals.getNotes() );
		inlist.setNumber( newvals.getNumber() );
	}

	/**
	 * Gets the most popular accounts for this payeem in order from most to least
	 * popular
	 *
	 * @param payee the payee
	 * @return
	 */
	public List<Account> getPopularAccounts( Payee payee ) {
		List<SplitStub> allforpayee
				= stubman.getAll().filtered( MainApp.PF.filter( Payees.TYPE, payee.getId() ) );

		Map<URI, Account> map = getMap();

		Map<Account, Integer> counts = new HashMap<>();
		for ( SplitStub ss : allforpayee ) {
			int cnt = counts.getOrDefault( map.get( ss.getAccountId() ), 0 );
			counts.put( map.get( ss.getAccountId() ), cnt + 1 );
		}

		List<Account> ret = new ArrayList<>( counts.keySet() );
		Collections.sort( ret, new Comparator<Account>() {
			@Override
			public int compare( Account o1, Account o2 ) {
				return counts.getOrDefault( o2, Integer.MIN_VALUE )
						- counts.getOrDefault( o2, Integer.MIN_VALUE );
			}
		} );

		for ( Account u : ret ) {
			log.debug( "payee popular accts: " + u.getName() );
		}

		return ret;
	}
}
