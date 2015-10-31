/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class TransactionViewer extends AnchorPane {

	@FXML
	private TreeTableView view;

	@FXML
	private TreeTableColumn date;
	@FXML
	private TreeTableColumn number;
	@FXML
	private TreeTableColumn payee;
	@FXML
	private TreeTableColumn credit;
	@FXML
	private TreeTableColumn debit;
	@FXML
	private TreeTableColumn reco;
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
}
