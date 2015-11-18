/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.TransactionEntry.CloseListener;
import com.ostrichemulators.jfxhacc.cells.CreditDebitValueFactory;
import com.ostrichemulators.jfxhacc.cells.DateCellFactory;
import com.ostrichemulators.jfxhacc.cells.MoneyCellFactory;
import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoCellFactory;
import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoValueFactory;
import com.ostrichemulators.jfxhacc.cells.RecoCellFactory;
import com.ostrichemulators.jfxhacc.cells.RecoValueFactory;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.TransactionListener;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class TransactionViewController implements ShutdownListener, TransactionListener {

	private static final Logger log = Logger.getLogger( TransactionViewController.class );
	private static final String PREF_SPLITTER = "transviewer.splitter.location";
	private static final String PREF_SORTCOL = "transviewer.sort.col";
	private static final String PREF_SORTASC = "transviewer.sort.asc";

	@FXML
	private SplitPane splitter;
	@FXML
	protected TableView<Transaction> transtable;
	@FXML
	private TableColumn<Transaction, Date> date;
	@FXML
	private TableColumn<Transaction, String> number;
	@FXML
	private TableColumn<Transaction, PAMData> payee;
	@FXML
	private TableColumn<Transaction, Money> credit;
	@FXML
	private TableColumn<Transaction, Money> debit;
	@FXML
	private TableColumn<Transaction, ReconcileState> reco;

	private final TransactionEntry dataentry = new TransactionEntry();

	protected Account account;
	protected Journal journal;
	private final PayeeAccountMemoValueFactory payeefac = new PayeeAccountMemoValueFactory();
	private final CreditDebitValueFactory creditfac = new CreditDebitValueFactory( true );
	private final CreditDebitValueFactory debitfac = new CreditDebitValueFactory( false );
	private final RecoValueFactory recofac = new RecoValueFactory();
	private boolean firstload = true;
	protected double splitterpos;
	protected TransactionMapper tmap;
	protected final ObservableList<Transaction> transactions
			= FXCollections.observableArrayList();

	public void setAccount( Account acct, Journal j ) {
		account = acct;
		journal = j;
		payeefac.setAccount( acct );
		creditfac.setAccount( acct );
		debitfac.setAccount( acct );
		recofac.setAccount( acct );
		dataentry.setAccount( acct, journal );
		refresh();
	}

	protected List<Transaction> getTransactions() {
		try {
			log.debug( "fetching transactions for " + account + " in journal " + journal );
			return tmap.getAll( account, journal );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
		return new ArrayList<>();
	}

	public void refresh() {
		List<TableColumn<Transaction, ?>> sortcols = new ArrayList<>();
		sortcols.addAll( transtable.getSortOrder() );

		transactions.setAll( getTransactions() );
		log.debug( "populated transaction viewer with " + transactions.size()
				+ " transactions" );
		transtable.setItems( transactions );

		if ( firstload ) {
			firstload = false;
			Preferences prefs = Preferences.userNodeForPackage( getClass() );
			ObservableList<TableColumn<Transaction, ?>> cols = transtable.getColumns();
			int sortcol = prefs.getInt( PREF_SORTCOL, 0 );
			boolean sortasc = prefs.getBoolean( PREF_SORTASC, true );

			int i = 0;
			for ( TableColumn<Transaction, ?> tc : cols ) {
				double size = prefs.getDouble( "transviewer.col" + ( i++ ), -1 );
				if ( size > 0 ) {
					tc.setPrefWidth( size );
				}

				if ( sortcol == i ) {
					tc.setSortType( sortasc ? SortType.ASCENDING : SortType.DESCENDING );
					sortcols.clear();
					sortcols.add( tc );
				}
			}
		}

		transtable.getSortOrder().setAll( sortcols );
		transtable.sort();
	}

	protected double getRowHeight() {
		return 48d; // FIXME
	}

	protected PayeeAccountMemoCellFactory getPayeeAccountMemoCellFactory() {
		return new PayeeAccountMemoCellFactory( false );
	}

	@FXML
	public void initialize() {
		MainApp.getShutdownNotifier().addShutdownListener( this );
		transtable.setOnKeyTyped( event -> keyTyped( event ) );

		transtable.setFixedCellSize( getRowHeight() );
		transtable.setItems( transactions );

		date.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, Date> p )
				-> new ReadOnlyObjectWrapper<>( p.getValue().getDate() ) );
		date.setCellFactory( new DateCellFactory() );

		payee.setCellValueFactory( payeefac );
		payee.setCellFactory( getPayeeAccountMemoCellFactory() );

		number.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, String> p )
				-> new ReadOnlyStringWrapper( p.getValue().getNumber() ) );

		reco.setCellValueFactory( recofac );
		reco.setCellFactory( new RecoCellFactory() );

		credit.setCellValueFactory( creditfac );
		credit.setCellFactory( new MoneyCellFactory() );

		debit.setCellValueFactory( debitfac );
		debit.setCellFactory( new MoneyCellFactory() );

		splitter.getItems().add( dataentry );

		Preferences prefs = Preferences.userNodeForPackage( TransactionViewController.class );
		splitterpos = prefs.getDouble( PREF_SPLITTER, 0.70 );
		splitter.setDividerPositions( 1.0 );

		transtable.setOnMouseClicked( new EventHandler<MouseEvent>() {

			@Override
			public void handle( MouseEvent t ) {
				double y = t.getY();
				double maxy = transactions.size() * transtable.getFixedCellSize();

				// see if our mouse click is actually past our row position
				// (user clicked in empty space below all items)
				mouseClick( y > maxy
						? null
						: transtable.getSelectionModel().getSelectedItem() );
			}
		} );

		tmap = MainApp.getEngine().getTransactionMapper();
		tmap.addMapperListener( this );

		dataentry.addCloseListener( new CloseListener() {

			@Override
			public void closed() {
				splitterpos = splitter.getDividerPositions()[0];
				splitter.setDividerPositions( 1.0 );
				transtable.requestFocus();
			}

			@Override
			public void added( Transaction t ) {
				closed();
			}

			@Override
			public void updated( Transaction t ) {
				closed();
			}
		} );
	}

	protected ReconcileState getDefaultReconcileState() {
		return ReconcileState.NOT_RECONCILED;
	}

	protected void mouseClick( Transaction t ) {
		if ( null == t ) {
			openEditor( new Date(), getDefaultReconcileState() );
		}
		else {
			openEditor( transtable.getSelectionModel().getSelectedItem() );
		}
	}

	public void openEditor( Transaction t ) {
		if ( null == t ) {
			openEditor( new Date(), getDefaultReconcileState() );
		}
		else {
			splitter.setDividerPositions( splitterpos );
			dataentry.setTransaction( t );
		}
	}

	public void openEditor( Date d, ReconcileState rs ) {
		splitter.setDividerPositions( splitterpos );

		boolean to = true;
		dataentry.setTransaction( d, rs, to );
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
			prefs.putDouble( "transviewer.col" + ( i++ ), tc.getWidth() );

			if ( tc.equals( sortcol ) ) {
				prefs.putInt( PREF_SORTCOL, i );

				SortType stype = tc.getSortType();
				prefs.putBoolean( PREF_SORTASC,
						( null == stype ? true : stype == SortType.ASCENDING ) );
			}
		}

		prefs.putDouble( PREF_SPLITTER, splitterpos );
	}

	@FXML
	public void keyTyped( KeyEvent ke ) {
		String code = ke.getCharacter();
		Transaction t = transtable.getSelectionModel().getSelectedItem();
		if ( "I".equalsIgnoreCase( code ) ) {
			ke.consume();
			Date tdate = ( null == t ? new Date() : t.getDate() );
			openEditor( tdate, getDefaultReconcileState() );
		}
		else if ( "R".equalsIgnoreCase( code ) ) {
			ke.consume();
			Split s = t.getSplits().get( account );
			// cycle through the reconcile states
			ReconcileState rs = s.getReconciled();
			ReconcileState states[] = ReconcileState.values();
			try {
				tmap.reconcile( states[( rs.ordinal() + 1 ) % states.length], account, s );
				updated( t );
			}
			catch ( MapperException me ) {
				log.error( me, me );
				// FIXME: tell the user
			}

		}
		else if ( "E".equalsIgnoreCase( code ) ) {
			ke.consume();
			openEditor( transtable.getSelectionModel().getSelectedItem() );
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

	@Override
	public void removed( URI uri ) {
		ListIterator<Transaction> transit = transactions.listIterator();
		while ( transit.hasNext() ) {
			Transaction listt = transit.next();
			if ( listt.getId().equals( uri ) ) {
				transit.remove();
				break;
			}
		}
	}

	@Override
	public void reconciled( Account acct, Collection<Split> splits ) {
		Map<URI, Transaction> revmap = new HashMap<>();
		for ( Transaction t : transactions ) {
			for ( Split s : t.getSplits().values() ) {
				revmap.put( s.getId(), t );
			}
		}

		for ( Split s : splits ) {
			Transaction t = revmap.get( s.getId() );
			t.addSplit( acct, s );
			updated( t );
		}
	}

	public static final class PAMData implements Comparable<PAMData> {

		public final String payee;
		public final String account;
		public final String memo;

		public PAMData( String payee, String account, String memo ) {
			this.payee = payee;
			this.account = account;
			this.memo = memo;
		}

		@Override
		public int compareTo( PAMData o ) {
			return toString().compareTo( o.toString() );
		}

		@Override
		public String toString() {
			return payee + account + memo;
		}
	}
}
