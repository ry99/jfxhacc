/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.MainApp;
import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoCellFactory;
import com.ostrichemulators.jfxhacc.datamanager.SplitStubManager;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.SplitBase.ReconcileState;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class ReconcileViewController extends TransactionViewController {

	private static final Logger log = Logger.getLogger( ReconcileViewController.class );
	private Date date;
	private final ObjectProperty<Money> reco = new SimpleObjectProperty<>( new Money() );
	private final ObservableList<SplitStub> credits = FXCollections.observableArrayList();
	private final ObservableList<SplitStub> debits = FXCollections.observableArrayList();
	private final InvalidationListener listener;

	public ReconcileViewController( TransactionMapper tm, SplitStubManager stubs ) {
		super( tm, stubs );
		listener = new InvalidationListener() {
			@Override
			public void invalidated( Observable observable ) {
				credits.setAll( transtable.getItems().filtered( MainApp.PF.credits() ) );
				debits.setAll( transtable.getItems().filtered( MainApp.PF.debits() ) );
			}
		};
	}

	@Override
	public void refresh() {
		super.refresh();
		transtable.getItems().addListener( listener );
		credits.setAll( transtable.getItems().filtered( MainApp.PF.credits() ) );
		debits.setAll( transtable.getItems().filtered( MainApp.PF.debits() ) );
	}

	@Override
	protected Collection<Predicate<SplitStub>> getFilters() {
		List<Predicate<SplitStub>> filters = new ArrayList<>( super.getFilters() );
		filters.add( MainApp.PF.state( ReconcileState.CLEARED, ReconcileState.NOT_RECONCILED ) );
		filters.add( MainApp.PF.between( null, date ) );
		return filters;
	}

	public void setAccount( Account acct, Date d ) {
		reco.setValue( new Money() );
		date = d;
		super.setAccount( acct );
	}

	public Date getDate() {
		return date;
	}

	public ReadOnlyObjectProperty<Money> getClearedValueProperty() {
		return reco;
	}

	public ObservableList<SplitStub> getClearedCredits() {
		return credits.filtered( MainApp.PF.state( ReconcileState.CLEARED ) );
	}

	public ObservableList<SplitStub> getClearedDebits() {
		return debits.filtered( MainApp.PF.state( ReconcileState.CLEARED ) );
	}

	@Override
	protected String getPrefPrefix() {
		return "recviewer";
	}

	@Override
	protected double getRowHeight() {
		return super.getRowHeight() / 2;
	}

	@Override
	protected PayeeAccountMemoCellFactory getPayeeAccountMemoCellFactory() {
		return new PayeeAccountMemoCellFactory( true );
	}

	@Override
	protected ReconcileState getDefaultReconcileState() {
		return ReconcileState.CLEARED;
	}

	@Override
	protected void mouseClick( SplitStub t ) {
		if ( null != t ) {
			toggle( t );
		}
	}

	private void toggle( SplitStub s ) {
		ReconcileState newrec = ( ReconcileState.CLEARED == s.getReconciled()
				? ReconcileState.NOT_RECONCILED
				: ReconcileState.CLEARED );

		s.setReconciled( newrec );

		int idx = transtable.getSelectionModel().getSelectedIndex();
		if ( ReconcileState.CLEARED == newrec ) {
			ListIterator<SplitStub> it = getData().listIterator( idx );
			while ( it.hasNext() ) {
				// move forward to our first unreconciled transaction
				SplitStub nextsplit = it.next();
				if ( ReconcileState.NOT_RECONCILED == nextsplit.getReconciled() ) {
					idx = it.nextIndex() - 1;
					break;
				}
			}
		}

		if ( idx < getData().size() ) {
			transtable.getSelectionModel().clearAndSelect( idx );
			transtable.getFocusModel().focus( idx );

			// scroll, but not all the way...give a couple rows buffer
			if ( idx - 5 > 0 ) {
				transtable.scrollTo( idx - 5 );
			}
		}

		reco.set( account.getAccountType().sum( getData().filtered(
				MainApp.PF.state( ReconcileState.CLEARED ) ) ) );
	}

	@FXML
	@Override
	public void keyTyped( KeyEvent ke ) {
		String code = ke.getCharacter();
		SplitStub t = transtable.getSelectionModel().getSelectedItem();
		if ( "R".equalsIgnoreCase( code ) || " ".equals( code ) ) {
			ke.consume();
			toggle( t );
		}
		else {
			super.keyTyped( ke );
		}
	}

	public void upgradeSplits() {
		List<SplitStub> splits = getData().filtered( MainApp.PF.state( ReconcileState.CLEARED ) );

		try {
			tmap.reconcile( ReconcileState.RECONCILED, splits.toArray( new SplitStub[0] ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}
}
