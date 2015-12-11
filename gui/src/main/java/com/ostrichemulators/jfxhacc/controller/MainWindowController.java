package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.MainApp;
import com.ostrichemulators.jfxhacc.MainApp.StageRememberer;
import com.ostrichemulators.jfxhacc.ShutdownListener;
import com.ostrichemulators.jfxhacc.cells.MoneyTableTreeCellFactory;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.engine.impl.RdfDataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper.BalanceType;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.mapper.TransactionListener;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.utility.AccountBalanceCache;
import com.ostrichemulators.jfxhacc.utility.AccountBalanceCache.MoneyPair;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

public class MainWindowController implements ShutdownListener {

	private static final String PREF_SELECTED = "accountviewer.selected";
	private static final String PREF_ASIZE = "accountviewer.account.width";
	private static final String PREF_BSIZE = "accountviewer.balance.width";
	private static final String PREF_SORTCOL = "accountviewer.sort.col";
	private static final String PREF_SORTASC = "accountviewer.sort.asc";
	private static final String PREF_SPLITTER = "stage.splitter.location";

	private static final Logger log = Logger.getLogger( MainWindowController.class );
	@FXML
	private TitledPane accountsPane;
	@FXML
	private TreeTableView<Account> accounts;
	@FXML
	private TreeTableColumn<Account, String> accountName;
	@FXML
	private TreeTableColumn<Account, Money> accountBalance;
	@FXML
	private Accordion accordion;
	@FXML
	private SplitPane splitter;
	@FXML
	private Label balrec;
	@FXML
	private Label balcurr;
	@FXML
	private Label acctname;
	@FXML
	private Label transnum;
	@FXML
	private Label messagelabel;
	@FXML
	private Button recoBtn;
	@FXML
	private VBox topxbox;
	@FXML
	private Menu fileMenu;

	private final TransactionViewController transactions = new TransactionViewController();
	private AccountBalanceCache acb;
	private Journal journal;

