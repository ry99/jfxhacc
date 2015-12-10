/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.converter.JournalStringConverter;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.io.IOException;
import java.util.Collection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class RecurringTransactionWindowController {

	private static final Logger log = Logger.getLogger( RecurringTransactionWindowController.class );

	@FXML
	private SplitPane splitter;
	@FXML
	private ListView<Recurrence> list;
	@FXML
	private TextField label;
	@FXML
	private ChoiceBox<Journal> journal;
	@FXML
	private TextField payee;
	@FXML
	private TextField number;
	@FXML
	private AnchorPane splitsarea;

	private Stage stage;
	private final DataEngine engine;
	private ScheduleDataWindowController scheduledata;
	private SplitsWindowController splitdata;

	public RecurringTransactionWindowController( DataEngine eng ) {
		engine = eng;
	}

	@FXML
	public void initialize() {
		FXMLLoader loader
				= new FXMLLoader( getClass().getResource( "/fxml/ScheduleDataWindow.fxml" ) );
		scheduledata = new ScheduleDataWindowController( engine );
		loader.setController( scheduledata );

		FXMLLoader l2
				= new FXMLLoader( getClass().getResource( "/fxml/SplitsWindow.fxml" ) );
		splitdata = new SplitsWindowController( engine );
		l2.setController( splitdata );

		try {
			splitter.getItems().add( loader.load() );
			Node spls = l2.load();
			splitsarea.getChildren().add( spls );
			splitdata.hideOkWhenBalanced( true );
			AnchorPane.setBottomAnchor( spls, 0d );
			AnchorPane.setTopAnchor( spls, 0d );
			AnchorPane.setRightAnchor( spls, 0d );
			AnchorPane.setLeftAnchor( spls, 0d );

			Collection<Recurrence> recs = engine.getRecurrenceMapper().getAll();
			list.getItems().setAll( recs );
			list.setCellFactory( new Callback<ListView<Recurrence>, ListCell<Recurrence>>() {

				@Override
				public ListCell<Recurrence> call( ListView<Recurrence> p ) {
					return new ListCell<Recurrence>() {

						@Override
						protected void updateItem( Recurrence item, boolean empty ) {
							super.updateItem( item, empty );
							if ( !( null == item || empty ) ) {
								super.setText( item.getName() );
							}
						}
					};
				}
			} );

			list.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<Recurrence>() {

				@Override
				public void changed( ObservableValue<? extends Recurrence> ov,
						Recurrence t, Recurrence t1 ) {
					setCurrent( t1 );
				}
			} );

			Collection<Journal> journals = engine.getJournalMapper().getAll();
			journal.getItems().setAll( journals );
			journal.setConverter( new JournalStringConverter( journals ) );
			if ( 1 == journals.size() ) {
				journal.setValue( journal.getItems().get( 0 ) );
				journal.setDisable( true );
			}
		}
		catch ( IOException | MapperException e ) {
			log.error( e, e );
		}
	}

	public void setStage( Stage s ) {
		stage = s;
	}

	@FXML
	public void close() {
		stage.close();
	}

	@FXML
	public void save() {
		Recurrence rec = scheduledata.getRecurrence();
		Recurrence curr = list.getSelectionModel().getSelectedItem();
		curr.setFrequency( rec.getFrequency() );
		curr.setName( label.getText() );
		curr.setNextRun( rec.getNextRun() );
		try {
			engine.getRecurrenceMapper().update( curr );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	@FXML
	public void runnow() {

	}

	private void setCurrent( Recurrence current ) {
		scheduledata.setRecurrence( current );
		label.setText( current.getName() );
		try {
			Transaction t = engine.getTransactionMapper().get( current );

			number.textProperty().bindBidirectional( t.getNumberProperty() );
			payee.setText( t.getPayee().getName() );

			splitdata.setSplits( t.getSplitsProperty() );
		}
		catch ( MapperException n ) {
			log.error( n, n );
		}
	}
}
