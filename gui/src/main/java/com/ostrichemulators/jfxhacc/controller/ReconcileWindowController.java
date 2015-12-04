/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.Callable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
	private Button balanceBtn;
	@FXML
	private BorderPane borders;

	private Stage stage;
	private Account account;
	private Journal journal;
	private final ReconcileViewController transviewer = new ReconcileViewController();
	private final ObjectProperty<Money> areco;

	public ReconcileWindowController( ObjectProperty<Money> prop ) {
		areco = prop;
	}

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

		openbal.textProperty().bind( areco.asString() );
		stmtbal.setText( new Money().toString() );
		recodiff.textProperty().bind( transviewer.getClearedValueProperty().asString() );

		balanceBtn.disableProperty().bind( Bindings.createBooleanBinding( new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return Money.valueOf( diff.getText() ).isZero();
			}
		}, diff.textProperty() ) );

		diff.textProperty().bind( Bindings.createStringBinding( new Callable<String>() {

			@Override
			public String call() throws Exception {
				Money opening = Money.valueOf( openbal.getText() );
				Money ending = Money.valueOf( stmtbal.getText() );

				Money sdiff = ending.minus( opening );
				Money rdiff = transviewer.getClearedValueProperty().get();
				stmtdiff.setText( sdiff.toString() );
				return sdiff.minus( rdiff ).toString();
			}
		},
				openbal.textProperty(), stmtbal.textProperty(),
				transviewer.getClearedValueProperty() ) );
	}

	public void setAccount( Account a, Journal j ) {
		account = a;
		journal = j;
		updateParams();
	}

	@FXML
	private void updateParams() {
		Instant instant = Instant.from( stmtdate.getValue().atStartOfDay( ZoneId.systemDefault() ) );
		Date d = Date.from( instant );
		Date lastdate = transviewer.getDate();

		if ( !d.equals( lastdate ) ) {
			transviewer.setAccount( account, journal, d );
		}
	}

	@FXML
	public void newtrans( ActionEvent event ) {
		Instant instant = Instant.from( stmtdate.getValue().atStartOfDay( ZoneId.systemDefault() ) );
		transviewer.openEditor( Date.from( instant ), Split.ReconcileState.CLEARED );
	}

	@FXML
	public void balance( ActionEvent event ) {
		Split split = new SplitImpl();
		split.setValue( Money.valueOf( diff.getText() ).opposite() );
		split.setMemo( "balance adjustment" );
		split.setReconciled( transviewer.getDefaultReconcileState() );
		Instant instant = Instant.from( stmtdate.getValue().atStartOfDay( ZoneId.systemDefault() ) );
		split.setAccount( account );
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
