/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.io.IOException;
import java.util.Collection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class LoanWindowController {

	private static final Logger log = Logger.getLogger( LoanWindowController.class );
	@FXML
	private TextField name;
	@FXML
	private TextField amount;
	@FXML
	private TextField rate;
	@FXML
	private TextField numpayments;
	@FXML
	private Label payment;
	@FXML
	private ComboBox<Account> payacct;
	@FXML
	private ComboBox<Account> prinacct;
	@FXML
	private ComboBox<Account> intacct;
	@FXML
	private AnchorPane schedarea;

	private ScheduleDataWindowController scheduledata;
	private final DataEngine engine;

	public LoanWindowController( DataEngine eng ) {
		engine = eng;
	}

	@FXML
	public void initialize() {
		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/ScheduleDataWindow.fxml" ) );
		scheduledata = new ScheduleDataWindowController( engine );
		loader.setController( scheduledata );
		try {
			Node sched = loader.load();
			schedarea.getChildren().add( sched );
			AnchorPane.setBottomAnchor( sched, 0d );
			AnchorPane.setTopAnchor( sched, 0d );
			AnchorPane.setRightAnchor( sched, 0d );
			AnchorPane.setLeftAnchor( sched, 0d );

			AccountMapper amap = engine.getAccountMapper();
			Collection<Account> accts = amap.getAll();
			GuiUtils.makeAccountCombo( payacct, accts, amap );
			GuiUtils.makeAccountCombo( prinacct, accts, amap );
			GuiUtils.makeAccountCombo( intacct, accts, amap );
		}
		catch ( IOException | MapperException ioe ) {
			log.error( ioe, ioe );
		}
	}

	@FXML
	void newLoan( ActionEvent event ) {

	}

	@FXML
	void removeLoan( ActionEvent event ) {

	}

}
