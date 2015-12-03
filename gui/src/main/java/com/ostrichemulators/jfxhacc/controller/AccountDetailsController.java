/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.cells.MoneyStringConverter;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.impl.AccountImpl;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import com.ostrichemulators.jfxhacc.utility.TreeNode;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class AccountDetailsController {

	public static final Logger log = Logger.getLogger( AccountDetailsController.class );

	@FXML
	private TextArea notes;
	@FXML
	private TextField name;
	@FXML
	private TextField number;
	@FXML
	private TextField balance;
	@FXML
	private ChoiceBox<AccountType> type;
	@FXML
	private ComboBox<Account> parentacct;
	@FXML
	private Button cancelBtn;
	private EventHandler<ActionEvent> okhandler;
	private final Account acct;
	private final DataEngine engine;

	public AccountDetailsController( Account acct, DataEngine eng ) {
		this.acct = ( null == acct ? null : new AccountImpl( acct ) );
		this.engine = eng;
	}

	@FXML
	public void initialize() {
		type.getItems().addAll( AccountType.values() );

		List<Account> accounts = new ArrayList<>();
		try {
			accounts.addAll( engine.getAccountMapper().getAll() );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		if ( null == acct ) {
			type.getSelectionModel().select( AccountType.ASSET );
		}
		else {
			balance.textProperty().bindBidirectional( acct.getOpeningBalanceProperty(),
					new MoneyStringConverter() );
			name.textProperty().bindBidirectional( acct.getNameProperty() );
			type.setDisable( true );
			type.getSelectionModel().select( acct.getAccountType() );
			notes.textProperty().bindBidirectional( acct.getNotesProperty() );
			number.textProperty().bindBidirectional( acct.getNumberProperty() );

			try {
				TreeNode<Account> tree = engine.getAccountMapper().getAccounts( null );
				TreeNode<Account> parent = tree.findChild( acct );

				accounts.removeAll( parent.findChild( acct ).getAllChildren() );
				accounts.remove( acct );
			}
			catch ( MapperException me ) {
				log.error( me, me );
			}
		}

		try {
			GuiUtils.makeAccountCombo( parentacct, accounts, engine.getAccountMapper() );
			Account pp = engine.getAccountMapper().getParent( acct );
			parentacct.getSelectionModel().select( pp );
			parentacct.setValue( pp );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	public void setOkListener( EventHandler<ActionEvent> event ) {
		okhandler = event;
	}

	public void setCancelAction( EventHandler<ActionEvent> event ) {
		cancelBtn.setOnAction( event );
	}

	@FXML
	public void okPressed( ActionEvent ev ) {
		// do the save
		try {
			if ( null == acct ) {
				engine.getAccountMapper().create( name.getText(), type.getValue(),
						Money.valueOf( balance.getText() ), number.getText(), notes.getText(),
						parentacct.getValue() );
			}
			else {
				engine.getAccountMapper().update( acct );
			}

			if ( null != okhandler ) {
				okhandler.handle( ev );
			}
		}

		catch ( MapperException e ) {
			log.error( e, e );
		}
	}
}
