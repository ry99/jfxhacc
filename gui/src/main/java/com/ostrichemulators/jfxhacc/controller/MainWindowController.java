package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.MainApp;
import com.ostrichemulators.jfxhacc.MainApp.StageRememberer;
import com.ostrichemulators.jfxhacc.ShutdownListener;
import com.ostrichemulators.jfxhacc.cells.MoneyTableTreeCellFactory;
import com.ostrichemulators.jfxhacc.charting.AccountBalanceMaker;
import com.ostrichemulators.jfxhacc.charting.AccountDeltaMaker;
import com.ostrichemulators.jfxhacc.charting.PieMaker;
import com.ostrichemulators.jfxhacc.charting.SeriesMaker;
import com.ostrichemulators.jfxhacc.controller.ChartController.ChartType;
import com.ostrichemulators.jfxhacc.datamanager.AccountManager;
import com.ostrichemulators.jfxhacc.datamanager.SplitStubManager;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.engine.impl.RdfDataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.SplitBase.ReconcileState;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
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
	private static final String PREF_FAVORITES = "stage.favories";

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
	private VBox favoritesBox;
	@FXML
	private Menu fileMenu;
	@FXML
	private MenuItem favoriteAcct;
	@FXML
	private SplitMenuButton recurbtn;

	private final TransactionViewController transactions;
	private final SplitStubManager stubman;
	private final AccountManager acctman;
	private ObservableList<SplitStub> seltrans = null;
	private ObservableList<Account> favorites = FXCollections.observableArrayList();

	public MainWindowController( DataEngine engine ) {
		stubman = new SplitStubManager( engine );
		acctman = new AccountManager( engine, stubman );
		transactions
				= new TransactionViewController( engine.getTransactionMapper(), stubman );
	}

	@FXML
	public void initialize() {
		MainApp.getShutdownNotifier().addShutdownListener( this );

		GuiUtils.makeAnimatedLabel( messagelabel, 4, 2 );

		DataEngine engine = MainApp.getEngine();

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
			// String jidstr = prefs.get( JNL_SELECTED, null );
			Map<Account, TreeItem<Account>> treemap
					= GuiUtils.makeAccountTree( acctman.getParentMap(), root );
			for ( Map.Entry<Account, TreeItem<Account>> en : treemap.entrySet() ) {
				en.getValue().setExpanded( true );

				if ( en.getKey().getId().equals( selected ) ) {
					toselect1 = en.getValue();
				}
			}

			favoriteAcct.textProperty().bind( Bindings.createStringBinding( () -> {
				TreeItem<Account> item = accounts.getSelectionModel().getSelectedItem();
				if ( null == item ) {
					return "Add to Favorites";
				}

				Account acct = item.getValue();
				return ( isFavorite( acct )
						? "Remove from Favorites"
						: "Add to Favorites" );
			}, accounts.getSelectionModel().selectedItemProperty(), favorites ) );

			ToggleGroup favebg = new ToggleGroup();
			final Map<Account, ToggleButton> faves = new HashMap<>();

			favorites.addListener( new ListChangeListener<Account>() {
				@Override
				public void onChanged( ListChangeListener.Change<? extends Account> c ) {
					while ( c.next() ) {
						if ( c.wasAdded() ) {
							for ( Account a : c.getAddedSubList() ) {
								ToggleButton btn = new ToggleButton( a.getName() );
								faves.put( a, btn );
								btn.prefWidthProperty().bind( favoritesBox.widthProperty() );
								btn.prefHeightProperty().bind( favoritesBox.heightProperty().divide( Bindings.size( favorites ) ) );
								btn.setToggleGroup( favebg );
								favoritesBox.getChildren().add( btn );

								btn.setOnAction( ( ActionEvent event ) -> {
									TreeItem<Account> ti = findItem( a );
									select( ti.getValue() );
								} );
							}
						}
						else if ( c.wasRemoved() ) {
							for ( Account a : c.getRemoved() ) {
								favoritesBox.getChildren().remove( faves.get( a ) );
							}
						}
					}
				}
			} );

			fetchFavorites( prefs, acctman );

			Map<Account, ToggleButton> pops = new HashMap<>();
			List<Account> accts = acctman.getPopularAccounts( 10 );
			ToggleGroup topxbg = new ToggleGroup();
			for ( Account a : accts ) {
				ToggleButton btn = new ToggleButton( a.getName() );
				btn.prefWidthProperty().bind( topxbox.widthProperty() );
				btn.prefHeightProperty().bind( topxbox.heightProperty().divide( accts.size() ) );
				btn.setToggleGroup( topxbg );
				topxbox.getChildren().add( btn );
				pops.put( a, btn );

				btn.setOnAction( ( ActionEvent event ) -> {
					TreeItem<Account> ti = findItem( a );
					select( ti.getValue() );
				} );
			}

			accounts.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Account>>() {

				@Override
				public void changed( ObservableValue<? extends TreeItem<Account>> ov,
						TreeItem<Account> oldsel, TreeItem<Account> newsel ) {
					if ( null == newsel ) {
						return;
					}
					Account acct = newsel.getValue();
					if ( faves.containsKey( acct ) ) {
						faves.get( acct ).setSelected( true );
					}
					else {
						favebg.selectToggle( null );
					}

					if ( pops.containsKey( acct ) ) {
						pops.get( acct ).setSelected( true );
					}
					else {
						topxbg.selectToggle( null );
					}
				}
			} );

			List<Recurrence> recs = engine.getRecurrenceMapper().getDue( new Date() );

			for ( Recurrence r : recs ) {
				MenuItem mi = new MenuItem( String.format( "%s (%s)", r.getName(),
						DateFormat.getDateInstance( DateFormat.SHORT ).format( r.getNextRun() ) ) );
				recurbtn.getItems().add( mi );

				mi.setOnAction( event -> {
					try {
						engine.getRecurrenceMapper().execute( r );
						recurbtn.getItems().remove( mi );

						int sz = recurbtn.getItems().size();
						recurbtn.setText( String.format( "Run %d Recurrence%s", sz,
								1 == sz ? "" : "s" ) );
					}
					catch ( MapperException mex ) {
						log.error( mex, mex );
					}
				} );
			}

			int sz = recurbtn.getItems().size();
			recurbtn.setText( String.format( "Run %d Recurrence%s", sz,
					1 == sz ? "" : "s" ) );
			recurbtn.disableProperty().bind( Bindings.isEmpty( recurbtn.getItems() ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		log.debug( "creating accounts tree" );
		final TreeItem<Account> toselect = toselect1;

		accountName.setCellValueFactory( ( CellDataFeatures<Account, String> p )
				-> p.getValue().getValue().getNameProperty() );

		accountBalance.setCellValueFactory( ( CellDataFeatures<Account, Money> p )
				-> acctman.getCurrentProperty( p.getValue().getValue() ) );
		accountBalance.setCellFactory( new MoneyTableTreeCellFactory<>() );

		root.setExpanded( true );
		accounts.setRoot( root );

		try {
			splitter.getItems().add( makeTransactionViewer( transactions ) );
		}
		catch ( IOException ioe ) {
			log.fatal( ioe, ioe );
		}

		makeListeners( acctman, engine, root );
		log.debug( "done making listeners" );

		Platform.runLater( new Runnable() {
			@Override
			public void run() {
				log.debug( "running later" );
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

				ReadOnlyObjectProperty<TreeItem<Account>> treesel
						= accounts.getSelectionModel().selectedItemProperty();

				treesel.addListener( new InvalidationListener() {
					@Override
					public void invalidated( Observable observable ) {
						handleTreeSelection( treesel.get() );
					}
				} );

				// select and scroll after the sorting is complete
				if ( null != toselect ) {
					accounts.getSelectionModel().select( toselect );
					accounts.scrollTo( accounts.getRow( toselect ) );
				}

				log.debug( "done running later" );
			}
		} );
	}

	private void handleTreeSelection( TreeItem<Account> treesel ) {
		transnum.textProperty().unbind();
		balcurr.textProperty().unbind();
		balrec.textProperty().unbind();
		Account acct = treesel.getValue();
		if ( null == acct ) {
			seltrans = null;
			transnum.setText( "0 Transactions" );
			balrec.setText( "" );
			balcurr.setText( "" );
		}
		else {
			log.debug( "tree selected account: " + acct );
			seltrans = stubman.getSplitStubs( MainApp.PF.account( acct ) );
			balcurr.textProperty().bind( Bindings.convert( acctman.getCurrentProperty( acct ) ) );
			balrec.textProperty().bind( Bindings.convert( acctman.getRecoProperty( acct ) ) );

			transnum.textProperty().bind( Bindings.createStringBinding( new Callable<String>() {
				@Override
				public String call() throws Exception {
					int size = stubman.getSplitStubs().filtered( MainApp.PF.account( acct ) ).size();
					return ( size + " " + ( 1 == size ? " Transaction"
							: " Transactions" ) );
				}
			}, Bindings.size( seltrans ) ) );

		}
	}

	private void makeListeners( AccountManager acctmgr, DataEngine eng, TreeItem<Account> root ) {
		log.debug( "making listeners" );
		accounts.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Account>>() {

			@Override
			public void changed( ObservableValue<? extends TreeItem<Account>> ov,
					TreeItem<Account> oldsel, TreeItem<Account> newsel ) {
				if ( null == newsel ) {
					return;
				}
				Account acct = newsel.getValue();
				transactions.setAccount( acct );
				acctname.textProperty().bind( acct.getNameProperty() );
				MainApp.getShutdownNotifier().getStage().
						titleProperty().bind( Bindings.concat( "JFXHacc - ",
								GuiUtils.getFullNameProperty( acct, acctman ) ) );
				recoBtn.setDisable( false );
			}
		} );

		acctman.getAll().addListener( new ListChangeListener<Account>() {
			@Override
			public void onChanged( ListChangeListener.Change<? extends Account> c ) {
				while ( c.next() ) {
					if ( c.wasAdded() ) {
						for ( Account a : c.getAddedSubList() ) {
							Account parent = acctman.getParent( a );
							TreeItem<Account> pnode = findItem( parent );
							pnode.getChildren().add( new TreeItem<>( a ) );
						}
					}
					else if ( c.wasRemoved() ) {
						for ( Account a : c.getRemoved() ) {
							TreeItem<Account> pnode = findItem( a );
							pnode.getParent().getChildren().remove( pnode );
						}
					}
				}
			}
		} );
	}

	public void select( Account acct ) {
		TreeItem<Account> ti = findItem( acct );
		TreeItem parent = ti.getParent();
		while ( null != parent ) {
			parent.setExpanded( true );
			parent = parent.getParent();
		}

		if ( !accounts.getRoot().equals( ti ) ) {
			int row = accounts.getRow( ti );
			accounts.getSelectionModel().select( ti );
			accounts.scrollTo( row );
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
				= new RecurringTransactionWindowController( MainApp.getEngine(), acctman );
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
	public void openloans() {
		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/LoanWindow.fxml" ) );
		LoanWindowController cnt = new LoanWindowController( MainApp.getEngine(), acctman );
		loader.setController( cnt );

		try {
			Parent root = loader.load();

			Stage stage = new Stage();
			stage.setTitle( "Loans" );
			stage.setScene( new Scene( root ) );
			cnt.setStage( stage );

			StageRememberer mem = new StageRememberer( stage, "loan" );
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
					= new ReconcileWindowController( acctman.getRecoProperty( acct ),
							MainApp.getEngine().getTransactionMapper(), stubman );

			loader.setController( controller );
			Parent root = loader.load();
			controller.setAccount( acct );

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
		AccountDetailsController acd
				= new AccountDetailsController( acct, MainApp.getEngine(), acctman );
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
		log.debug( "making transaction viewer" );
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
		messagelabel.setText( message );
	}

	@FXML
	protected void newjnl() {
		TextInputDialog dialog = new TextInputDialog( "New Journal" );
		dialog.setTitle( "Create Journal" );
		dialog.setHeaderText( "Enter a name to create a new Journal" );
		dialog.setContentText( "New Journal name:" );

		Optional<String> result = dialog.showAndWait();
		result.ifPresent( name -> {
			try {
				MainApp.getEngine().getJournalMapper().create( name );
			}
			catch ( MapperException me ) {
				log.error( me, me );
			}
		} );
	}

	@FXML
	protected void editjnl() {
		JournalsWindowController controller = new JournalsWindowController( MainApp.getEngine() );
		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/JournalsWindow.fxml" ) );
		loader.setController( controller );
		try {
			Scene scene = new Scene( loader.load() );
			Stage stage = new Stage();
			stage.setTitle( "Edit Journals" );
			stage.setScene( scene );
			controller.setStage( stage );
			stage.show();
		}
		catch ( IOException ioe ) {
			log.error( ioe, ioe );
		}
	}

	@FXML
	public void runRecurrences() {
		List<MenuItem> items = new ArrayList<>( recurbtn.getItems() );
		for ( MenuItem mi : items ) {
			mi.fire();
		}
	}

	@FXML
	public void openBalance() {
		openChart( new AccountBalanceMaker( MainApp.getEngine().getAccountMapper(),
				MainApp.getEngine().getTransactionMapper() ), ChartType.AREA );
	}

	@FXML
	public void openDelta() {
		openChart( new AccountDeltaMaker( stubman ), ChartType.AREA );
	}

	@FXML
	public void openCreditsPie() {
		openChart( new PieMaker( true, MainApp.getEngine().getAccountMapper(),
				MainApp.getEngine().getTransactionMapper() ), ChartType.PIE );
	}

	@FXML
	public void openDebitsPie() {
		openChart( new PieMaker( false, MainApp.getEngine().getAccountMapper(),
				MainApp.getEngine().getTransactionMapper() ), ChartType.PIE );
	}

	private void openChart( SeriesMaker maker, ChartType type ) {
		ChartController controller
				= new ChartController( MainApp.getEngine(), type, maker );

		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/ChartWindow.fxml" ) );
		loader.setController( controller );
		try {
			Scene scene = new Scene( loader.load() );
			Stage stage = new Stage();
			stage.setTitle( maker.getTitle() );
			stage.setScene( scene );
			controller.setStage( stage );
			stage.show();
		}
		catch ( IOException ioe ) {
			log.error( ioe, ioe );
		}
	}

	@FXML
	public void toggleFavorite( ActionEvent event ) {
		Account acct = ( null == event
				? null : accounts.getSelectionModel().getSelectedItem().getValue() );
		Preferences prefs = Preferences.userNodeForPackage( getClass() );

		// see if we're removing the account
		if ( isFavorite( acct ) ) {
			favorites.remove( acct );
		}
		else {
			favorites.add( acct );
		}

		// write the preferences for next startup
		StringBuilder sb = new StringBuilder();
		for ( Account a : favorites ) {
			if ( 0 != sb.length() ) {
				sb.append( " " );
			}
			sb.append( a.getId() );
		}
		prefs.put( PREF_FAVORITES, sb.toString() );
	}

	private void fetchFavorites( Preferences prefs, AccountManager aman ) {
		String faves = prefs.get( PREF_FAVORITES, "" );
		for ( String aid : faves.split( " " ) ) {
			aid = aid.trim();
			if ( !aid.isEmpty() ) {
				Account acct = aman.get( new URIImpl( aid ) );
				if ( null != acct ) {
					favorites.add( acct );
				}
			}
		}
	}

	private boolean isFavorite( Account a ) {
		return favorites.contains( a );
	}
}
