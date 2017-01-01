/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.charting;

import com.ostrichemulators.jfxhacc.model.Money;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public abstract class AbstractSeriesMakerBase implements SeriesMaker {

	private static final Logger log = Logger.getLogger( AbstractSeriesMakerBase.class );

	public static String getLabel( LocalDate d ) {
		Calendar cal = Calendar.getInstance();
		cal.set( d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth() );

		return cal.getDisplayName( Calendar.MONTH,
				Calendar.SHORT_STANDALONE, Locale.getDefault() ) + " "
				+ cal.get( Calendar.DAY_OF_MONTH ) + " "
				+ cal.get( Calendar.YEAR );
	}

	public static Tooltip installTooltip( Node n, final LocalDate pfirst,
			final Money value, final DateFormat DF ) {

		Instant labeldate = pfirst.atStartOfDay().atZone( ZoneId.systemDefault() ).toInstant();
		return installTooltip( n, DF.format( Date.from( labeldate ) )
				+ "\n" + value.toString() );
	}

	public static Tooltip installTooltip( Node n, String text ) {

		Tooltip tt = new Tooltip( text );
		Tooltip.install( n, tt );

		n.setOnMouseEntered( new EventHandler<MouseEvent>() {

			@Override
			public void handle( MouseEvent t ) {
				n.getStyleClass().add( "onHover" );
			}
		} );
		n.setOnMouseExited( new EventHandler<MouseEvent>() {

			@Override
			public void handle( MouseEvent t ) {
				n.getStyleClass().remove( "onHover" );
			}
		} );

		return tt;
	}
}
