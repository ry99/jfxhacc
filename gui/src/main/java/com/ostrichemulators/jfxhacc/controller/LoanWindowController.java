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
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Loan;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.impl.LoanImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Loans;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
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
	private ChoiceBox<Journal> journalchsr;
	@FXML
	private AnchorPane schedarea;
	@FXML
	private ListView<Recurrence> list;

	private ScheduleDataWindowController scheduledata;
	private final DataEngine engine;
	private Stage stage;
	private Recurrence recurrence;
	private Loan loan;
	private final RateFormatter ratefmt = new RateFormatter();
	private final ChangeListener<String> changer = new ChangeListener<String>(){

		@Override
		public void changed( ObservableValue<? extends String> ov, String t, String t1 ) {
			loan.setApr( ratefmt.getValue() );
		}
	};

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

			journalchsr.setItems( engine.getJournalMapper().getObservable() );
			journalchsr.getSelectionModel().clearAndSelect( 0 );

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

			prinacct.setValue( amap.getAccounts( AccountType.LIABILITY ).asList().get( 0 ) );
			intacct.setValue( amap.getAccounts( AccountType.EXPENSE ).asList().get( 0 ) );
			payacct.setValue( amap.getAccounts( AccountType.ASSET ).asList().get( 0 ) );

			rate.setTextFormatter( ratefmt );
		}
		catch ( IOException | MapperException ioe ) {
			log.error( ioe, ioe );
		}
	}

	@FXML
	void newLoan( ActionEvent event ) {
		try {
			loan = new LoanImpl();
			loan.setApr( null == ratefmt.getValue() ? 0d : ratefmt.getValue() );
			loan.setNumberOfPayments( Integer.valueOf( numpayments.getText().isEmpty()
					? "36" : numpayments.getText() ) );
			loan.setInitialValue( Money.valueOf( amount.getText().isEmpty() ? "1000.00"
					: amount.getText() ) );

			loan.setSourceAccount( payacct.getValue() );
			loan.setPrincipalAccount( prinacct.getValue() );
			loan.setInterestAccount( intacct.getValue() );
			loan.setJournal( journalchsr.getValue() );

			Recurrence r = scheduledata.getRecurrence();
			r.setName( String.format( "New Loan %s",
					new SimpleDateFormat( "MM/dd/yyyy'-'hh:mm:ss" ).format( new Date() ) ) );
			r = engine.getRecurrenceMapper().create( r, loan );
			list.getItems().add( r );
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
		amount.textProperty().bindBidirectional( loan.getInitialValueProperty(),
				new MoneyStringConverter() );

		numpayments.textProperty().unbind();
		numpayments.textProperty().bindBidirectional( loan.getNumberOfPaymentsProperty(),
				new NumberStringConverter() );

		ratefmt.setValue( loan.getApr() );
		rate.textProperty().addListener( changer );

		name.textProperty().unbind();
		name.textProperty().bindBidirectional( r.getNameProperty() );

		Property<Money> valprp = new SimpleObjectProperty<>();
		valprp.bind( loan.getPaymentAmount() );
		payment.textProperty().unbind();
		payment.textProperty().bindBidirectional( valprp, new MoneyStringConverter() );

		scheduledata.setRecurrence( r );

		journalchsr.valueProperty().unbind();
		journalchsr.valueProperty().bindBidirectional( loan.getJournalProperty() );

		prinacct.valueProperty().unbind();
		prinacct.valueProperty().bindBidirectional( loan.getPrincipalAccountProperty() );

		intacct.valueProperty().unbind();
		intacct.valueProperty().bindBidirectional( loan.getInterestAccountProperty() );

		payacct.valueProperty().unbind();
		payacct.valueProperty().bindBidirectional( loan.getSourceAccountProperty() );
	}

	@FXML
	public void save() {
		try {
			loan.setApr( ratefmt.getValue() );

			engine.getLoanMapper().update( loan );
			Recurrence rec = scheduledata.getRecurrence();
			rec.setName( name.getText() );
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

	private class RateFormatter extends TextFormatter<Double> {

		public RateFormatter() {
			super( new StringConverter<Double>() {

				@Override
				public String toString( Double t ) {
					if ( null == t ) {
						t = 0d;
					}
					return String.format( "%1.4f%%", t * 100 );
				}

				@Override
				public Double fromString( String string ) {
					if ( null == string || string.isEmpty() ) {
						return 0d;
					}

					return Double.parseDouble( string.replaceAll( "%", "" ) )/100;
				}
			} );
		}
	}
}
