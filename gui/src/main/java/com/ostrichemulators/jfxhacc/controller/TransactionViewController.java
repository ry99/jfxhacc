/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.MainApp;
import com.ostrichemulators.jfxhacc.ShutdownListener;
import com.ostrichemulators.jfxhacc.controller.TransactionEntryController.CloseListener;
import com.ostrichemulators.jfxhacc.cells.DateCellFactory;
import com.ostrichemulators.jfxhacc.cells.JournalCellFactory;
import com.ostrichemulators.jfxhacc.cells.MoneyCellFactory;
import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoCellFactory;
import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoValueFactory;
import com.ostrichemulators.jfxhacc.cells.RecoCellFactory;
import com.ostrichemulators.jfxhacc.datamanager.AccountManager;
import com.ostrichemulators.jfxhacc.datamanager.JournalManager;
import com.ostrichemulators.jfxhacc.datamanager.PayeeManager;
import com.ostrichemulators.jfxhacc.datamanager.SplitStubManager;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitBase.ReconcileState;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
public class TransactionViewController implements ShutdownListener {

	private static final Logger log = Logger.getLogger( TransactionViewController.class );

	@FXML
	private SplitPane splitter;
	@FXML
	protected TableView<SplitStub> transtable;
	@FXML
	private TableColumn<SplitStub, URI> jcol;
	@FXML
	private TableColumn<SplitStub, Date> date;
	@FXML
	private TableColumn<SplitStub, String> number;
	@FXML
	private TableColumn<SplitStub, PAMData> payee;
	@FXML
	private TableColumn<SplitStub, Money> increase;
	@FXML
	private TableColumn<SplitStub, Money> decrease;
	@FXML
	private TableColumn<SplitStub, ReconcileState> reco;

	private final TransactionEntry dataentry;

	protected Account account;
	private final PayeeAccountMemoValueFactory payeefac;
	private boolean firstload = true;
	private SplitStub transUnderMouse = null;
	protected double splitterpos;
	protected TransactionMapper tmap;
	private final SplitStubManager stubman;
	private final PayeeManager payman;
	private final AccountManager acctman;
	private final JournalManager jrnlman;

	public TransactionViewController( TransactionMapper t, SplitStubManager stubs ) {
		tmap = t;
		stubman = stubs;
		payman = new PayeeManager( MainApp.getEngine() );
		acctman = new AccountManager( MainApp.getEngine(), stubs );
		jrnlman = new JournalManager( MainApp.getEngine() );
		dataentry = new TransactionEntry( MainApp.getEngine(), acctman, payman );
		payeefac = new PayeeAccountMemoValueFactory( stubman, acctman, payman );
	}

	public void setAccount( Account acct ) {
		account = acct;
		dataentry.setAccount( acct );
		splitter.setDividerPositions( 1.0 );
		refresh();
		transtable.scrollTo( getData().size() - 1 );

		if ( acct.getAccountType().isDebitPlus() ) {
			increase.setCellValueFactory( ( TableColumn.CellDataFeatures<SplitStub, Money> p )
					-> p.getValue().getDebitProperty() );
			decrease.setCellValueFactory( ( TableColumn.CellDataFeatures<SplitStub, Money> p )
					-> p.getValue().getCreditProperty() );
		}
		else {
			increase.setCellValueFactory( ( TableColumn.CellDataFeatures<SplitStub, Money> p )
					-> p.getValue().getCreditProperty() );
			decrease.setCellValueFactory( ( TableColumn.CellDataFeatures<SplitStub, Money> p )
					-> p.getValue().getDebitProperty() );
		}

//		log.debug( "acct: " + acct );
//		for ( SplitStub s : transtable.getItems() ) {
//			log.debug( s );
//		}
	}

	protected ObservableList<SplitStub> getData() {
		return transtable.getItems();
	}

	protected String getPrefPrefix() {
		return "transviewer";
	}

	protected Collection<Predicate<SplitStub>> getFilters() {
		return Arrays.asList( MainApp.PF.account( account ) );
	}