	@FXML
	public void initialize() {
		MainApp.getShutdownNotifier().addShutdownListener( this );

		DataEngine engine = MainApp.getEngine();
		AccountMapper amap = engine.getAccountMapper();

		acb = new AccountBalanceCache( amap, engine.getTransactionMapper() );

		if ( log.isTraceEnabled() ) {
			MenuItem mi = new MenuItem( "Dump Database" );
			mi.setOnAction( event -> {
				File out = new File( FileUtils.getTempDirectory(), "dump.ttl" );
				try {
					RdfDataEngine.class.cast( MainApp.getEngine() ).dump( out );
					setMessage( "database dumped to " + out );
				}
				catch ( RepositoryException | IOException ioe ) {
					log.error( ioe, ioe );
				}
			} );
			fileMenu.getItems().add( mi );
		}

		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		String selidstr = prefs.get( PREF_SELECTED, "" );
		URI selected = ( selidstr.isEmpty() ? null : new URIImpl( selidstr ) );

		final TreeItem<Account> root = new TreeItem<>();

		accordion.setExpandedPane( accountsPane );
		TreeItem<Account> toselect1 = null;
		try {
			journal = engine.getJournalMapper().getAll().iterator().next();
			toselect1 = retree( amap, root, selected );

			List<Account> accts = amap.getPopularAccounts( 10 );
			ToggleGroup bg = new ToggleGroup();
			for ( Account a : accts ) {
				ToggleButton btn = new ToggleButton( a.getName() );
				btn.prefWidthProperty().bind( topxbox.widthProperty() );
				btn.prefHeightProperty().bind( topxbox.heightProperty().divide( accts.size() ) );
				btn.setToggleGroup( bg );
				topxbox.getChildren().add( btn );

				btn.setOnAction( ( ActionEvent event ) -> {
					TreeItem<Account> ti = findItem( a );
					accounts.getSelectionModel().select( ti );
					accounts.scrollTo( accounts.getRow( ti ) );
				} );
				accounts.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Account>>() {

					@Override
					public void changed( ObservableValue<? extends TreeItem<Account>> ov, TreeItem<Account> t, TreeItem<Account> t1 ) {
						Account treeval = t1.getValue();
						if ( treeval.equals( a ) ) {
							btn.setSelected( true );
						}
					}
				} );
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		final TreeItem<Account> toselect = toselect1;

		accountName.setCellValueFactory( ( CellDataFeatures<Account, String> p )
				-> p.getValue().getValue().getNameProperty() );

		accountBalance.setCellValueFactory( ( CellDataFeatures<Account, Money> p )
				-> acb.getCurrentProperty( p.getValue().getValue() ) );
		accountBalance.setCellFactory( new MoneyTableTreeCellFactory<>() );

		root.setExpanded( true );
		accounts.setRoot( root );

		try {
			splitter.getItems().add( makeTransactionViewer( transactions ) );
		}
		catch ( IOException ioe ) {
			log.fatal( ioe, ioe );
		}

		makeListeners( acb, engine, root );

		Platform.runLater( new Runnable() {
			@Override
			public void run() {

				accounts.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );

				double splitterpos = prefs.getDouble( PREF_SPLITTER, 0.25 );
				splitter.setDividerPositions( splitterpos );

				double asize = prefs.getDouble( PREF_ASIZE, 100 );
				double bsize = prefs.getDouble( PREF_BSIZE, 100 );
				accountName.setPrefWidth( asize );
				accountBalance.setPrefWidth( bsize );

				ObservableList<TreeTableColumn<Account, ?>> cols = accounts.getColumns();
				int sortcol = prefs.getInt( PREF_SORTCOL, 0 );
				boolean sortasc = prefs.getBoolean( PREF_SORTASC, true );

				int i = 0;
				for ( TreeTableColumn<Account, ?> tc : cols ) {
					i++;

					if ( sortcol == i ) {
						tc.setSortType( sortasc
								? TreeTableColumn.SortType.ASCENDING
								: TreeTableColumn.SortType.DESCENDING );
						accounts.getSortOrder().clear();
						accounts.getSortOrder().add( tc );
						accounts.sort();
					}
				}

				// select and scroll after the sorting is complete
				if ( null != toselect ) {
					accounts.getSelectionModel().select( toselect );
					accounts.scrollTo( accounts.getRow( toselect ) );
				}

			}
		} );
	}

	private void makeListeners( AccountBalanceCache acb, DataEngine eng, TreeItem<Account> root ) {
		AccountMapper amap = eng.getAccountMapper();
		accounts.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Account>>() {

			@Override
			public void changed( ObservableValue<? extends TreeItem<Account>> ov,
					TreeItem<Account> oldsel, TreeItem<Account> newsel ) {
				Account acct = newsel.getValue();
				transactions.setAccount( acct, journal );
				String fname = GuiUtils.getFullName( acct, amap );
				acctname.setText( fname );
				MainApp.getShutdownNotifier().getStage().setTitle( fname );
				updateBalancesLabel();
				recoBtn.setDisable( false );
			}
		} );

		acb.getMap().addListener( new MapChangeListener<Account, MoneyPair>() {

			@Override
			public void onChanged( MapChangeListener.Change<? extends Account, ? extends MoneyPair> change ) {
				TreeItem<Account> item = accounts.getSelectionModel().getSelectedItem();
				Account acct = ( null == item ? null : item.getValue() );

				if ( change.getKey().equals( acct ) ) {
					updateBalancesLabel();
				}
			}
		} );

		amap.addMapperListener( new MapperListener<Account>() {
			@Override
			public void added( Account t ) {
				try {
					Account parent = amap.getParent( t );
					TreeItem<Account> pnode = findItem( parent );
					pnode.getChildren().add( new TreeItem<>( t ) );
				}
				catch ( MapperException me ) {
					log.error( me, me );
				}
			}

			@Override
			public void updated( Account t ) {
				TreeItem<Account> pnode = findItem( t );
				pnode.getValue().setName( t.getName() );
			}

			@Override
			public void removed( URI uri ) {
				TreeItem<Account> pnode = findItem( uri );
				pnode.getParent().getChildren().remove( pnode );
			}
		} );

