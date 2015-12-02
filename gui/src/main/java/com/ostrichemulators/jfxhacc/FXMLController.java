package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.cells.MoneyTableTreeCellFactory;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper.BalanceType;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.utility.AccountBalanceCache;
import com.ostrichemulators.jfxhacc.utility.AccountBalanceCache.MoneyPair;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class FXMLController implements ShutdownListener {

	private static final String PREF_SELECTED = "accountviewer.selected";
	private static final String PREF_ASIZE = "accountviewer.account-width";
	private static final String PREF_SPLITTER = "stage.splitter-location";

	private static final Logger log = Logger.getLogger( FXMLController.class );
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
	private Label acctname;
	@FXML
	private Button recoBtn;

	private final TransactionViewController transactions = new TransactionViewController();
	private AccountBalanceCache acb;
	private Journal journal;

	@FXML
	public void initialize() {
		MainApp.getShutdownNotifier().addShutdownListener( this );

		DataEngine engine = MainApp.getEngine();
		AccountMapper amap = engine.getAccountMapper();

		acb = new AccountBalanceCache( amap, engine.getTransactionMapper() );

		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		String selidstr = prefs.get( PREF_SELECTED, "" );
		URI selected = ( selidstr.isEmpty() ? null : new URIImpl( selidstr ) );

		final TreeItem<Account> root = new TreeItem<>();

		accordion.setExpandedPane( accountsPane );
		TreeItem<Account> toselect1 = null;
		try {
			journal = engine.getJournalMapper().getAll().iterator().next();
			toselect1 = retree( amap, root, selected );
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
			private TreeItem<Account> findItem( URI u ) {
				if ( null == u ) {
					return root;
				}

				Deque<TreeItem<Account>> todo = new ArrayDeque<>();
				todo.addAll( root.getChildren() );
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

		Platform.runLater( new Runnable() {
			@Override
			public void run() {

				double asize = prefs.getDouble( PREF_ASIZE, 0.5 );
				accountName.setPrefWidth( asize );

				accounts.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
				if ( null != toselect ) {
					accounts.getSelectionModel().select( toselect );
				}

				double splitterpos = prefs.getDouble( PREF_SPLITTER, 0.25 );
				splitter.setDividerPositions( splitterpos );
			}
		} );
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
			balrec.setText( curr.toString() + "/" + rec.toString() + " R" );
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
		prefs.putDouble( PREF_SPLITTER, splitter.getDividerPositions()[0] );
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
			ReconcileWindowController controller = new ReconcileWindowController();

			loader.setController( controller );
			Parent root = loader.load();
			controller.setAccount( acct, journal );

			Stage stage = new Stage();
			stage.setTitle( "Reconcile " + acct.getName() );
			stage.setScene( new Scene( root ) );
			controller.setStage( stage );
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
}
