package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.cells.MoneyTableTreeCellFactory;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper.BalanceType;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class FXMLController implements Initializable, ShutdownListener {

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
	private TransactionViewer transactions;
	@FXML
	private Label balrec;

	@FXML
	private Label acctname;

	@Override
	public void initialize( URL url, ResourceBundle rb ) {
		MainApp.getShutdownNotifier().addShutdownListener( this );

		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		String selidstr = prefs.get( PREF_SELECTED, "" );
		URI selected = ( selidstr.isEmpty() ? null : new URIImpl( selidstr ) );

		final TreeItem<Account> root = new TreeItem<>();

		accordion.setExpandedPane( accountsPane );
		TreeItem<Account> toselect1 = null;
		AccountMapper amap = MainApp.getEngine().getAccountMapper();
		Map<Account, TreeItem<Account>> items = new HashMap<>();
		Map<Account, Account> childparentlkp = new HashMap<>();
		try {
			childparentlkp.putAll( amap.getParentMap() );
			for ( Account acct : childparentlkp.keySet() ) {
				TreeItem<Account> aitem = new TreeItem<>( acct );
				items.put( acct, aitem );

				if ( acct.getId().equals( selected ) ) {
					toselect1 = aitem;
				}
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		for ( Map.Entry<Account, Account> en : childparentlkp.entrySet() ) {
			Account child = en.getKey();
			Account parent = en.getValue();
			TreeItem<Account> childitem = items.get( child );
			TreeItem<Account> parentitem
					= ( null == parent ? root : items.get( parent ) );
			parentitem.getChildren().add( childitem );
		}

		final TreeItem<Account> toselect = toselect1;

		accountName.setCellValueFactory( ( CellDataFeatures<Account, String> p )
				-> new ReadOnlyStringWrapper( p.getValue().getValue().getName() ) );

		accountBalance.setCellValueFactory( ( CellDataFeatures<Account, Money> p )
				-> new ReadOnlyObjectWrapper<>( amap.getBalance( p.getValue().getValue(),
								AccountMapper.BalanceType.CURRENT ) ) );
		accountBalance.setCellFactory( new MoneyTableTreeCellFactory() );

		root.setExpanded( true );
		accounts.setRoot( root );

		transactions = new TransactionViewer();
		splitter.getItems().add( transactions );

		accounts.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Account>>() {

			@Override
			public void changed( ObservableValue<? extends TreeItem<Account>> ov,
					TreeItem<Account> oldsel, TreeItem<Account> newsel ) {
				Account acct = newsel.getValue();
				transactions.setAccount( acct );
				String fname = GuiUtils.getFullName( acct, amap );
				acctname.setText( fname );
				MainApp.getShutdownNotifier().getStage().setTitle( fname );

				Money curr = amap.getBalance( acct, BalanceType.CURRENT );
				Money rec = amap.getBalance( acct, BalanceType.RECONCILED );
				balrec.setText( curr.toString() + "/" + rec.toString() + " R" );
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
}
