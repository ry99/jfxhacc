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
import com.ostrichemulators.jfxhacc.utility.AccountHelper;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
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
	public void createPieData( Account account,	LocalDate start, LocalDate end,
			PieChart chart ) {
	}

	@Override
	public void createSeries( Account acct, final LocalDate start,
			final LocalDate end, XYChart<String, Number> chart ) {
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName( acct.getName() );
		chart.getData().add( series );

		LocalDate pfirst = start;
		Instant instant = pfirst.atStartOfDay( ZoneId.systemDefault() ).toInstant();
		Date starttime = Date.from( instant );

		Instant instant2 = end.atStartOfDay( ZoneId.systemDefault() ).toInstant();
		Date endtime = Date.from( instant2 );
		LocalDate plast = end.minusDays( 1l );

		Map<LocalDate, List<Split>> splits = new HashMap<>();
		try {
			splits.putAll( tmap.getSplits( acct, starttime, endtime ) );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		Money value = amap.getBalance( acct, AccountMapper.BalanceType.CURRENT, starttime );
		final int dayOfMonth = pfirst.getDayOfMonth();
		final DateFormat DF = DateFormat.getDateInstance( DateFormat.MEDIUM );

		while ( pfirst.isBefore( end ) ) {
			for ( Split s : splits.getOrDefault( pfirst, new ArrayList<>() ) ) {
				Money change = AccountHelper.getSplitValueForAccount( s, acct );
				value = value.add( change );
			}

			String label = pfirst.toString();
			boolean shownode = ( pfirst.getDayOfMonth() == dayOfMonth || pfirst.equals( plast ) );

			XYChart.Data<String, Number> datapoint
					= new XYChart.Data<>( label, value.toDouble() );
			series.getData().add( datapoint );

			Node n = datapoint.getNode();
			n.setVisible( shownode );

			installTooltip( n, pfirst, value, DF );

			pfirst = pfirst.plusDays( 1l );
		}
	}
}
