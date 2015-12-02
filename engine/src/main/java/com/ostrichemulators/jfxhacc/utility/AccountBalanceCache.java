/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper.BalanceType;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.mapper.TransactionListener;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Collection;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class AccountBalanceCache {

	public static final Logger log = Logger.getLogger( AccountBalanceCache.class );
	private final ObservableMap<Account, MoneyPair> balances
			= FXCollections.observableHashMap();
	private final AccountMapper amap;

	public AccountBalanceCache( AccountMapper a, TransactionMapper tmap ) {
		amap = a;
		tmap.addMapperListener( new TransactionListener() {

			@Override
			public void added( Transaction t ) {
				AccountBalanceCache.this.added( t );
			}

			@Override
			public void updated( Transaction t ) {
				AccountBalanceCache.this.updated( t );
			}

			@Override
			public void removed( URI uri ) {
				AccountBalanceCache.this.removed( uri );
			}

			@Override
			public void reconciled( Account acct, Collection<Split> splits ) {
				recache( acct );
			}
		} );

		amap.addMapperListener( new MapperListener<Account>() {

			@Override
			public void added( Account t ) {
				recache( t );
			}

			@Override
			public void updated( Account upd ) {
				Money ubal = upd.getOpeningBalance();
				for ( Account stored : balances.keySet() ) {
					if ( stored.equals( upd ) && !stored.getOpeningBalance().equals( ubal ) ) {
						recache( upd );
					}
				}
			}

			@Override
			public void removed( URI uri ) {
				recache();
			}
		} );

		try {
			for ( Account acct : amap.getAll() ) {
				recache( acct );
			}
		}
		catch ( MapperException e ) {
			log.error( e, e );
		}

	}

	public ObservableMap<Account, MoneyPair> getMap() {
		return balances;
	}

	private void added( Transaction t ) {
		for ( Split s : t.getSplits() ) {
			recache( s.getAccount() );
		}
	}

	private void updated( Transaction t ) {
		for ( Split s : t.getSplits() ) {
			recache( s.getAccount() );
		}
	}

	private void removed( URI uri ) {
		for ( Account a : balances.keySet() ) {
			recache( a );
		}
	}

	public Money get( Account a, BalanceType type ) {
		MoneyPair mp = balances.get( a );
		switch ( type ) {
			case OPENING:
				return a.getOpeningBalance();
			case RECONCILED:
				return mp.reco.getValue();
			default:
				return mp.current.getValue();
		}
	}

	public Property<Money> getCurrentProperty( Account acct ) {
		return balances.get( acct ).current;
	}

	private void recache( Account a ) {
		if ( balances.containsKey( a ) ) {
			MoneyPair mp = balances.get( a );
			mp.current.setValue( amap.getBalance( a, BalanceType.CURRENT ) );
			mp.reco.setValue( amap.getBalance( a, BalanceType.RECONCILED ) );
		}
		else {
			Money c = amap.getBalance( a, BalanceType.CURRENT );
			Money r = amap.getBalance( a, BalanceType.RECONCILED );
			balances.put( a, new MoneyPair( c, r ) );
		}
	}

	private void recache() {
		for ( Account a : balances.keySet() ) {
			recache( a );
		}
	}

	public static class MoneyPair {

		public final Property<Money> current;
		public final Property<Money> reco;

		public MoneyPair( Money c, Money r ) {
			current = new SimpleObjectProperty<>( c );
			reco = new SimpleObjectProperty<>( r );
		}
	}
}
