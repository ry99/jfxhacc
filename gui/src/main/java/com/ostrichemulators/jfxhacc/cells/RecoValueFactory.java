/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class RecoValueFactory implements Callback<TreeTableColumn.CellDataFeatures<Transaction, ReconcileState>, ObservableValue<ReconcileState>> {

	public static final Logger log = Logger.getLogger( RecoValueFactory.class );
	private Account selected;

	public void setAccount( Account a ) {
		selected = a;
	}

	@Override
	public ObservableValue<ReconcileState> call( TreeTableColumn.CellDataFeatures<Transaction, ReconcileState> p ) {
		Transaction trans = p.getValue().getValue();
		Map<Account, Split> splits = trans.getSplits();
		Split s = splits.get( selected );

		return new ReadOnlyObjectWrapper<>( null == s ? null : s.getReconciled() );
	}
}
