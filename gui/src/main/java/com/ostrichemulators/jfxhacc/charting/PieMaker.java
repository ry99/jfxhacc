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
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.utility.TransactionHelper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class PieMaker extends AbstractSeriesMakerBase {

	private static final Logger log = Logger.getLogger( AccountBalanceMaker.class );
	private final AccountMapper amap;
	private final TransactionMapper tmap;
	private final boolean doCredits;

	public PieMaker( boolean credits, AccountMapper amap, TransactionMapper tmap ) {
		this.amap = amap;
		this.tmap = tmap;
		doCredits = credits;
	}

	@Override
	public String getTitle() {
		return ( doCredits ? "Credits Pie" : "Debits Pie" );
	}

	@Override
	public String getYLabel() {
		return "Date";
	}

	@Override
	public void createPieData( Account account,
			LocalDate start, LocalDate end, PieChart chart ) {
		LocalDate pfirst = start;
		Instant instant = pfirst.atStartOfDay( ZoneId.systemDefault() ).toInstant();
		Date starttime = Date.from( instant );

		Instant instant2 = end.atStartOfDay( ZoneId.systemDefault() ).toInstant();
		Date endtime = Date.from( instant2 );

		try {
			Collection<Transaction> trans = tmap.getAll( account, starttime, endtime );
			Map<Account, Money> data = new HashMap<>();
			for ( Transaction t : trans ) {

				Split mysplit = TransactionHelper.getSplit( t, account );
				if ( doCredits == mysplit.isCredit() ) {
					// this is a transaction we want to handle
					for ( Split s : TransactionHelper.getOthers( t, account ) ) {
						Account a = s.getAccount();
						Money saved = data.getOrDefault( a, new Money() );
						data.put( a, saved.plus( s.getValue() ) );
					}
				}
			}

			Map<String, Double> moddata = condense( data, 7 );
			double total = 0d;
			for ( Map.Entry<String, Double> e : moddata.entrySet() ) {
				PieChart.Data slice = new PieChart.Data( e.getKey(), e.getValue() );
				total += e.getValue();
				chart.getData().add( slice );
			}

			Money allmoney = Money.valueOf( total );
			for ( PieChart.Data slice : chart.getData() ) {
				String tt = String.format( "%s\n%s of %s (%2.1f%%)", slice.getName(),
						Money.valueOf( slice.getPieValue() ), allmoney,
						slice.getPieValue() * 100 / total );
				installTooltip( slice.getNode(), tt );
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	@Override
	public void createSeries( Account acct,
			final LocalDate start, final LocalDate end, XYChart<String, Number> chart ) {
	}

	private static Map<String, Double> condense( Map<Account, Money> map, int maxsize ) {
		List<Map.Entry<Account, Money>> datalist = new ArrayList<>( map.entrySet() );
		datalist.sort( new Comparator<Map.Entry<Account, Money>>() {

			@Override
			public int compare( Map.Entry<Account, Money> o1, Map.Entry<Account, Money> o2 ) {
				return o2.getValue().compareTo( o1.getValue() );
			}
		} );

		Map<String, Double> moddata = new HashMap<>();
		int limit = Math.min( datalist.size(), maxsize );

		for ( int i = 0; i < limit; i++ ) {
			Map.Entry<Account, Money> entry = datalist.get( i );
			moddata.put( entry.getKey().getName(), entry.getValue().toDouble() );
		}

		if ( datalist.size() > limit ) {
			ListIterator<Map.Entry<Account, Money>> li = datalist.listIterator( maxsize );
			Money money = new Money();
			while ( li.hasNext() ) {
				Map.Entry<Account, Money> entry = li.next();
				money = money.plus( entry.getValue() );
			}

			moddata.put( "Other", money.toDouble() );
		}

		return moddata;
	}
}
