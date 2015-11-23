/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.cells.AccountCellFactory;
import com.ostrichemulators.jfxhacc.cells.MoneyCellFactory;
import com.ostrichemulators.jfxhacc.cells.RecoCellFactory;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class SplitsWindowController {

	@FXML
	private TableView<Map.Entry<Account, Split>> splittable;
	@FXML
	private TableColumn<Map.Entry<Account, Split>, Account> account;
	@FXML
	private TableColumn<Map.Entry<Account, Split>, Money> credit;
	@FXML
	private TableColumn<Map.Entry<Account, Split>, Money> debit;
	@FXML
	private TableColumn<Map.Entry<Account, Split>, ReconcileState> reco;

	private Stage stage;
	private final DataEngine engine;
	private final ObservableMap<Account, Split> splits = FXCollections.observableHashMap();
	private boolean canceled = false;

	public SplitsWindowController( DataEngine eng ){
		engine = eng;
	}

	@FXML
	void cancel( ActionEvent event ) {
		canceled = true;
		stage.close();
	}

	public boolean wasCanceled() {
		return canceled;
	}

	@FXML
	void save( ActionEvent event ) {
		stage.close();
	}

	@FXML
	public void initialize() {
		reco.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Map.Entry<Account, Split>, ReconcileState>, ObservableValue<ReconcileState>>() {

			@Override
			public ObservableValue<ReconcileState> call( TableColumn.CellDataFeatures<Map.Entry<Account, Split>, ReconcileState> p ) {
				Split s = p.getValue().getValue();
				return new ReadOnlyObjectWrapper<>( null == s ? null : s.getReconciled() );
			}
		} );
		reco.setCellFactory( new RecoCellFactory<>() );

		credit.setCellValueFactory( new CDValueFactory( true ) );
		credit.setCellFactory( new MoneyCellFactory<>() );
		debit.setCellValueFactory( new CDValueFactory( false ) );
		debit.setCellFactory( new MoneyCellFactory<>() );

		account.setCellValueFactory( ( TableColumn.CellDataFeatures<Map.Entry<Account, Split>, Account> p )
				-> new ReadOnlyObjectWrapper<>( p.getValue().getKey() ) );
		account.setCellFactory( new AccountCellFactory<>( engine.getAccountMapper(), true ) );
	}

	public void setSplitMap( Map<Account, Split> map ) {
		splits.putAll( map );
		splittable.getItems().setAll( splits.entrySet() );
	}

	public ObservableMap<Account, Split> getSplitMap() {
		return splits;
	}

	public void setStage( Stage s ) {
		stage = s;
	}

	private static class CDValueFactory implements Callback<TableColumn.CellDataFeatures<Map.Entry<Account, Split>, Money>, ObservableValue<Money>> {

		public static final Logger log = Logger.getLogger( CDValueFactory.class );
		private final boolean credit;

		public CDValueFactory( boolean iscredit ) {
			credit = iscredit;
		}

		@Override
		public ObservableValue<Money> call( TableColumn.CellDataFeatures<Map.Entry<Account, Split>, Money> p ) {
			Split s = p.getValue().getValue();
			Money val = null;
			if ( ( credit && s.isCredit() ) || ( !credit && s.isDebit() ) ) {
				val = s.getValue();
			}

			return new ReadOnlyObjectWrapper<>( val );
		}
	}
}
