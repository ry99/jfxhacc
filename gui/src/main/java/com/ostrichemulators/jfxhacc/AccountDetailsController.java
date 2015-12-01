/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.cells.MoneyStringConverter;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
	private EventHandler<ActionEvent> ae;
	private final Account acct;
	private final DataEngine engine;

	public AccountDetailsController( Account acct, DataEngine eng ) {
		this.acct = acct;
		this.engine = eng;
	}

	@FXML
	public void initialize() {

		if ( null == acct ) {
			type.getItems().addAll( AccountType.values() );

		}
		else {
			balance.textProperty().bindBidirectional( acct.getOpeningBalanceProperty(),
					new MoneyStringConverter() );
			name.textProperty().bindBidirectional( acct.getNameProperty() );
			type.getItems().add( acct.getAccountType() );
			notes.textProperty().bindBidirectional( acct.getNotesProperty() );
			number.textProperty().bindBidirectional( acct.getNumberProperty() );
			// parent
		}
	}

	public void setOkListener( EventHandler<ActionEvent> event ) {
		ae = event;
	}

	@FXML
	public void okPressed( ActionEvent ev ) {
		// do the save
		try {
			if ( null == acct ) {
				engine.getAccountMapper().create( name.getText(), type.getValue(),
						Money.valueOf( balance.getText() ), number.getText(), notes.getText(),
						null );
			}
			else {
				engine.getAccountMapper().update( acct );
			}

			if ( null != ae ) {
				ae.handle( ev );
			}
		}

		catch ( MapperException e ) {
			log.error( e, e );
		}
	}
}
