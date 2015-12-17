/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.converter.JournalStringConverter;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.impl.RecurrenceMapperImpl;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.RecurrenceImpl;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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
	@FXML
	private Label feedback;

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
							if ( null == item || empty ) {
								textProperty().unbind();
								setText( null );
							}
							else {
								textProperty().bind( item.getNameProperty() );
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

			if ( !recs.isEmpty() ) {
				list.getSelectionModel().clearAndSelect( 0 );
			}

			Collection<Journal> journals = engine.getJournalMapper().getAll();
			journal.getItems().setAll( journals );
			journal.setConverter( new JournalStringConverter( journals ) );
			journal.setDisable( 1 == journals.size() );
			journal.setValue( journal.getItems().get( 0 ) );
		}
		catch ( IOException | MapperException e ) {
			log.error( e, e );
		}

		GuiUtils.makeAnimatedLabel( feedback, 2, 1 );
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
			Transaction trans = engine.getTransactionMapper().get( curr );
			Payee pay = engine.getPayeeMapper().createOrGet( payee.getText() );
			trans.setPayee( pay );
			trans.setNumber( number.getText() );
			trans.setJournal( journal.getValue() );
			trans.setSplits( splitdata.getSplits() );

			engine.getRecurrenceMapper().update( curr );
			engine.getTransactionMapper().update( trans );
			feedback.setText( "Saved" );
		}
		catch ( MapperException me ) {
			log.error( me, me );
			feedback.setText( me.getLocalizedMessage() );
		}
	}

	@FXML
	public void runnow() {
		try {
			Recurrence rec = scheduledata.getRecurrence();

			Transaction ti = new TransactionImpl( rec.getNextRun(), number.getText(),
					engine.getPayeeMapper().createOrGet( payee.getText() ) );
			ti.setSplits( splitdata.getSplits() );
			ti.setJournal( journal.getValue() );
			engine.getTransactionMapper().create( ti );

			rec.setNextRun( RecurrenceMapperImpl.getNextRun( rec ) );
			scheduledata.setRecurrence( rec );
			feedback.setText( "Transaction Added" );
		}
		catch ( MapperException me ) {
			log.error( me, me );
			feedback.setText( me.getLocalizedMessage() );
		}

	}

	@FXML
	public void remove() {
		try {
			int idx = list.getSelectionModel().getSelectedIndex();
			engine.getRecurrenceMapper().remove( list.getSelectionModel().getSelectedItem() );
			list.getItems().remove( idx );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	@FXML
	public void newone() {
		try {
			Recurrence rec = new RecurrenceImpl();
			rec.setName( "New Recurring Transaction "
					+ new SimpleDateFormat( "MM/dd/yyyy'-'hh:mm:ss" ).format( new Date() ) );
			rec.setNextRun( new Date() );

			TransactionImpl trans = new TransactionImpl();
			trans.setJournal( journal.getValue() );

			rec = engine.getRecurrenceMapper().create( rec, trans );

			list.getItems().add( rec );
			list.getSelectionModel().select( rec );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

	}

	private void setCurrent( Recurrence current ) {
		scheduledata.setRecurrence( current );
		label.setText( current.getName() );
		try {
			Transaction t = engine.getTransactionMapper().get( current );
			journal.valueProperty().unbind();

			if ( null == t ) {
				number.textProperty().unbind();
				payee.setText( null );
				splitdata.clear();
				journal.setValue( journal.getItems().get( 0 ) );
			}
			else {
				if ( null == t.getJournal() ) {
					t.setJournal( journal.getItems().get( 0 ) );
				}

				number.textProperty().bindBidirectional( t.getNumberProperty() );
				payee.setText( null == t.getPayee() ? null : t.getPayee().getName() );
				splitdata.setSplits( t.getSplitsProperty() );
				journal.valueProperty().bindBidirectional( t.getJournalProperty() );
			}
		}
		catch ( MapperException n ) {
			log.error( n, n );
		}
	}
}