	public void refresh() {
		List<TableColumn<SplitStub, ?>> sortcols = new ArrayList<>();
		sortcols.addAll( transtable.getSortOrder() );

		transtable.setItems( stubman.getSplitStubs( getFilters() ) );

		log.debug( "populated transaction viewer with " + transtable.getItems().size()
				+ " transactions" );

		if ( firstload ) {
			firstload = false;
			String PREF_SORTCOL = getPrefPrefix() + ".sort.col";
			String PREF_SORTASC = getPrefPrefix() + ".sort.asc";

			Preferences prefs = Preferences.userNodeForPackage( getClass() );
			ObservableList<TableColumn<SplitStub, ?>> cols = transtable.getColumns();
			int sortcol = prefs.getInt( PREF_SORTCOL, 0 );
			boolean sortasc = prefs.getBoolean( PREF_SORTASC, true );

			int i = 0;
			for ( TableColumn<SplitStub, ?> tc : cols ) {
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

		log.debug( "sorting data" );
		transtable.getSortOrder().setAll( sortcols );
		transtable.sort();
		log.debug( "done sorting" );
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

		jcol.setCellValueFactory( ( TableColumn.CellDataFeatures<SplitStub, URI> p )
				-> p.getValue().getJournalIdProperty() );
		jcol.setCellFactory( new JournalCellFactory( MainApp.getEngine() ) );

		date.setCellValueFactory( ( TableColumn.CellDataFeatures<SplitStub, Date> p )
				-> p.getValue().getDateProperty() );
		date.setCellFactory( new DateCellFactory() );

		payee.setCellValueFactory( payeefac );
		payee.setCellFactory( getPayeeAccountMemoCellFactory() );

		number.setCellValueFactory( ( TableColumn.CellDataFeatures<SplitStub, String> p )
				-> new ReadOnlyStringWrapper( p.getValue().getNumber() ) );

		reco.setCellValueFactory( ( TableColumn.CellDataFeatures<SplitStub, ReconcileState> p )
				-> p.getValue().getReconciledProperty() );
		reco.setCellFactory( new RecoCellFactory<>( false ) );

		increase.setCellFactory( new MoneyCellFactory<>() );
		decrease.setCellFactory( new MoneyCellFactory<>() );

		splitter.getItems().add( dataentry );

		Preferences prefs = Preferences.userNodeForPackage( TransactionViewController.class );
		String PREF_SPLITTER = getPrefPrefix() + ".splitter.location";
		splitterpos = prefs.getDouble( PREF_SPLITTER, 0.70 );
		splitter.setDividerPositions( 1.0 );

		transtable.setRowFactory( new Callback<TableView<SplitStub>, TableRow<SplitStub>>() {
			@Override
			public TableRow<SplitStub> call( TableView<SplitStub> p ) {
				TableRow<SplitStub> row = new TableRow<>();
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

	protected void mouseClick( SplitStub t ) {
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
			SplitStub other = stubman.getOtherStub( transUnderMouse );
			if ( null != other ) {
				Account acct = acctman.get( other.getAccountId() );
				item1 = new MenuItem( "Flip to " + acct.getName() );
				item1.setOnAction( new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent e ) {
						MainApp.select( acct );
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

	public void openEditor( SplitStub t ) {
		if ( null == t ) {
			openEditor( new Date(), getDefaultReconcileState() );
		}
		else {
			splitter.setDividerPositions( splitterpos );

			List<SplitStub> stubs
					= stubman.getSplitStubs( MainApp.PF.filter( Transactions.TYPE,
							t.getTransactionId() ) );

			log.debug( "openeditor" );
			for ( SplitStub s : stubs ) {
				log.debug( s );
			}
			Transaction trans
					= SplitStubManager.toTransaction( stubs, acctman, jrnlman, payman );
			dataentry.setTransaction( trans );
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
		ObservableList<TableColumn<SplitStub, ?>> cols = transtable.getColumns();
		int i = 0;

		TableColumn<SplitStub, ?> sortcol = null;
		ObservableList<TableColumn<SplitStub, ?>> sorts = transtable.getSortOrder();
		if ( !sorts.isEmpty() ) {
			sortcol = sorts.get( 0 );
		}

		for ( TableColumn<SplitStub, ?> tc : cols ) {
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
		SplitStub t = transtable.getSelectionModel().getSelectedItem();
		if ( "I".equalsIgnoreCase( code ) ) {
			ke.consume();
			Date tdate = ( null == t ? new Date() : t.getDate() );
			openEditor( tdate, getDefaultReconcileState() );
		}
		else if ( "R".equalsIgnoreCase( code ) ) {
			ke.consume();

			// cycle through the reconcile states
			ReconcileState rs = t.getReconciled();
			ReconcileState states[] = ReconcileState.values();
			try {
				tmap.reconcile( states[( rs.ordinal() + 1 ) % states.length], t );
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

	public static final class PAMData implements Comparable<PAMData> {

		public final StringProperty payee = new SimpleStringProperty();
		public final StringProperty account = new SimpleStringProperty();
		public final StringProperty memo = new SimpleStringProperty();

		public PAMData( String payee, String account, String memo ) {
			this.payee.setValue( payee );
			this.account.setValue( account );
			this.memo.setValue( memo );
		}

		@Override
		public int compareTo( PAMData o ) {
			return toString().compareTo( o.toString() );
		}

		@Override
		public String toString() {
			return payee.getValueSafe() + account.getValueSafe() + memo.getValueSafe();
		}
	}
}
