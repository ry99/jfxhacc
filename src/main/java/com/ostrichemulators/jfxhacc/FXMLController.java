package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import org.apache.log4j.Logger;

public class FXMLController implements Initializable {

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
		final TreeItem<Account> root = new TreeItem<>();

		accordion.setExpandedPane( accountsPane );

		AccountMapper amap = MainApp.getEngine().getAccountMapper();
		try {
			for ( Account acct : amap.getAll() ) {
				root.getChildren().add( new TreeItem<>( acct ) );
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

		root.setExpanded( true );
		accounts.setRoot( root );

		transactions = new TransactionViewer();
		splitter.getItems().add( transactions );

	}
}
