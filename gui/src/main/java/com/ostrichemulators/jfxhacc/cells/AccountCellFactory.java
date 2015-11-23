/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
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
		return new TableCell<T, Account>() {
			@Override
			protected void updateItem( Account acct, boolean empty ) {
				super.updateItem( acct, empty );
				if ( ( null == acct || empty ) ) {
					setText( null );
				}
				else {
					setText( usefull ? GuiUtils.getFullName( acct, amap ) : acct.getName() );
				}
			}
		};
	}
}
