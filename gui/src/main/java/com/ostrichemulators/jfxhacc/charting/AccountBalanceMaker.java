/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.charting;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.utility.AccountBalanceCache;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.chart.XYChart;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class AccountBalanceMaker extends AbstractSeriesMakerBase {

	private static final Logger log = Logger.getLogger( AccountBalanceMaker.class );
	private final AccountMapper amap;
	private final TransactionMapper tmap;

	public AccountBalanceMaker( AccountMapper amap, TransactionMapper tmap ) {
		this.amap = amap;
		this.tmap = tmap;
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
	public Collection<XYChart.Series<String, Number>> createSeries( Account acct,
			final LocalDate start, final LocalDate end ) {
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName( acct.getName() );
		//XYChart.Series<String, Number> amapseries = new XYChart.Series<>();
		//amapseries.setName( acct.getName() + " amap" );

		LocalDate pfirst = start;
		Instant instant = pfirst.atStartOfDay( ZoneId.systemDefault() ).toInstant();
		Date starttime = Date.from( instant );

		Instant instant2 = end.atStartOfDay( ZoneId.systemDefault() ).toInstant();
		Date endtime = Date.from( instant2 );

		Map<LocalDate, List<Split>> splits = new HashMap<>();
		try {
			splits.putAll( tmap.getSplits( acct, starttime, endtime ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		Money value = amap.getBalance( acct, AccountMapper.BalanceType.CURRENT, starttime );
		// Money value2 = new Money( value.value() );
		while ( pfirst.isBefore( end ) ) {
			instant = pfirst.atStartOfDay( ZoneId.systemDefault() ).toInstant();
			Calendar cal = Calendar.getInstance();
			Date startdate = Date.from( instant );
			cal.setTime( startdate );
			String label = getLabel( pfirst );
			series.getData().add( new XYChart.Data<>( label, value.toDouble() ) );

			log.debug( "calculated balance at " + startdate + ": " + value );
			//if ( !value2.equals( value ) ) {
			//	value2 = amap.getBalance( acct,
			//			AccountMapper.BalanceType.CURRENT, startdate );
			//}
			//log.debug( "amap balance at " + startdate + " is " + value2 );
			//amapseries.getData().add( new XYChart.Data<>( label, value2.toDouble() ) );

			LocalDate plast = pfirst.plusMonths( 1l );
			//  don't overshoot our end date
			if ( plast.isAfter( end ) ) {
				plast = end;
			}

			for ( Split s : getSplits( splits, pfirst, plast ) ) {
				Money change = AccountBalanceCache.getSplitValueForAccount( s, acct );
				//log.debug( "  " + running + " split: " + s.getId() + " -> " + s.getValue() );
				value = value.add( change );
			}

			// see if we have any "leftover" days in the next month to worry about
			pfirst = pfirst.plusMonths( 1l );
			if ( !pfirst.isBefore( end ) ) {
				pfirst = pfirst.minusMonths( 1l );
				Period days = pfirst.until( end );
				if ( days.getDays() > 0 ) {
					pfirst = pfirst.plusDays( days.getDays() );

				}
				else {
					pfirst = pfirst.plusMonths( 1l );
				}
			}
		}

		//return Arrays.asList( series, amapseries );
		return Arrays.asList( series );
	}
}
