/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.charting.SeriesMaker;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class ChartController {

	private static final Logger log = Logger.getLogger( ChartController.class );

	public static enum ChartType {

		BAR, LINE, AREA
	};

	@FXML
	private AnchorPane grapharea;
	@FXML
	private DatePicker startdate;
	@FXML
	private DatePicker enddate;
	@FXML
	private ComboBox<Account> accounts;

	private final SeriesMaker seriesmaker;
	private final DataEngine engine;
	private final Set<Account> selecteds = new LinkedHashSet<>();
	private final AccountMapper amap;
	private XYChart<String, Number> chart;
	private ChartType type;
	private Stage stage;

	public ChartController( DataEngine eng, ChartType type, SeriesMaker maker,
			Account... accounts ) {
		engine = eng;
		seriesmaker = maker;
		amap = engine.getAccountMapper();
		this.type = type;
	}

	@FXML
	public void initialize() {
		startdate.setValue( LocalDate.now().minusYears( 1l ) );
		enddate.setValue( LocalDate.now() );

		try {
			GuiUtils.makeAccountCombo( accounts, amap.getAll(), amap );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		setChartType( type );
	}

	private void setChartType( ChartType t ) {
		grapharea.getChildren().clear();
		CategoryAxis xaxis = new CategoryAxis();
		NumberAxis yaxis = new NumberAxis();
		xaxis.setLabel( seriesmaker.getYLabel() );
		xaxis.setTickLabelsVisible( true );

		switch ( t ) {
			case BAR:
				BarChart<String, Number> bchart = new BarChart<>( xaxis, yaxis );
				bchart.setBarGap( 3 );
				bchart.setCategoryGap( 20 );
				chart = bchart;
				break;
			case AREA:
				AreaChart<String, Number> achart = new AreaChart<>( xaxis, yaxis );
				chart = achart;
				break;
			case LINE:
				AreaChart<String, Number> lchart = new AreaChart<>( xaxis, yaxis );
				chart = lchart;
				break;
			default:
				throw new IllegalArgumentException( "unhandled chart type: " + t );
		}

		grapharea.getChildren().add( chart );
		AnchorPane.setBottomAnchor( chart, 0d );
		AnchorPane.setTopAnchor( chart, 0d );
		AnchorPane.setRightAnchor( chart, 0d );
		AnchorPane.setLeftAnchor( chart, 0d );

		chart.setTitle( seriesmaker.getTitle() );
		replot();
		// xaxis.invalidateRange( xaxis.getCategories() );
	}

	@FXML
	public void replot() {
		chart.getData().clear();
		for ( Account a : selecteds ) {
			plotSeries( a );
		}
	}

	@FXML
	public void addaccount( ActionEvent event ) {
		Account acct = accounts.getValue();
		if ( !selecteds.contains( acct ) ) {
			selecteds.add( acct );
			plotSeries( acct );
		}

		accounts.setValue( null );
	}

	private void plotSeries( Account acct ) {
		chart.getData().add( seriesmaker.createSeries( acct, startdate.getValue(),
				enddate.getValue() ) );
	}

	public void setStage( Stage s ) {
		stage = s;
	}

	@FXML
	public void close() {
		stage.close();
	}
}
