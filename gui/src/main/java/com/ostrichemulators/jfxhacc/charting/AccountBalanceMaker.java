/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.charting;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javafx.scene.chart.XYChart;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class AccountBalanceMaker implements SeriesMaker {

	private static final Logger log = Logger.getLogger( AccountBalanceMaker.class );
	private final AccountMapper amap;

	public AccountBalanceMaker( AccountMapper amap ) {
		this.amap = amap;
	}

	@Override
	public String getTitle() {
		return "Account Balance";
	}

	@Override
	public String getYLabel() {
		return "Date";
	}

	@Override
	public XYChart.Series<String, Number> createSeries( Account acct, LocalDate start, final LocalDate end ) {
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName( acct.getName() );

		LocalDate pfirst = start;

		while ( pfirst.isBefore( end ) ) {
			LocalDate plast = pfirst.plusMonths( 1l ).minusDays( 1l );
			// don't overshoot our end date
			if ( plast.isAfter( end ) ) {
				plast = end;
			}

			Instant instant = pfirst.atStartOfDay( ZoneId.systemDefault() ).toInstant();
			Date stime = Date.from( instant );

			//Instant instant2 = plast.atStartOfDay( ZoneId.systemDefault() ).toInstant();
			//Date etime = Date.from( instant2 );
			Money sm = amap.getBalance( acct, AccountMapper.BalanceType.CURRENT, stime );

			//Money em = amap.getBalance( acct, BalanceType.CURRENT, etime );
			double diff = sm.toDouble(); // em.minus( sm ).toDouble();
			if ( !acct.getAccountType().isDebitPlus() ) {
				diff = 0 - diff;
			}

			Calendar cal = Calendar.getInstance();
			cal.setTime( stime );
			String label = cal.getDisplayName( Calendar.MONTH,
					Calendar.SHORT_STANDALONE, Locale.getDefault() ) + " " + pfirst.getYear();
			series.getData().add( new XYChart.Data<>( label, diff ) );

			pfirst = pfirst.plusMonths( 1l );
		}

		return series;
	}
}
