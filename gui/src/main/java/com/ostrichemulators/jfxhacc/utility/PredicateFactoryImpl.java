/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.Journals;
import com.ostrichemulators.jfxhacc.model.vocabulary.Payees;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class PredicateFactoryImpl implements PredicateFactory {

	private static final Logger log = Logger.getLogger( PredicateFactoryImpl.class );

	@Override
	public Predicate<SplitStub> account( Account p ) {
		URI acctid = p.getId();
		return new Predicate<SplitStub>() {

			@Override
			public boolean test( SplitStub t ) {
				return t.getAccountId().equals( acctid );
			}
		};
	}

	@Override
	public Predicate<SplitStub> transaction( Transaction p ) {
		URI acctid = p.getId();
		return new Predicate<SplitStub>() {

			@Override
			public boolean test( SplitStub t ) {
				return t.getTransactionId().equals( acctid );
			}
		};
	}

	@Override
	public Predicate<SplitStub> payee( Payee p ) {
		URI acctid = p.getId();
		return new Predicate<SplitStub>() {

			@Override
			public boolean test( SplitStub t ) {
				return t.getPayee().equals( acctid );
			}
		};
	}

	@Override
	public Predicate<SplitStub> journal( Journal p ) {
		URI acctid = p.getId();
		return new Predicate<SplitStub>() {

			@Override
			public boolean test( SplitStub t ) {
				return t.getJournalId().equals( acctid );
			}
		};
	}

	@Override
	public Predicate<SplitStub> state( Split.ReconcileState... a ) {
		Set<Split.ReconcileState> states = new HashSet<>( Arrays.asList( a ) );

		return new Predicate<SplitStub>() {
			@Override
			public boolean test( SplitStub t ) {
				return states.contains( t.getReconciled() );
			}
		};
	}

	@Override

	public Predicate<SplitStub> between( Date from, Date to ) {
		long realfrom = ( null == from ? Long.MIN_VALUE : from.getTime() );
		long realto = ( null == to ? Long.MAX_VALUE : to.getTime() );

		return new Predicate<SplitStub>() {
			@Override
			public boolean test( SplitStub t ) {
				if ( null == t.getDate() ) {
					log.warn( "I have a null date!" );
					return false;
				}

				long td = t.getDate().getTime();
				return ( td < realto && td >= realfrom );
			}
		};
	}

	@Override
	public Predicate<SplitStub> filter( URI type, URI id ) {
		if ( Accounts.TYPE == type ) {
			return new Predicate<SplitStub>() {

				@Override
				public boolean test( SplitStub t ) {
					return t.getAccountId().equals( id );
				}
			};
		}
		else if ( Journals.TYPE == type ) {
			return new Predicate<SplitStub>() {

				@Override
				public boolean test( SplitStub t ) {
					return t.getJournalId().equals( id );
				}
			};
		}
		else if ( Transactions.TYPE == type ) {
			return new Predicate<SplitStub>() {

				@Override
				public boolean test( SplitStub t ) {
					return t.getTransactionId().equals( id );
				}
			};
		}
		else if ( Payees.TYPE == type ) {
			return new Predicate<SplitStub>() {

				@Override
				public boolean test( SplitStub t ) {
					return t.getPayee().equals( id );
				}
			};
		}

		throw new IllegalArgumentException( "unknown type: " + type );
	}
}
