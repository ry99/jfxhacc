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
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

	public static void makeAnimatedLabel( Label lbl, int waitsec, int fadesec ) {
		lbl.textProperty().addListener( new ChangeListener<String>() {

			@Override
			public void changed( ObservableValue<? extends String> ov, String t, String t1 ) {
				lbl.setOpacity( 1.0 );
				

				new AnimationTimer() {
					// wait and fade times (in nanoseconds)
					private final long WAITLIMIT = waitsec * 1000000000l;
					private final long FADELIMIT = fadesec * 1000000000l;
					long start = 0;

					@Override
					public void handle( long l ) {
						if ( 0 == start ) {
							start = l;
						}

						long diff = l - start;
						if ( diff < WAITLIMIT ) {
							return;
						}
						diff -= WAITLIMIT;

						double pct = ( (double) diff / (double) FADELIMIT );
						lbl.setOpacity( 1.0 - pct );
						if ( pct > 1.0d ) {
							stop();
							lbl.setText( null );
						}
					}
				}.start();
			}
		} );

	}
}
