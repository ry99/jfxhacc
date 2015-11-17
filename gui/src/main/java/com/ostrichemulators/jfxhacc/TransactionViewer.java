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
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class TransactionViewer extends AnchorPane implements ShutdownListener, MapperListener<Transaction> {

	private static final Logger log = Logger.getLogger( TransactionViewer.class );
	private static final String PREF_SPLITTER = "transviewer.splitter.location";
	private static final String PREF_SORTCOL = "transviewer.sort.col";
	private static final String PREF_SORTASC = "transviewer.sort.asc";

	@FXML
	private SplitPane splitter;
	@FXML
	private TableView<Transaction> transtable;
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

	private TransactionEntry dataentry = new TransactionEntry();

	private Account account;
	private final PayeeAccountMemoValueFactory payeefac = new PayeeAccountMemoValueFactory();
	private final CreditDebitValueFactory creditfac = new CreditDebitValueFactory( true );
	private final CreditDebitValueFactory debitfac = new CreditDebitValueFactory( false );
	private final RecoValueFactory recofac = new RecoValueFactory();
	private boolean firstload = true;
	private double splitterpos;
	private TransactionMapper tmap;
	private final ObservableList<Transaction> transactions
			= FXCollections.observableArrayList();

	public TransactionViewer() {
		FXMLLoader fxmlLoader
				= new FXMLLoader( getClass().getResource( "/fxml/TransactionViewer.fxml" ) );
		fxmlLoader.setRoot( this );
		fxmlLoader.setController( this );

		try {
			fxmlLoader.load();
		}
		catch ( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

	public void setAccount( Account acct ) {
		account = acct;
		payeefac.setAccount( acct );
		creditfac.setAccount( acct );
		debitfac.setAccount( acct );
		recofac.setAccount( acct );
		dataentry.setAccount( acct );
		refresh();
	}

	public void refresh() {
		List<TableColumn<Transaction, ?>> sortcols = new ArrayList<>();
		sortcols.addAll( transtable.getSortOrder() );

		transactions.clear();

		DataEngine engine = MainApp.getEngine();
		try {
			List<Journal> journals = new ArrayList<>( engine.getJournalMapper().getAll() );
			Journal jnl = journals.get( 0 );
			dataentry.setJournal( jnl );
			log.debug( "fetching transactions for " + account + " in journal " + jnl );
			transactions.addAll( tmap.getAll( account, jnl ) );
			log.debug( "populated transaction viewer with " + transactions.size()
					+ " transactions" );
			transtable.setItems( transactions );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

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

	@FXML
	public void initialize() {
		MainApp.getShutdownNotifier().addShutdownListener( this );
		transtable.setOnKeyTyped( event -> keyTyped( event ) );

		transtable.setItems( transactions );

		transtable.setFixedCellSize( 48 ); // FIXME

		date.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, Date> p )
				-> new ReadOnlyObjectWrapper<>( p.getValue().getDate() ) );
		date.setCellFactory( new DateCellFactory() );

		payee.setCellValueFactory( payeefac );
		payee.setCellFactory( new PayeeAccountMemoCellFactory() );

		number.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, String> p )
				-> new ReadOnlyStringWrapper( p.getValue().getNumber() ) );

		reco.setCellValueFactory( recofac );
		reco.setCellFactory( new RecoCellFactory() );

		credit.setCellValueFactory( creditfac );
		credit.setCellFactory( new MoneyCellFactory() );

		debit.setCellValueFactory( debitfac );
		debit.setCellFactory( new MoneyCellFactory() );

		splitter.getItems().add( dataentry );

		Preferences prefs = Preferences.userNodeForPackage( TransactionViewer.class );
		splitterpos = prefs.getDouble( PREF_SPLITTER, 0.70 );
		splitter.setDividerPositions( 1.0 );

		transtable.setOnMouseClicked( new EventHandler<MouseEvent>() {

			@Override
			public void handle( MouseEvent t ) {
				double y = t.getY();
				double maxy = transtable.getItems().size() * transtable.getFixedCellSize();

				// see if our mouse click is actually past our row position
				// (user clicked in empty space below all items)
				if ( y > maxy ) {
					openEditor( new Date(), ReconcileState.NOT_RECONCILED );
				}
				else {
					openEditor( transtable.getSelectionModel().getSelectedItem() );
				}
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

	public void openEditor( Transaction t ) {
		if ( null == t ) {
			openEditor( new Date(), ReconcileState.NOT_RECONCILED );
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
			ReconcileState rs = ReconcileState.NOT_RECONCILED;
			openEditor( tdate, rs );
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
