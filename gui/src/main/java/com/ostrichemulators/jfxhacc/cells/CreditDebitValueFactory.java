/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
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
public class CreditDebitValueFactory implements Callback<TableColumn.CellDataFeatures<Transaction, Money>, ObservableValue<Money>> {

	public static final Logger log = Logger.getLogger( CreditDebitValueFactory.class );
	private Account selected;
	private final boolean credit;

	public CreditDebitValueFactory( boolean iscredit ) {
		credit = iscredit;
	}

	public void setAccount( Account a ) {
		selected = a;
	}

	@Override
	public ObservableValue<Money> call( TableColumn.CellDataFeatures<Transaction, Money> p ) {
		Transaction trans = p.getValue();
		Map<Account, Split> splits = trans.getSplits();
		Split s = splits.get( selected );

		Money val = null;
		if ( ( credit && s.isCredit() ) || ( !credit && s.isDebit() ) ) {
			val = s.getValue();
		}

		return new ReadOnlyObjectWrapper<>( val );
	}
}
