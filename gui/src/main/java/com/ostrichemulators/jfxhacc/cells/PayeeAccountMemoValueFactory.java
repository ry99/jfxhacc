/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.MainApp;
import com.ostrichemulators.jfxhacc.controller.TransactionViewController.PAMData;
import com.ostrichemulators.jfxhacc.datamanager.AccountManager;
import com.ostrichemulators.jfxhacc.datamanager.PayeeManager;
import com.ostrichemulators.jfxhacc.datamanager.SplitStubManager;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class PayeeAccountMemoValueFactory implements Callback<TableColumn.CellDataFeatures<SplitStub, PAMData>, ObservableValue<PAMData>> {

	public static final Logger log = Logger.getLogger( PayeeAccountMemoValueFactory.class );
	private Account selected;
	private final SplitStubManager stubman;
	private final AccountManager acctman;
	private final Map<URI, String> paynames;
	private ObservableList<SplitStub> stubs = FXCollections.observableArrayList();

	public PayeeAccountMemoValueFactory( SplitStubManager stubman, AccountManager aman,
			PayeeManager pman ) {
		this.stubman = stubman;
		this.acctman = aman;
		this.paynames = pman.getNameMap();
	}

	public void setAccount( Account a ) {
		selected = a;

		stubs = stubman.getAllSplitStubs( selected );
	}

	@Override
	public ObservableValue<PAMData> call( TableColumn.CellDataFeatures<SplitStub, PAMData> p ) {
		SplitStub trans = p.getValue();
		List<SplitStub> splits = stubs
				.filtered( MainApp.PF.filter( Transactions.TYPE, trans.getTransactionId() ) )
				.filtered( MainApp.PF.account( selected ).negate() );
		String acct = "";

		if ( splits.size() > 1 ) {
			acct = "Split";
		}
		else {
			// we don't want to show our current account here; we want the other one
			acct = ( splits.isEmpty() ? "{BUG}"
					: acctman.getMap().get( splits.get( 0 ).getAccountId() ).getName() );
		}

		PAMData data
				= new PAMData( paynames.get( trans.getPayee() ), acct, trans.getMemo() );
		return new ReadOnlyObjectWrapper<>( data );
	}
}
