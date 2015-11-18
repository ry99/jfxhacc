/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoCellFactory;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.input.KeyEvent;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class ReconcileViewController extends TransactionViewController {

	private static final Logger log = Logger.getLogger( ReconcileViewController.class );
	private static final String PREF_SPLITTER = "recviewer.splitter.location";
	private static final String PREF_SORTCOL = "recviewer.sort.col";
	private static final String PREF_SORTASC = "recviewer.sort.asc";
	private Date date;

	@Override
	protected List<Transaction> getTransactions() {
		try {
			return tmap.getUnreconciled( account, journal, date );
		}
		catch ( MapperException ioe ) {
			log.error( ioe, ioe );
		}

		return new ArrayList<>();
	}

	public void setDate( Date d ) {
		date = d;
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
	public void shutdown() {
		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		ObservableList<TableColumn<Transaction, ?>> cols = transtable.getColumns();
		int i = 0;

		TableColumn<Transaction, ?> sortcol = null;
		ObservableList<TableColumn<Transaction, ?>> sorts = transtable.getSortOrder();
		if ( !sorts.isEmpty() ) {
			sortcol = sorts.get( 0 );
		}

		for ( TableColumn<Transaction, ?> tc : cols ) {
			prefs.putDouble( "recviewer.col" + ( i++ ), tc.getWidth() );

			if ( tc.equals( sortcol ) ) {
				prefs.putInt( PREF_SORTCOL, i );

				SortType stype = tc.getSortType();
				prefs.putBoolean( PREF_SORTASC,
						( null == stype ? true : stype == SortType.ASCENDING ) );
			}
		}

		prefs.putDouble( PREF_SPLITTER, splitterpos );
	}

	@Override
	protected void mouseClick( Transaction t ) {
		toggle( t );
	}

	private void toggle( Transaction t ) {
		if ( null == t ) {
			t = transtable.getSelectionModel().getSelectedItem();
		}

		Split s = t.getSplits().get( account );
		ReconcileState newrec = ( ReconcileState.CLEARED == s.getReconciled()
				? ReconcileState.NOT_RECONCILED
				: ReconcileState.CLEARED );

		try {
			tmap.reconcile( newrec, account, s );
			updated( t );
		}
		catch ( MapperException me ) {
			log.error( me, me );
			// FIXME: tell the user
		}

		int idx = transtable.getSelectionModel().getSelectedIndex();
		if ( ReconcileState.CLEARED == newrec ) {
			ListIterator<Transaction> it = transactions.listIterator( idx );
			while ( it.hasNext() ) {
				// move forward to our first unreconciled transaction
				Transaction next = it.next();
				Split nextsplit = next.getSplits().get( account );
				if ( ReconcileState.NOT_RECONCILED == nextsplit.getReconciled() ) {
					idx = it.nextIndex() - 1;
					break;
				}
			}
		}
		if ( idx < transactions.size() ) {
			transtable.getSelectionModel().clearAndSelect( idx );
			transtable.getFocusModel().focus( idx );
		}
	}

	@FXML
	@Override
	public void keyTyped( KeyEvent ke ) {
		String code = ke.getCharacter();
		Transaction t = transtable.getSelectionModel().getSelectedItem();
		if ( "R".equalsIgnoreCase( code ) || " ".equals( code ) ) {
			ke.consume();
			toggle( t );
		}
		else {
			super.keyTyped( ke );
		}
	}

	@Override
	public void added( Transaction t ) {
		if ( t.getSplits().containsKey( account ) ) {
			transactions.add( t );
			transtable.sort();
		}
	}

	@Override
	public void updated( Transaction t ) {
		if ( t.getSplits().containsKey( account ) ) {
			ListIterator<Transaction> transit = transactions.listIterator();
			while ( transit.hasNext() ) {
				Transaction listt = transit.next();
				if ( listt.equals( t ) ) {
					transit.set( t );
					transtable.sort();
					break;
				}
			}
		}
	}

	public void upgradeSplits() {
		List<Split> splits = new ArrayList<>();
		for ( Transaction t : transactions ) {
			Split s = t.getSplits().get( account );
			if ( ReconcileState.CLEARED == s.getReconciled() ) {
				splits.add( s );
			}
		}

		try {
			tmap.reconcile( ReconcileState.RECONCILED, account,
					splits.toArray( new Split[0] ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}
}
