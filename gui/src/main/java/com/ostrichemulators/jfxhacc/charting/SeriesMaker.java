/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.charting;

import com.ostrichemulators.jfxhacc.model.Account;
import java.time.LocalDate;
import javafx.scene.chart.XYChart.Series;

/**
 *
 * @author ryan
 */
public interface SeriesMaker {

	public String getTitle();

	public String getYLabel();

	public Series<String, Number> createSeries( Account account, LocalDate start, LocalDate end );
}
