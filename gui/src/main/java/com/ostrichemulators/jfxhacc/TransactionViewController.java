/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.TransactionEntryController.CloseListener;
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
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.utility.TransactionHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class TransactionViewController implements ShutdownListener, TransactionListener {

	private static final Logger log = Logger.getLogger( TransactionViewController.class );

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

	private final TransactionEntry dataentry = new TransactionEntry( MainApp.getEngine() );

	protected Account account;
	protected Journal journal;
	private final PayeeAccountMemoValueFactory payeefac = new PayeeAccountMemoValueFactory();
	private final RecoValueFactory recofac = new RecoValueFactory();
	private boolean firstload = true;
	private Transaction transUnderMouse = null;
	protected double splitterpos;
	protected TransactionMapper tmap;
	protected final ObservableList<Transaction> transactions
			= FXCollections.observableArrayList();

	public void setAccount( Account acct, Journal j ) {
		account = acct;
		journal = j;
		payeefac.setAccount( acct );
		recofac.setAccount( acct );
		dataentry.setAccount( acct, journal );
		refresh();
		transtable.scrollTo( transactions.size() - 1 );
		splitter.setDividerPositions( 1.0 );
	}

	public ObservableList<Transaction> getData() {
		return transactions;
	}

	protected String getPrefPrefix() {
		return "transviewer";
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
			String PREF_SORTCOL = getPrefPrefix() + ".sort.col";
			String PREF_SORTASC = getPrefPrefix() + ".sort.asc";

			Preferences prefs = Preferences.userNodeForPackage( getClass() );
			ObservableList<TableColumn<Transaction, ?>> cols = transtable.getColumns();
			int sortcol = prefs.getInt( PREF_SORTCOL, 0 );
			boolean sortasc = prefs.getBoolean( PREF_SORTASC, true );

			int i = 0;
			for ( TableColumn<Transaction, ?> tc : cols ) {
				double size = prefs.getDouble( getPrefPrefix() + ".col" + ( i++ ), -1 );
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
				-> p.getValue().getDateProperty() );
		date.setCellFactory( new DateCellFactory() );

		payee.setCellValueFactory( payeefac );
		payee.setCellFactory( getPayeeAccountMemoCellFactory() );

		number.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, String> p )
				-> new ReadOnlyStringWrapper( p.getValue().getNumber() ) );

		reco.setCellValueFactory( recofac );
		reco.setCellFactory( new RecoCellFactory<>( false ) );

		credit.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, Money> p )
				-> p.getValue().getSplit( account ).getRawValueProperty() );
		credit.setCellFactory( new MoneyCellFactory<>( true ) );

		debit.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, Money> p )
				-> p.getValue().getSplit( account ).getRawValueProperty() );
		debit.setCellFactory( new MoneyCellFactory<>( false ) );

		splitter.getItems().add( dataentry );

		Preferences prefs = Preferences.userNodeForPackage( TransactionViewController.class );
		String PREF_SPLITTER = getPrefPrefix() + ".splitter.location";
		splitterpos = prefs.getDouble( PREF_SPLITTER, 0.70 );
		splitter.setDividerPositions( 1.0 );

		transtable.setRowFactory( new Callback<TableView<Transaction>, TableRow<Transaction>>() {
			@Override
			public TableRow<Transaction> call( TableView<Transaction> p ) {
				TableRow<Transaction> row = new TableRow<>();
				row.setOnMouseEntered( event -> {
					if ( null == row.getItem() ) {
						transUnderMouse = null;
					}
					else {
						transUnderMouse = row.getItem();
					}
				} );

				return row;
			}
		} );

		final ContextMenu menu = new ContextMenu();
		menu.getItems().add( new MenuItem() );
		menu.setOnShowing( event -> {
			menu.getItems().setAll( buildContextItems() );
		} );

		transtable.setContextMenu( menu );
		transtable.setOnMousePressed( new EventHandler<MouseEvent>() {

			@Override
			public void handle( MouseEvent t ) {
				if ( t.isPrimaryButtonDown() ) {
					t.consume();
					mouseClick( transUnderMouse );
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

	protected Collection<? extends MenuItem> buildContextItems() {
		List<MenuItem> items = new ArrayList<>();
		MenuItem item1;
		if ( null == transUnderMouse ) {
			item1 = new MenuItem();
		}
		else {
			Split other = TransactionHelper.getOther( transUnderMouse, account );
			if ( null != other ) {
				item1 = new MenuItem( "Flip to " + other.getAccount().getName() );
				item1.setOnAction( new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent e ) {
						MainApp.select( other.getAccount() );
					}
				} );
			}
			else {
				item1 = new MenuItem();
			}
		}

		items.add( item1 );

		return items;
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
		SplitImpl s = new SplitImpl();
		s.setAccount( account );
		s.setReconciled( rs );
		openEditor( d, s );
	}

	protected void openEditor( Date d, Split s ) {
		splitter.setDividerPositions( splitterpos );
		dataentry.setTransaction( d, s );
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
			prefs.putDouble( getPrefPrefix() + ".col" + ( i++ ), tc.getWidth() );

			if ( tc.equals( sortcol ) ) {
				String PREF_SORTCOL = getPrefPrefix() + ".sort.col";
				String PREF_SORTASC = getPrefPrefix() + ".sort.asc";
				prefs.putInt( PREF_SORTCOL, i );

				SortType stype = tc.getSortType();
				prefs.putBoolean( PREF_SORTASC,
						( null == stype ? true : stype == SortType.ASCENDING ) );
			}
		}

		String PREF_SPLITTER = getPrefPrefix() + ".splitter.location";
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
			Split s = t.getSplit( account );
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
		if ( null != t.getSplit( account ) ) {
			transactions.add( t );
			transtable.sort();
		}
	}

	@Override
	public void updated( Transaction t ) {
		if ( null != t.getSplit( account ) ) {
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
			for ( Split s : t.getSplits() ) {
				revmap.put( s.getId(), t );
			}
		}

		for ( Split s : splits ) {
			Transaction t = revmap.get( s.getId() );
			t.getSplit( acct ).setReconciled( s.getReconciled() );
			// FIXME: don't need this anymore, I think
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
