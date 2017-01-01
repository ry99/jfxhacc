/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.Recurrence.Frequency;
import com.ostrichemulators.jfxhacc.model.impl.RecurrenceImpl;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class ScheduleDataWindowController {

	private static final Logger log
			= Logger.getLogger( ScheduleDataWindowController.class );
	@FXML
	private DatePicker datepicker;
	@FXML
	private GridPane grid;

	private final Map<Frequency, RadioButton> buttons = new HashMap<>();
	private final ToggleGroup group = new ToggleGroup();

	@FXML
	public void initialize() {
		buttons.clear();
		Frequency freqs[] = Frequency.values();

		for ( int i = 0; i < freqs.length; i++ ) {
			RadioButton btn = new RadioButton( freqs[i].toString() );
			btn.setSelected( 0 == i );
			btn.setToggleGroup( group );
			btn.prefWidthProperty().bind( grid.widthProperty().divide( 3 ) );
			btn.setUserData( freqs[i] );
			buttons.put( freqs[i], btn );
			grid.add( btn, i % 3, i / 3 );
		}
	}

	public void setRecurrence( Recurrence r ) {
		group.selectToggle( buttons.get( r.getFrequency() ) );

		Instant instant = r.getNextRun().toInstant();
		LocalDate ld = instant.atZone( ZoneId.systemDefault() ).toLocalDate();
		datepicker.setValue( ld );
	}

	public Recurrence getRecurrence() {
		RecurrenceImpl r = new RecurrenceImpl();
		r.setFrequency( Frequency.class.cast( group.getSelectedToggle().getUserData() ) );

		LocalDate localDate = datepicker.getValue();
		if ( null == localDate ) {
			localDate = LocalDate.now( ZoneId.systemDefault() );
		}
		Instant instant = Instant.from( localDate.atStartOfDay( ZoneId.systemDefault() ) );
		r.setNextRun( Date.from( instant ) );

		return r;
	}
}
