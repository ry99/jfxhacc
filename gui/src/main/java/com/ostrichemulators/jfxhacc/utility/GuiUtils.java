/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.cells.AccountListCell;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class GuiUtils {

	public static final Logger log = Logger.getLogger( GuiUtils.class );

	private GuiUtils() {
	}

	public static String getFullName( Account a, AccountMapper amap ) {
		try {
			List<Account> parents = amap.getParents( a );
			StringBuilder sb = new StringBuilder();
			for ( Account parent : parents ) {
				sb.append( parent.getName() ).append( "::" );
			}
			sb.append( a.getName() );
			return sb.toString();
		}
		catch ( MapperException me ) {
			log.warn( me, me );
		}

		return a.getName();
	}

	public static SortedList<Account> makeAccountCombo( ComboBox<Account> field,
			Collection<Account> accts, AccountMapper amap ) {
		ObservableList<Account> accounts = FXCollections.observableArrayList( accts );

		SortedList<Account> sorted = new SortedList<>( accounts );
		sorted.setComparator( new Comparator<Account>() {

			@Override
			public int compare( Account o1, Account o2 ) {
				return Collator.getInstance().compare( GuiUtils.getFullName( o1, amap ),
						GuiUtils.getFullName( o2, amap ) );
			}
		} );

		field.setItems( sorted );

		field.setButtonCell( new AccountListCell( amap ) );
		field.setCellFactory( new Callback<ListView<Account>, ListCell<Account>>() {

			@Override
			public ListCell<Account> call( ListView<Account> p ) {
				return new AccountListCell( amap );
			}
		} );

		return sorted;
	}
}
