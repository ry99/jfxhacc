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
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class PayeeAccountMemoValueFactory implements Callback<TableColumn.CellDataFeatures<SplitStub, PAMData>, ObservableValue<PAMData>> {

	public static final Logger log = Logger.getLogger( PayeeAccountMemoValueFactory.class );
	private final SplitStubManager stubman;
	private final AccountManager acctman;
	private final PayeeManager payman;
	private final ObservableList<SplitStub> allstubs;

	public PayeeAccountMemoValueFactory( SplitStubManager stubman, AccountManager aman,
			PayeeManager pman ) {
		this.stubman = stubman;
		this.acctman = aman;
		this.payman = pman;
		allstubs = FXCollections.observableArrayList( ( SplitStub ss ) -> {
			return new Observable[]{
				ss.getAccountIdProperty(),
				ss.getPayeeProperty(),
				ss.getMemoProperty()
			};
		} );

		allstubs.addListener( new ListChangeListener<SplitStub>() {
			@Override
			public void onChanged( ListChangeListener.Change<? extends SplitStub> c ) {
				log.debug( "allstubs changed somehow" );
			}
		} );

		Bindings.bindContent( allstubs, stubman.getAll() );
	}

	private void setPamData( PAMData data, SplitStub trans,
			ObservableList<SplitStub> splits ) {

		if ( splits.size() > 1 ) {
			data.account.set( "Split" );
		}
		else {
			// we don't want to show our current account here; we want the other one
			SplitStub other = splits.get( 0 );
			data.account.set( splits.isEmpty() ? "{BUG}"
					: acctman.getMap().get( other.getAccountId() ).getName() );
		}

		data.memo.unbind();
		data.memo.bind( trans.getMemoProperty() );

		data.payee.unbind();
		data.payee.bind( Bindings.createStringBinding( () -> {
			return payman.get( trans.getPayee() ).getName();
		}, trans.getPayeeProperty() ) );
	}

	@Override
	public ObservableValue<PAMData> call( TableColumn.CellDataFeatures<SplitStub, PAMData> p ) {
		SplitStub trans = p.getValue();

		PAMData data = new PAMData( payman.get( trans.getPayee() ).getName(),
				null, trans.getMemo() );

		allstubs.addListener( new ListChangeListener<SplitStub>() {
			@Override
			public void onChanged( ListChangeListener.Change<? extends SplitStub> c ) {
				setPamData( data, trans, stubman.getOtherStubs( trans ) );
			}
		} );

		setPamData( data, trans, stubman.getOtherStubs( trans ) );

		return new ReadOnlyObjectWrapper<>( data );
	}
}
