/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoCellFactory;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class ReconcileViewController extends TransactionViewController {

	private static final Logger log = Logger.getLogger( ReconcileViewController.class );
	private Date date;
	private final ObjectProperty<Money> reco = new SimpleObjectProperty<>( new Money() );

	@Override
	protected List<Transaction> getTransactions() {
		try {
			List<Transaction> list = tmap.getUnreconciled( account, journal, date );
			return list;
		}
		catch ( MapperException ioe ) {
			log.error( ioe, ioe );
		}

		return new ArrayList<>();
	}

	public void setAccount( Account acct, Journal j, Date d ) {
		date = d;
		super.setAccount( acct, j );
	}

	public Date getDate() {
		return date;
	}

	public ReadOnlyObjectProperty<Money> getClearedValueProperty() {
		return reco;
	}

	public Collection<Split> getSplits() {
		List<Split> splits = new ArrayList<>();
		for ( Transaction t : transactions ) {
			splits.add( t.getSplit( account ) );
		}
		return splits;
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
	protected void mouseClick( Transaction t ) {
		toggle( t );
	}

	private void toggle( Transaction t ) {
		if ( null == t ) {
			t = transtable.getSelectionModel().getSelectedItem();
		}

		Split s = t.getSplit( account );
		ReconcileState newrec = ( ReconcileState.CLEARED == s.getReconciled()
				? ReconcileState.NOT_RECONCILED
				: ReconcileState.CLEARED );

		s.setReconciled( newrec );

		int idx = transtable.getSelectionModel().getSelectedIndex();
		if ( ReconcileState.CLEARED == newrec ) {
			ListIterator<Transaction> it = transactions.listIterator( idx );
			while ( it.hasNext() ) {
				// move forward to our first unreconciled transaction
				Transaction next = it.next();
				Split nextsplit = next.getSplit( account );
				if ( ReconcileState.NOT_RECONCILED == nextsplit.getReconciled() ) {
					idx = it.nextIndex() - 1;
					break;
				}
			}
		}

		if ( idx < transactions.size() ) {
			transtable.getSelectionModel().clearAndSelect( idx );
			transtable.getFocusModel().focus( idx );

			// scroll, but not all the way...give a couple rows buffer
			if ( idx - 2 > 0 ) {
				transtable.scrollTo( idx - 2 );
			}
		}

		updateRecoProp();
	}

	private void updateRecoProp() {
		int calc = 0;
		for ( Split s : getSplits() ) {
			if ( ReconcileState.CLEARED == s.getReconciled() ) {
				int cents = s.getValue().value();
				if ( s.isCredit() ) {
					calc += cents;
				}
				else {
					calc -= cents;
				}
			}
		}

		if ( !account.getAccountType().isDebitPlus() ) {
			calc = 0 - calc;
		}

		reco.set( new Money( calc ) );
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
	protected boolean includable( Transaction t ) {
		boolean ok = ( super.includable( t ) && !t.getDate().after( date ) );

		// check to make sure this isn't an already-reconciled split
		if ( ok ) {
			Split s = t.getSplit( account );
			ok = ( !( null == s || ReconcileState.RECONCILED == s.getReconciled() ) );
		}

		return ok;
	}

	@Override
	public void added( Transaction t ) {
		super.added( t );
		if ( includable( t ) ) {
			updateRecoProp();
		}
	}

	@Override
	public void updated( Transaction t ) {
		super.updated( t );
		// see if this new transaction is in our
		// current list...if not, we need to add it

		final URI tid = t.getId();
		if ( includable( t ) ) {
			boolean found = false;
			for ( Transaction tt : getData() ) {
				if ( tt.getId().equals( tid ) ) {
					found = true;
					updateRecoProp();
					break; // nothing more to do...it's already been updated in super
				}
			}

			if ( !found ) {
				super.added( t );
			}
		}
		else {
			// see if we need to remove this split (already reconciled)
			removed( tid );
			updateRecoProp();
		}
	}

	public void upgradeSplits() {
		List<Split> splits = new ArrayList<>();
		for ( Transaction t : transactions ) {
			Split s = t.getSplit( account );
			if ( null != s && ReconcileState.CLEARED == s.getReconciled() ) {
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


	@Override
	public void reconciled( Account acct, Collection<Split> splits ) {
		super.reconciled( acct, splits );
	}
}
