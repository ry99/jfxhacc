/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.TransactionViewController.PAMData;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class PayeeAccountMemoValueFactory implements Callback<TableColumn.CellDataFeatures<Transaction, PAMData>, ObservableValue<PAMData>> {

	public static final Logger log = Logger.getLogger( PayeeAccountMemoValueFactory.class );
	private Account selected;

	public void setAccount( Account a ) {
		selected = a;
	}

	@Override
	public ObservableValue<PAMData> call( TableColumn.CellDataFeatures<Transaction, PAMData> p ) {
		Transaction trans = p.getValue();
		Map<Account, Split> splits = trans.getSplits();
		Split mysplit = splits.get( selected );

		String acct = null;
		if ( splits.size() > 2 ) {
			acct = "Split";
		}
		else {
			// we don't want to show our current account here; we want the other one
			for ( Account a : splits.keySet() ) {
				if ( !a.equals( selected ) ) {
					acct = a.getName();
				}
			}
		}

		PAMData data
				= new PAMData( trans.getPayee().getName(), acct, mysplit.getMemo() );
		return new ReadOnlyObjectWrapper<>( data );
	}
}