		eng.getTransactionMapper().addMapperListener( new TransactionListener() {
			private Account account() {
				return accounts.getSelectionModel().getSelectedItem().getValue();
			}

			@Override
			public void reconciled( Account acct, Collection<Split> splits ) {
				if ( acct.equals( account() ) ) {
					updateBalancesLabel();
				}
			}

			@Override
			public void added( Transaction t ) {
				if ( null != t.getSplit( account() ) ) {
					updateBalancesLabel();
				}
			}

			@Override
			public void updated( Transaction t ) {
				added( t );
			}

			@Override
			public void removed( URI uri ) {
				updateBalancesLabel();
			}
		} );
	}

	public void select( Account acct ) {
		TreeItem<Account> ti = findItem( acct );
		if ( !accounts.getRoot().equals( ti ) ) {
			accounts.getSelectionModel().select( ti );
		}
	}

	private TreeItem<Account> retree( AccountMapper amap, TreeItem<Account> root,
			URI selected ) throws MapperException {
		Map<Account, TreeItem<Account>> items = new HashMap<>();
		Map<Account, Account> childparentlkp = new HashMap<>();
		TreeItem<Account> toselect = null;

		childparentlkp.putAll( amap.getParentMap() );
		for ( Account acct : childparentlkp.keySet() ) {
			TreeItem<Account> aitem = new TreeItem<>( acct );
			items.put( acct, aitem );

			if ( acct.getId().equals( selected ) ) {
				toselect = aitem;
			}
		}

		for ( Map.Entry<Account, Account> en : childparentlkp.entrySet() ) {
			Account child = en.getKey();
			Account parent = en.getValue();
			TreeItem<Account> childitem = items.get( child );
			TreeItem<Account> parentitem
					= ( null == parent ? root : items.get( parent ) );
			parentitem.getChildren().add( childitem );
		}

		return toselect;
	}

	private void updateBalancesLabel() {
		TreeItem<Account> item = accounts.getSelectionModel().getSelectedItem();
		if ( null != item ) {
			Account acct = item.getValue();
			Money curr = acb.get( acct, BalanceType.CURRENT );
			Money rec = acb.get( acct, BalanceType.RECONCILED );
			balrec.setText( rec.toString() );
			balcurr.setText( curr.toString() );

			int size = transactions.getData().size();
			String transtxt = ( 1 == size ? " Transaction" : " Transactions" );
			transnum.setText( size + transtxt );
		}
	}

	@Override
	public void shutdown() {
		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		TreeItem<Account> selected = accounts.getSelectionModel().getSelectedItem();
		if ( null != selected ) {
			prefs.put( PREF_SELECTED, selected.getValue().getId().stringValue() );
		}

		prefs.putDouble( PREF_ASIZE, accounts.getColumns().get( 0 ).getWidth() );
		prefs.putDouble( PREF_BSIZE, accounts.getColumns().get( 1 ).getWidth() );
		prefs.putDouble( PREF_SPLITTER, splitter.getDividerPositions()[0] );

		ObservableList<TreeTableColumn<Account, ?>> cols = accounts.getColumns();
		TreeTableColumn<Account, ?> sortcol = null;
		ObservableList<TreeTableColumn<Account, ?>> sorts = accounts.getSortOrder();
		if ( !sorts.isEmpty() ) {
			sortcol = sorts.get( 0 );
		}

		int i = 0;
		for ( TreeTableColumn<Account, ?> tc : cols ) {
			i++;
			if ( tc.equals( sortcol ) ) {
				prefs.putInt( PREF_SORTCOL, i );

				TreeTableColumn.SortType stype = tc.getSortType();
				prefs.putBoolean( PREF_SORTASC,
						( null == stype ? true : stype == TreeTableColumn.SortType.ASCENDING ) );
			}
		}
	}

	@FXML
	public void close() {
		MainApp.getShutdownNotifier().getStage().close();
	}

	@FXML
	public void showmemtrans() {
		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/RecurringTransactionWindow.fxml" ) );
		RecurringTransactionWindowController cnt
				= new RecurringTransactionWindowController( MainApp.getEngine() );
		loader.setController( cnt );

		try {
			Parent root = loader.load();

			Stage stage = new Stage();
			stage.setTitle( "Recurring Transactions" );
			stage.setScene( new Scene( root ) );
			cnt.setStage( stage );

			StageRememberer mem = new StageRememberer( stage, "memtrans" );
			mem.restore( stage );
			stage.setOnHiding( mem );
			stage.show();
		}
		catch ( IOException e ) {
			log.error( e, e );
		}
	}

	@FXML
	public void newtrans() {
		transactions.openEditor( new Date(), ReconcileState.NOT_RECONCILED );
	}

	@FXML
	public void reconcile() {
		Account acct = accounts.getSelectionModel().getSelectedItem().getValue();

		try {
			FXMLLoader loader
					= new FXMLLoader( getClass().getResource( "/fxml/ReconcileWindow.fxml" ) );
			ReconcileWindowController controller
					= new ReconcileWindowController( acb.getRecoProperty( acct ) );

			loader.setController( controller );
			Parent root = loader.load();
			controller.setAccount( acct, journal );

			Stage stage = new Stage();
			stage.setTitle( "Reconcile " + acct.getName() );
			stage.setScene( new Scene( root ) );
			controller.setStage( stage );

			StageRememberer mem = new StageRememberer( stage, "reconcile" );
			mem.restore( stage );
			stage.setOnHiding( mem );

			stage.show();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@FXML
	void editAccount( ActionEvent event ) {
		// a little hack-y, but if we have a null event, assume we want a new account
		Account acct = ( null == event
				? null : accounts.getSelectionModel().getSelectedItem().getValue() );

		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/AccountDetails.fxml" ) );
		AccountDetailsController acd = new AccountDetailsController( acct, MainApp.getEngine() );
		loader.setController( acd );
		try {
			Scene scene = new Scene( loader.load() );
			Stage stage = new Stage();
			stage.setTitle( null == acct ? "Create Account" : "Edit " + acct.getName() );
			stage.setScene( scene );

			acd.setOkListener( e -> {
				stage.close();
			} );

			acd.setCancelAction( e -> {
				stage.close();
			} );

			stage.show();
		}
		catch ( IOException ioe ) {
			log.error( ioe, ioe );
		}

	}

	@FXML
	void newAccount( ActionEvent event ) {
		editAccount( null );
	}

	private Node makeTransactionViewer( TransactionViewController controller ) throws IOException {
		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/TransactionViewer.fxml" ) );
		loader.setController( controller );
		return loader.load();
	}

	private TreeItem<Account> findItem( URI u ) {
		if ( null == u ) {
			return accounts.getRoot();
		}

		Deque<TreeItem<Account>> todo = new ArrayDeque<>();
		todo.addAll( accounts.getRoot().getChildren() );
		while ( !todo.isEmpty() ) {
			TreeItem<Account> n = todo.poll();
			if ( n.getValue().getId().equals( u ) ) {
				return n;
			}

			todo.addAll( n.getChildren() );
		}
		return null;
	}

	private TreeItem<Account> findItem( Account a ) {
		return findItem( null == a ? null : a.getId() );
	}

	public void setMessage( String message ) {
		messagelabel.setOpacity( 1.0d );
		messagelabel.setText( message );

		new AnimationTimer() {
			// wait for 4 seconds, fade for 2 seconds (in nanoseconds)
			private static final long WAITLIMIT = 4 * 1000000000l;
			private static final long FADELIMIT = 2 * 1000000000l;
			long start = 0;

			@Override
			public void handle( long l ) {
				if ( 0 == start ) {
					start = l;
				}

				long diff = l - start;
				if ( diff < WAITLIMIT ) {
					return;
				}
				diff -= WAITLIMIT;

				double pct = ( (double) diff / (double) FADELIMIT );
				messagelabel.setOpacity( 1.0 - pct );
				if ( pct > 1.0d ) {
					stop();
				}
			}
		}.start();
	}
}
