/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.SplitBase.ReconcileState;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Date;
import java.util.function.Predicate;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public interface PredicateFactory {

	public Predicate<SplitStub> id( URI uri );

	public Predicate<SplitStub> account( Account p );

	public Predicate<SplitStub> transaction( Transaction p );

	public Predicate<SplitStub> journal( Journal p );

	public Predicate<SplitStub> payee( Payee a );

	public Predicate<SplitStub> state( ReconcileState... a );

	public Predicate<SplitStub> between( Date from, Date to );

	public Predicate<SplitStub> filter( URI type, URI id );

	public Predicate<SplitStub> credits();

	public Predicate<SplitStub> debits();

	public Predicate<SplitStub> increases( Account a );

	public Predicate<SplitStub> decreases( Account a );
}
