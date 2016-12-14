/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.Callable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
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
	private Label deposits;
	@FXML
	private Label withdrawals;
	@FXML
	private Button balanceBtn;
	@FXML
	private BorderPane borders;

	private Stage stage;
	private Account account;
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

		ObservableList<Split> minuses = transviewer.getClearedCredits();
		minuses.addListener( new InvalidationListener() {

			@Override
			public void invalidated( Observable o ) {
				int val = 0;
				for ( Split s : minuses ) {
					val += s.getValue().value();
				}
				withdrawals.setText( new Money( val ).toString() );
			}
		} );

		ObservableList<Split> pluses = transviewer.getClearedDebits();
		pluses.addListener( new InvalidationListener() {

			@Override
			public void invalidated( Observable o ) {
				int val = 0;
				for ( Split s : pluses ) {
					val += s.getValue().value();
				}
				deposits.setText( new Money( val ).toString() );
			}
		} );

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
				Money minuses = Money.valueOf( withdrawals.getText() );
				Money pluses = Money.valueOf( deposits.getText() );

				Money recdiff = opening.plus( pluses ).minus( minuses );

				Money stmt = Money.valueOf( stmtbal.getText() );
				Money stmtdiff = recdiff.minus( stmt );
				log.debug( String.format( "opening/plus/minus: %s/%s/%s", opening, pluses, minuses ) );
				log.debug( String.format( "  stmt/recdiff/stmtdiff: %s/%s/%s", stmt, recdiff, stmtdiff ) );

				return stmtdiff.toString();
			}
		},
				openbal.textProperty(), stmtbal.textProperty(), withdrawals.textProperty(),
				deposits.textProperty(), transviewer.getClearedValueProperty() ) );
	}

	public void setAccount( Account a ) {
		account = a;
		updateParams();
	}

	@FXML
	private void updateParams() {
		Instant instant = Instant.from( stmtdate.getValue().atStartOfDay( ZoneId.systemDefault() ) );
		Date d = Date.from( instant );
		Date lastdate = transviewer.getDate();

		if ( !d.equals( lastdate ) ) {
			transviewer.setAccount( account, d );
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
