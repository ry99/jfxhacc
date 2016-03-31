/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Account;
import javafx.scene.control.TreeCell;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class AccountTreeCell extends TreeCell<Account> {

	private static final Logger log = Logger.getLogger( AccountTreeCell.class );

	@Override
	protected void updateItem( Account item, boolean empty ) {
		super.updateItem( item, empty );
		setText( empty || null == item ? "" : item.getName() );
	}
}
