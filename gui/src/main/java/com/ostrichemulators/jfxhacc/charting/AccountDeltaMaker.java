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
import com.ostrichemulators.jfxhacc.utility.AccountHelper;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.chart.PieChart;
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
		return "Account Changes";
	}

	@Override
	public String getYLabel() {
		return "Date";
	}

	@Override
	public void createPieData( Account account, LocalDate start, LocalDate end,
			PieChart chart ) {
	}

	@Override
	public void createSeries( Account acct, final LocalDate start,
			final LocalDate end, XYChart<String, Number> chart ) {
		XYChart.Series<String, Number> adds = new XYChart.Series<>();
		XYChart.Series<String, Number> subs = new XYChart.Series<>();
		XYChart.Series<String, Number> deltas = new XYChart.Series<>();
		adds.setName( acct.getName() + " Additions" );
		subs.setName( acct.getName() + " Withdrawals" );
		deltas.setName( acct.getName() + " Delta" );

		chart.getData().addAll( Arrays.asList( adds, subs, deltas ) );

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

		DateFormat DF = DateFormat.getDateInstance( DateFormat.MEDIUM );

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
					creds = creds.plus( s.getValue() );
				}
				else {
					debs = debs.plus( s.getValue() );
				}
				Money change = AccountHelper.getSplitValueForAccount( s, acct );
				delta = delta.plus( change );
			}

			String label = getLabel( pfirst );
			log.debug( label + " credits: " + creds + "; debits: "
					+ debs + "; delta: " + delta );

			XYChart.Data<String, Number> deltadp = new XYChart.Data<>( label, delta.toDouble() );
			XYChart.Data<String, Number> adp;
			XYChart.Data<String, Number> sdp;

			deltas.getData().add( deltadp );
			installTooltip( deltadp.getNode(), pfirst, delta, DF );

			if ( acct.getAccountType().isDebitPlus() ) {
				adp = new XYChart.Data<>( label, debs.toDouble() );
				sdp = new XYChart.Data<>( label, creds.opposite().toDouble() );
				adds.getData().add( adp );
				subs.getData().add( sdp );

				installTooltip( adp.getNode(), pfirst, debs, DF );
				installTooltip( sdp.getNode(), pfirst, creds.opposite(), DF );
			}
			else {
				adp = new XYChart.Data<>( label, creds.toDouble() );
				sdp = new XYChart.Data<>( label, debs.opposite().toDouble() );
				adds.getData().add( adp );
				subs.getData().add( sdp );

				installTooltip( adp.getNode(), pfirst, creds, DF );
				installTooltip( sdp.getNode(), pfirst, debs.opposite(), DF );
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
	}
}
