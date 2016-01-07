/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.cells.RecurrenceListViewCellFactory;
import com.ostrichemulators.jfxhacc.converter.MoneyStringConverter;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Loan;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.impl.LoanImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Loans;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.io.IOException;
import java.util.Collection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
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
	@FXML
	private ListView<Recurrence> list;

	private ScheduleDataWindowController scheduledata;
	private final DataEngine engine;
	private Stage stage;
	private Recurrence recurrence;
	private Loan loan;

	public LoanWindowController( DataEngine eng ) {
		engine = eng;
	}

	public void setStage( Stage s ) {
		stage = s;
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

			Collection<Recurrence> recs = engine.getRecurrenceMapper().getAll( Loans.TYPE );
			list.getItems().setAll( recs );
			list.setCellFactory( new RecurrenceListViewCellFactory() );
			list.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<Recurrence>() {

				@Override
				public void changed( ObservableValue<? extends Recurrence> ov,
						Recurrence t, Recurrence t1 ) {
					setCurrent( t1 );
				}
			} );

		}
		catch ( IOException | MapperException ioe ) {
			log.error( ioe, ioe );
		}
	}

	@FXML
	void newLoan( ActionEvent event ) {
		try {
			Recurrence r = engine.getRecurrenceMapper().create( scheduledata.getRecurrence(),
					new LoanImpl() );
			setCurrent( r );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	@FXML
	void removeLoan( ActionEvent event ) {
		try {
			engine.getRecurrenceMapper().remove( recurrence );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	private void setCurrent( Recurrence r ) {
		recurrence = r;
		try {
			loan = engine.getLoanMapper().get( r );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		amount.textProperty().unbind();
		amount.textProperty().bindBidirectional( loan.getValueProperty(),
				new MoneyStringConverter() );

		numpayments.textProperty().unbind();
		numpayments.textProperty().bindBidirectional( loan.getNumberOfPaymentsProperty(),
				new NumberStringConverter() );

		rate.textProperty().unbind();
		rate.textProperty().bindBidirectional( loan.getAprProperty(),
				new NumberStringConverter() );

		name.textProperty().unbind();
		name.textProperty().bindBidirectional( r.getNameProperty() );
		scheduledata.setRecurrence( r );
	}

	@FXML
	public void save() {
		try {
			engine.getLoanMapper().update( loan );
			Recurrence rec = scheduledata.getRecurrence();
			rec.setId( recurrence.getId() );
			engine.getRecurrenceMapper().update( rec );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	@FXML
	public void close() {
		stage.close();
	}
}
