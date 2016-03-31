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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

		Map<Account, String> fullnames = new HashMap<>();
		for ( Account a : accounts ) {
			fullnames.put( a, GuiUtils.getFullName( a, amap ) );
		}

		SortedList<Account> sorted = new SortedList<>( accounts, new Comparator<Account>() {
			@Override
			public int compare( Account o1, Account o2 ) {
				return Collator.getInstance().compare( fullnames.get( o1 ), fullnames.get( o2 ) );
			}
		} );

		FilteredList<Account> filtered = new FilteredList<>( sorted );
		field.setItems( filtered );

		field.setOnKeyPressed( new EventHandler<KeyEvent>() {
			private final StringBuilder current = new StringBuilder();

			@Override
			public void handle( KeyEvent t ) {
				KeyCode code = t.getCode();
				t.consume();
				boolean refilter = false;
				if ( KeyCode.UP == code || KeyCode.DOWN == code || KeyCode.DELETE == code ) {
					current.delete( 0, current.length() );
					filtered.setPredicate( null );
				}
				else if ( KeyCode.BACK_SPACE == code ) {
					if ( current.length() > 0 ) {
						current.delete( current.length() - 1, current.length() );
					}
					refilter = true;
				}
				else if ( KeyCode.RIGHT == code ) {
					current.replace( 0, current.length(), fullnames.get( field.getValue() ) );
					refilter = true;
				}
				else if ( code.isLetterKey() ) {
					refilter = true;
					current.append( t.getText() );
				}

				log.debug( "account filter text is: " + current );

				if ( refilter ) {
					String upper = current.toString().toUpperCase();
					filtered.setPredicate( ( Account item ) -> {
						return fullnames.get( item ).toUpperCase().contains( upper );
					} );

					if ( !filtered.isEmpty() ) {
						field.setValue( filtered.get( 0 ) );
					}
				}
				field.show();
			}
		} );

		field.valueProperty().addListener( new ChangeListener<Account>() {

			@Override
			public void changed( ObservableValue<? extends Account> ov, Account t, Account t1 ) {
				if ( null == t1 ) {
					filtered.setPredicate( null );
				}
			}
		} );

		field.setButtonCell( new AccountListCell( amap, false ) );
		field.setCellFactory( new Callback<ListView<Account>, ListCell<Account>>() {

			@Override
			public ListCell<Account> call( ListView<Account> p ) {
				return new AccountListCell( amap, false );
			}
		} );

		return sorted;
	}

	public static void makeAnimatedLabel( Label lbl, int waitsec, int fadesec ) {

		AnimationTimer at = new AnimationTimer() {
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

			@Override
			public void start() {
				start = 0;
				super.start();
			}
		};

		lbl.textProperty().addListener( new ChangeListener<String>() {

			@Override
			public void changed( ObservableValue<? extends String> ov, String t, String t1 ) {
				lbl.setOpacity( 1.0 );
				if ( null != t1 ) {
					at.start();
				}
			}
		} );
	}

	public static Map<Account, TreeItem<Account>> makeAccountTree( Map<Account, Account> childparentlkp,
			TreeItem<Account> root )  {
		Map<Account, TreeItem<Account>> items = new HashMap<>();

		for ( Account acct : childparentlkp.keySet() ) {
			TreeItem<Account> aitem = new TreeItem<>( acct );
			items.put( acct, aitem );
		}

		for ( Map.Entry<Account, Account> en : childparentlkp.entrySet() ) {
			Account child = en.getKey();
			Account parent = en.getValue();
			TreeItem<Account> childitem = items.get( child );
			TreeItem<Account> parentitem
					= ( null == parent ? root : items.get( parent ) );
			parentitem.getChildren().add( childitem );
		}

		return items;
	}
}
