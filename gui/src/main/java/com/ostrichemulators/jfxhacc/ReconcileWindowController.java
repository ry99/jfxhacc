/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Split;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
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
	private Button newtrans;
	@FXML
	private Label diff;
	@FXML
	private BorderPane borders;

	private Stage stage;
	private Account account;
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
	}

	public void setAccount( Account a, Journal j ) {
		account = a;
		updateParams();
		transviewer.setAccount( a, j );
	}

	@FXML
	private void updateParams() {
		AccountMapper amap = MainApp.getEngine().getAccountMapper();
		Instant instant = Instant.from( stmtdate.getValue().atStartOfDay( ZoneId.systemDefault() ) );
		Date d = Date.from( instant );

		openbal.setText( amap.getBalance( account,
				AccountMapper.BalanceType.RECONCILED, d ).toString() );
		transviewer.setDate( d );
	}

	@FXML
	public void newtrans( ActionEvent event ) {
		transviewer.openEditor( null, Split.ReconcileState.CLEARED );
	}

	@FXML
	public void cancel( ActionEvent event ) {
		stage.close();
	}

	@FXML
	public void save( ActionEvent event ) {
		transviewer.upgradeSplits();
		stage.close();
	}

	public void setStage( Stage s ) {
		stage = s;
	}
}
