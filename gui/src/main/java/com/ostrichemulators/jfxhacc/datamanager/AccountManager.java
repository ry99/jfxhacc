/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.utility.PredicateFactory;
import com.ostrichemulators.jfxhacc.utility.PredicateFactoryImpl;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class AccountManager extends AbstractDataManager<Account> {

	private static final Logger log = Logger.getLogger( AccountManager.class );
	private final SplitStubManager stubman;
	private static final PredicateFactory PF = new PredicateFactoryImpl();
	private final Map<Account, ObjectProperty<Money>> currbals = new HashMap<>();
	private final Map<Account, ObjectProperty<Money>> recobals = new HashMap<>();

	public AccountManager( DataEngine de, SplitStubManager stubman ) {
		super( de.getAccountMapper() );
		this.stubman = stubman;

		for ( Account a : getAll() ) {
			init( a );
		}
	}

	public AccountManager( DataEngine de ) throws MapperException {
		this( de, new SplitStubManager( de ) );
	}

	@Override
	protected void add( Account item, ObservableList<Account> list ) {
		super.add( item, list );
		init( item );
	}

	private void init( Account a ) {
		log.debug( "initing " + a );
		final ObservableList<SplitStub> obsv
				= stubman.getSplitStubs().filtered( PF.account( a ) );
		final ObservableList<SplitStub> obsvrec
				= obsv.filtered( PF.state( Split.ReconcileState.RECONCILED ) );
		Property<Money> open = a.getOpeningBalanceProperty();

		ObjectProperty<Money> currbal = new SimpleObjectProperty<>();
		currbal.bind( Bindings.createObjectBinding( sumlist( a, obsv ), open, obsv ) );

		ObjectProperty<Money> recobal = new SimpleObjectProperty<>();
		recobal.bind( Bindings.createObjectBinding( sumlist( a, obsvrec ), open, obsvrec ) );

		currbals.put( a, currbal );
		recobals.put( a, recobal );
	}

	public Money get( Account a, AccountMapper.BalanceType type ) {
		switch ( type ) {
			case OPENING:
				return a.getOpeningBalance();
			case RECONCILED:
				return recobals.get( a ).getValue();
			default:
				return currbals.get( a ).getValue();
		}
	}

	public ObjectProperty<Money> getCurrentProperty( Account acct ) {
		return currbals.get( acct );
	}

	public ObjectProperty<Money> getRecoProperty( Account acct ) {
		return recobals.get( acct );
	}

	private Callable<Money> sumlist( Account a, Collection<SplitStub> splits ) {
		return new Callable<Money>() {
			@Override
			public Money call() throws Exception {
				return a.getOpeningBalance().plus( a.getAccountType().sum( splits ) );
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
}
