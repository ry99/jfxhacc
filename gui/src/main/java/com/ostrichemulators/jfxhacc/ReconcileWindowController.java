/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class ReconcileWindowController {

	private static final Logger log = Logger.getLogger( ReconcileWindowController.class );
	@FXML
	private DatePicker stmtdate;
	@FXML
	private TextField stmtbal;
	@FXML
	private Label openbal;
	@FXML
	private Label diff;
	@FXML
	private Label stmtdiff;
	@FXML
	private Label recodiff;
	@FXML
	private BorderPane borders;

	private Stage stage;
	private Account account;
	private Journal journal;
	private final ReconcileViewController transviewer = new ReconcileViewController();

	/**
	 * Initializes the controller class.
	 */
	@FXML
	public void initialize() {
		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/TransactionViewer.fxml" ) );
		loader.setController( transviewer );
		try {
			Node table = loader.load();

			borders.setCenter( table );
			stmtdate.setValue( LocalDate.now() );
		}
		catch ( IOException ioe ) {
			log.fatal( ioe, ioe );
		}

		transviewer.getData().addListener( new ListChangeListener<Transaction>() {

			@Override
			public void onChanged( ListChangeListener.Change<? extends Transaction> change ) {
				updateCalculation( Money.valueOf( openbal.getText() ) );
			}
		} );

	}

	public void setAccount( Account a, Journal j ) {
		account = a;
		journal = j;
		updateParams();
	}

	@FXML
	private void updateParams() {
		AccountMapper amap = MainApp.getEngine().getAccountMapper();
		Instant instant = Instant.from( stmtdate.getValue().atStartOfDay( ZoneId.systemDefault() ) );
		Date d = Date.from( instant );
		Date lastdate = transviewer.getDate();

		Money openingBal = amap.getBalance( account,
				AccountMapper.BalanceType.RECONCILED, d );
		openbal.setText( openingBal.toString() );
		if ( d.equals( lastdate ) ) {
			updateCalculation( openingBal );
		}
		else {
			transviewer.setAccount( account, journal, d );
		}
	}

	private void updateCalculation( Money openingbal ) {
		Money opening = Money.valueOf( openbal.getText() );
		Money ending = Money.valueOf( stmtbal.getText() );

		int calc = 0;
		for ( Split s : transviewer.getSplits() ) {
			if ( ReconcileState.CLEARED == s.getReconciled() ) {
				int cents = s.getValue().value();
				if ( s.isCredit() ) {
					calc += cents;
				}
				else {
					calc -= cents;
				}
			}
		}

		if ( account.getAccountType().isDebitPlus() ) {
			calc = 0 - calc;
		}

		Money sdiff = ending.minus( opening );
		Money rdiff = new Money( calc );
		stmtdiff.setText( sdiff.toString() );
		recodiff.setText( rdiff.toString() );
		diff.setText( sdiff.minus( rdiff ).toString() );
	}

	@FXML
	public void newtrans( ActionEvent event ) {
		Instant instant = Instant.from( stmtdate.getValue().atStartOfDay( ZoneId.systemDefault() ) );
		transviewer.openEditor( Date.from( instant ), Split.ReconcileState.CLEARED );
	}

	@FXML
	public void balance( ActionEvent event ) {
		TransactionMapper tmap = MainApp.getEngine().getTransactionMapper();
		Split split = tmap.create( Money.valueOf( diff.getText() ),
				"balance adjustment", transviewer.getDefaultReconcileState() );
		Instant instant = Instant.from( stmtdate.getValue().atStartOfDay( ZoneId.systemDefault() ) );
		transviewer.openEditor( Date.from( instant ), split );
	}

	@FXML
	public void cancel( ActionEvent event ) {
		transviewer.shutdown();
		stage.close();
	}

	@FXML
	public void save( ActionEvent event ) {
		transviewer.upgradeSplits();
		transviewer.shutdown();
		stage.close();
	}

	public void setStage( Stage s ) {
		stage = s;
	}
}
