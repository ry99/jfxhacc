/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.charting;

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
public class AccountDeltaMaker extends AbstractSeriesMakerBase {

	private static final Logger log = Logger.getLogger( AccountDeltaMaker.class );
	private final TransactionMapper tmap;

	public AccountDeltaMaker( TransactionMapper tmap ) {
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
		XYChart.Series<String, Number> adds = new XYChart.Series<>();
		XYChart.Series<String, Number> subs = new XYChart.Series<>();
		XYChart.Series<String, Number> deltas = new XYChart.Series<>();
		adds.setName( acct.getName() + " Additions" );
		subs.setName( acct.getName() + " Withdrawals" );
		deltas.setName( acct.getName() + " Delta" );

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

		while ( pfirst.isBefore( end ) ) {
			Money creds = new Money();
			Money debs = new Money();
			Money delta = new Money();

			LocalDate plast = pfirst.plusMonths( 1l );
			if ( plast.isAfter( end ) ) {
				plast = end;
			}

			for ( Split s : getSplits( splits, pfirst, plast ) ) {
				if ( s.isCredit() ) {
					//log.debug( "credit: " + s.getId() + " " + s.getValue() );
					creds = creds.add( s.getValue() );
				}
				else {
					//log.debug( "debit: " + s.getId() + " " + s.getValue() );
					debs = debs.add( s.getValue() );
				}
				Money change = AccountBalanceCache.getSplitValueForAccount( s, acct );
				delta = delta.add( change );
			}

			String label = getLabel( pfirst );
			log.debug( label + " credits: " + creds + "; debits: " + debs + "; delta: " + delta );

			deltas.getData().add( new XYChart.Data<>( label, delta.toDouble() ) );
			if ( acct.getAccountType().isDebitPlus() ) {
				adds.getData().add( new XYChart.Data<>( label, debs.toDouble() ) );
				subs.getData().add( new XYChart.Data<>( label, creds.opposite().toDouble() ) );
			}
			else {
				adds.getData().add( new XYChart.Data<>( label, creds.toDouble() ) );
				subs.getData().add( new XYChart.Data<>( label, debs.opposite().toDouble() ) );
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

		return Arrays.asList( adds, subs, deltas );
	}
}
