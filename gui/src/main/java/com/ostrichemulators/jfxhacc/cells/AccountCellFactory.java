/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class AccountCellFactory<T> implements Callback<TableColumn<T, Account>, TableCell<T, Account>> {

	public static final Logger log = Logger.getLogger( AccountCellFactory.class );
	private final AccountMapper amap;
	private boolean usefull = false;

	public AccountCellFactory( AccountMapper amap ) {
		this.amap = amap;
	}

	public AccountCellFactory( AccountMapper amap, boolean full ) {
		this.amap = amap;
		this.usefull = full;
	}

	@Override
	public TableCell<T, Account> call( TableColumn<T, Account> p ) {
		List<Account> accounts = new ArrayList<>();
		final Map<String, Account> acctmap = new HashMap<>();
		try {
			accounts.addAll( amap.getAll() );
			for ( Account a : accounts ) {
				acctmap.put( usefull ? GuiUtils.getFullName( a, amap ) : a.getName(), a );
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		Collections.sort( accounts, new Comparator<Account>() {

			@Override
			public int compare( Account o1, Account o2 ) {
				return Collator.getInstance().compare( GuiUtils.getFullName( o1, amap ),
						GuiUtils.getFullName( o2, amap ) );
			}
		} );

		AccountStringConverter asc = new AccountStringConverter( acctmap );
		ObservableList<Account> obsl = FXCollections.observableArrayList( accounts );
		ComboBoxTableCell<T, Account> cell = new ComboBoxTableCell<T, Account>( asc, obsl ) {
			@Override
			public void updateItem( Account acct, boolean empty ) {
				super.updateItem( acct, empty );
				if ( null == acct || empty ) {
					setText( null );
				}
				else {
					setText( usefull
							? GuiUtils.getFullName( acct, amap )
							: acct.getName() );
				}
			}
		};

		cell.setComboBoxEditable( false );
		return cell;
	}

	private class AccountStringConverter extends StringConverter<Account> {

		private final Map<String, Account> acctmap;

		AccountStringConverter( Map<String, Account> map ) {
			this.acctmap = map;
		}

		@Override
		public String toString( Account t ) {
			if( null == t ){
				return null;
			}

			return ( usefull ? GuiUtils.getFullName( t, amap ) : t.getName() );
		}

		@Override
		public Account fromString( String string ) {
			return acctmap.get( string );
		}
	}
}
