/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.datamanager.AccountManager;
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
	private final AccountManager aman;
	private final boolean useSplit;

	public AccountListCell( AccountManager amap, boolean saySplitOnEmpty ) {
		this.aman = amap;
		useSplit = saySplitOnEmpty;
	}

	@Override
	protected void updateItem( Account acct, boolean empty ) {
		super.updateItem( acct, empty );
		if ( null == acct || empty ) {
			setText( useSplit ? "Split" : null );
		}
		else {
			setText( GuiUtils.getFullName( acct, aman ) );
		}
	}
}
