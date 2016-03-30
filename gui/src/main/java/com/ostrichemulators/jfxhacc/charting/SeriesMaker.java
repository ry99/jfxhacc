/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.charting;

import com.ostrichemulators.jfxhacc.model.Account;
import java.time.LocalDate;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

/**
 *
 * @author ryan
 */
public interface SeriesMaker {

	public String getTitle();

	public String getYLabel();

	public void createSeries( Account account,
			LocalDate start, LocalDate end, XYChart<String, Number> chart );

	public void createPieData( Account account,
			LocalDate start, LocalDate end, PieChart chart );
}
