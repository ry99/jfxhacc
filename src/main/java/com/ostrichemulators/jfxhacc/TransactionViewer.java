/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class TransactionViewer extends AnchorPane implements Initializable {

	private static final Logger log = Logger.getLogger( TransactionViewer.class );

	@FXML
	private TreeTableView<Transaction> view;
	@FXML
	private TreeTableColumn<Transaction, Date> date;
	@FXML
	private TreeTableColumn<Transaction, String> number;
	@FXML
	private TreeTableColumn<Transaction, String> payee;
	@FXML
	private TreeTableColumn<Transaction, String> memo;
	@FXML
	private TreeTableColumn<Transaction, Money> credit;
	@FXML
	private TreeTableColumn<Transaction, Money> debit;
	@FXML
	private TreeTableColumn<Transaction, ReconcileState> reco;
	@FXML
	private AnchorPane dataentry;
	@FXML
	private Label accountLabel;
	@FXML
	private Button splitsBtn;
	@FXML
	private ComboBox accountsCmb;
	@FXML
	private ComboBox payeeCmb;

	private Account account;

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
		refresh();
	}

	public void refresh() {
		view.getRoot().getChildren().clear();

		DataEngine engine = MainApp.getEngine();
		try {
			List<Transaction> trans = engine.getTransactionMapper().getAll( account );
			for ( Transaction t : trans ) {
				view.getRoot().getChildren().add( new TreeItem<>( t ) );
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	@Override
	public void initialize( URL url, ResourceBundle rb ) {
		final TreeItem<Transaction> root = new TreeItem<>();
		root.setExpanded( true );
		view.setRoot( root );

		date.setCellValueFactory( ( TreeTableColumn.CellDataFeatures<Transaction, Date> p )
				-> new ReadOnlyObjectWrapper<>( p.getValue().getValue().getDate() ) );

		payee.setCellValueFactory( ( TreeTableColumn.CellDataFeatures<Transaction, String> p )
				-> new ReadOnlyStringWrapper( p.getValue().getValue().getPayee().getName() ) );
	}
}
