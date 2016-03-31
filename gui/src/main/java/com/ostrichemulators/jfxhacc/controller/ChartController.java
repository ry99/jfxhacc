/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.cells.AccountTreeCell;
import com.ostrichemulators.jfxhacc.charting.SeriesMaker;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class ChartController {

	private static final Logger log = Logger.getLogger( ChartController.class );

	public static enum ChartType {

		BAR, LINE, AREA, STACKEDAREA, STACKEDBAR, PIE
	};

	@FXML
	private AnchorPane grapharea;
	@FXML
	private DatePicker startdate;
	@FXML
	private DatePicker enddate;
	@FXML
	private TreeView<Account> accounttree;

	private final SeriesMaker seriesmaker;
	private final DataEngine engine;
	private final AccountMapper amap;
	private XYChart<String, Number> xychart;
	private PieChart piechart;
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

		TreeItem<Account> root = new TreeItem<>();
		accounttree.setRoot( root );
		accounttree.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
		accounttree.setCellFactory( new Callback<TreeView<Account>, TreeCell<Account>>() {

			@Override
			public TreeCell<Account> call( TreeView<Account> p ) {
				return new AccountTreeCell();
			}
		} );

		Comparator<TreeItem<Account>> cmp = new Comparator<TreeItem<Account>>() {

			@Override
			public int compare( TreeItem<Account> o1, TreeItem<Account> o2 ) {
				return o1.getValue().getName().toUpperCase().compareTo( o2.getValue().getName().toUpperCase() );
			}
		};

		try {
			Map<Account, TreeItem<Account>> map
					= GuiUtils.makeAccountTree( amap.getParentMap(), root );

			for ( TreeItem<Account> ti : map.values() ) {
				ti.setExpanded( true );
				ti.getChildren().sort( cmp );
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
		root.getChildren().sort( cmp );
		accounttree.getSelectionModel().getSelectedItems().addListener( new ListChangeListener<TreeItem<Account>>() {
			@Override
			public void onChanged( ListChangeListener.Change<? extends TreeItem<Account>> change ) {
				replot();
			}
		} );

		setChartType( type );
	}

	private void setChartType( ChartType t ) {
		grapharea.getChildren().clear();
		CategoryAxis xaxis = new CategoryAxis();
		NumberAxis yaxis = new NumberAxis();
		xaxis.setLabel( seriesmaker.getYLabel() );
		xaxis.setTickLabelsVisible( true );
		this.type = t;

		xychart = null;
		piechart = null;

		switch ( t ) {
			case BAR:
				BarChart<String, Number> bchart = new BarChart<>( xaxis, yaxis );
				bchart.setBarGap( 3 );
				bchart.setCategoryGap( 20 );
				xychart = bchart;
				break;
			case STACKEDBAR:
				StackedBarChart<String, Number> sbchart = new StackedBarChart<>( xaxis, yaxis );
				sbchart.setCategoryGap( 20 );
				xychart = sbchart;
				break;
			case AREA:
				AreaChart<String, Number> achart = new AreaChart<>( xaxis, yaxis );
				xychart = achart;
				break;
			case STACKEDAREA:
				StackedAreaChart<String, Number> sachart = new StackedAreaChart<>( xaxis, yaxis );
				xychart = sachart;
				break;
			case LINE:
				AreaChart<String, Number> lchart = new AreaChart<>( xaxis, yaxis );
				xychart = lchart;
				break;
			case PIE:
				piechart = new PieChart();
				break;
			default:
				throw new IllegalArgumentException( "unhandled chart type: " + t );
		}

		Chart chart = ( null == xychart ? piechart : xychart );
		grapharea.getChildren().add( chart );
		AnchorPane.setBottomAnchor( chart, 0d );
		AnchorPane.setTopAnchor( chart, 0d );
		AnchorPane.setRightAnchor( chart, 0d );
		AnchorPane.setLeftAnchor( chart, 0d );
		chart.setTitle( seriesmaker.getTitle() );

		replot();
	}

	@FXML
	public void replot() {
		Platform.runLater( new Runnable() {
			@Override
			public void run() {

				if ( null == piechart ) {
					xychart.getData().clear();
				}
				else {
					piechart.getData().clear();
				}

				for ( TreeItem<Account> ti : accounttree.getSelectionModel().getSelectedItems() ) {
					plotSeries( ti.getValue() );
				}
			}
		} );
	}

	private void plotSeries( Account acct ) {
		if ( startdate.getValue().isBefore( enddate.getValue() ) ) {
			if ( null == piechart ) {
				seriesmaker.createSeries( acct,
						startdate.getValue(), enddate.getValue(), xychart );
			}
			else {
				seriesmaker.createPieData( acct, startdate.getValue(),
						enddate.getValue(), piechart );
			}
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
	public void clear() {
		accounttree.getSelectionModel().clearSelection();
	}
}
