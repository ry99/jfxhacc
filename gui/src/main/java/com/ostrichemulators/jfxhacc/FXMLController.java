package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.cells.MoneyTableTreeCellFactory;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
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

	private static final String PREF_SELECTED = "selected";
	private static final String PREF_ASIZE = "account-width";
	private static final String PREF_SPLITTER = "splitter-location";

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

	@Override
	public void initialize( URL url, ResourceBundle rb ) {
		MainApp.getShutdownNotifier().addShutdownListener( this );

		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		String selidstr = prefs.get( PREF_SELECTED, "" );
		URI selected = ( selidstr.isEmpty() ? null : new URIImpl( selidstr ) );

		final TreeItem<Account> root = new TreeItem<>();

		accordion.setExpandedPane( accountsPane );
		TreeItem<Account> toselect = null;
		AccountMapper amap = MainApp.getEngine().getAccountMapper();
		try {
			for ( Account acct : amap.getAll() ) {
				TreeItem<Account> aitem = new TreeItem<>( acct );
				root.getChildren().add( aitem );

				if ( acct.getId().equals( selected ) ) {
					toselect = aitem;
				}
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
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
			public void changed( ObservableValue<? extends TreeItem<Account>> ov, TreeItem<Account> oldsel, TreeItem<Account> newsel ) {
				transactions.setAccount( newsel.getValue() );
			}
		} );

		double asize = prefs.getDouble( PREF_ASIZE, 0.5 );
		accountName.setPrefWidth( asize );
		accountBalance.setPrefWidth( accounts.getWidth() - asize );

		accounts.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
		if ( null != toselect ) {
			accounts.getSelectionModel().select( toselect );
		}

		double splitterpos = prefs.getDouble( PREF_SPLITTER, 100 );
		splitter.setDividerPosition( 0, splitterpos );
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
}
