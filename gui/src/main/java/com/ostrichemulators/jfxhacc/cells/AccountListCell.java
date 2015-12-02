/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import javafx.scene.control.ListCell;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class AccountListCell extends ListCell<Account> {

	private static final Logger log = Logger.getLogger( AccountListCell.class );
	private final AccountMapper amap;

	public AccountListCell( AccountMapper amap ) {
		this.amap = amap;
	}

	@Override
	protected void updateItem( Account acct, boolean empty ) {
		super.updateItem( acct, empty );
		if ( null == acct || empty ) {
			setText( "Split" );
		}
		else {
			setText( GuiUtils.getFullName( acct, amap ) );
		}
	}
}
