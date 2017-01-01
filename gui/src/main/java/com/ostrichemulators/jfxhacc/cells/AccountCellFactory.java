/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.datamanager.AccountManager;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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
 * @param <T>
 */
public class AccountCellFactory<T> implements Callback<TableColumn<T, Account>, TableCell<T, Account>> {

	public static final Logger log = Logger.getLogger( AccountCellFactory.class );
	private final AccountManager aman;
	private boolean usefull = false;

	public AccountCellFactory( AccountManager amap ) {
		this.aman = amap;
	}

	public AccountCellFactory( AccountManager amap, boolean full ) {
		this.aman = amap;
		this.usefull = full;
	}

	@Override
	public TableCell<T, Account> call( TableColumn<T, Account> p ) {
		ObservableList<Account> accounts = aman.getAll().sorted( new Comparator<Account>() {
			@Override
			public int compare( Account o1, Account o2 ) {
				return Collator.getInstance().compare( GuiUtils.getFullName( o1, aman ),
						GuiUtils.getFullName( o2, aman ) );
			}
		} );

		final Map<String, Account> acctmap = new HashMap<>();
		for ( Account a : accounts ) {
			acctmap.put( usefull ? GuiUtils.getFullName( a, aman ) : a.getName(), a );
		}

		AccountStringConverter asc = new AccountStringConverter( acctmap );
		
		ComboBoxTableCell<T, Account> cell = new ComboBoxTableCell<T, Account>( asc, accounts ) {
			@Override
			public void updateItem( Account acct, boolean empty ) {
				super.updateItem( acct, empty );
				if ( null == acct || empty ) {
					setText( null );
				}
				else {
					setText( usefull
							? GuiUtils.getFullName( acct, aman )
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
			if ( null == t ) {
				return null;
			}

			return ( usefull ? GuiUtils.getFullName( t, aman ) : t.getName() );
		}

		@Override
		public Account fromString( String string ) {
			return acctmap.get( string );
		}
	}
}
