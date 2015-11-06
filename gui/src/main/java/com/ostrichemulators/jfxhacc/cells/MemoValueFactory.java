/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Map;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class MemoValueFactory implements Callback<TreeTableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>> {

	public static final Logger log = Logger.getLogger( MemoValueFactory.class );
	private Account selected;

	public void setAccount( Account a ) {
		selected = a;
	}

	@Override
	public ObservableValue<String> call( TreeTableColumn.CellDataFeatures<Transaction, String> p ) {
		Transaction trans = p.getValue().getValue();
		Map<Account, Split> splits = trans.getSplits();
		Split s = splits.get( selected );
		return new ReadOnlyStringWrapper( null == s ? null : s.getMemo() );
	}
}
