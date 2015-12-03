/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class is a TextField which implements an "autocomplete" functionality,
 * based on a supplied list of entries.
 *
 * @author Caleb Brinkman (from
 * https://gist.githubusercontent.com/floralvikings/10290131/raw/d36def081abebee4e6afb8773f68fc9dd5d66f77/AutoCompleteTextBox.java)
 *
 * @author ryan
 */
public class AutoCompletePopupHandler {

	/**
	 * The existing autocomplete entries.
	 */
	private final SortedSet<String> entries;
	/**
	 * The popup used to select an entry.
	 */
	private ContextMenu entriesPopup;
	private final TextField text;

	/**
	 * Construct a new AutoCompleteTextField.
	 */
	public AutoCompletePopupHandler( TextField text ) {
		super();
		this.text = text;
		entries = new TreeSet<>( String.CASE_INSENSITIVE_ORDER );
		entriesPopup = new ContextMenu();
		text.textProperty().addListener( new ChangeListener<String>() {
			@Override
			public void changed( ObservableValue<? extends String> observableValue, String s, String s2 ) {
				if ( text.getText().length() < 2 ) {
					entriesPopup.hide();
				}
				else {
					Map<String, Integer> searchResult = new HashMap<>();
					String upper = text.getText().toUpperCase();
					for ( String needle : entries ) {
						String uneedle = needle.toUpperCase();
						if ( uneedle.contains( upper ) ) {
							searchResult.put( needle, uneedle.indexOf( upper ) );
						}
					}

					// searchResult.addAll( entries.subSet( text.getText(),
					//		text.getText() + Character.MAX_VALUE ) );
					entriesPopup.hide();
					if ( entries.size() > 0 ) {
						List<String> results = new ArrayList<>( searchResult.keySet() );
						Collections.sort( results, new Comparator<String>() {

							@Override
							public int compare( String o1, String o2 ) {
								int diff = searchResult.get( o1 ) - searchResult.get( o2 );
								if ( 0 == diff ) {
									diff = o1.compareTo( o2 );
								}
								return diff;
							}
						} );

						populatePopup( results );
						entriesPopup.show( text, Side.BOTTOM, 0, 0 );
					}
				}
			}
		} );

		text.focusedProperty().addListener( event -> {
			entriesPopup.hide();
		} );
	}

	/**
	 * Get the existing set of autocomplete entries.
	 *
	 * @return The existing autocomplete entries.
	 */
	public SortedSet<String> getEntries() {
		return entries;
	}

	/**
	 * Populate the entry set with the given search results. Display is limited to
	 * 10 entries, for performance.
	 *
	 * @param searchResult The set of matching strings.
	 */
	private void populatePopup( List<String> searchResult ) {
		List<CustomMenuItem> menuItems = new ArrayList<>();
		// If you'd like more entries, modify this line.
		int maxEntries = 30;
		int count = Math.min( searchResult.size(), maxEntries );
		for ( int i = 0; i < count; i++ ) {
			final String result = searchResult.get( i );
			Label entryLabel = new Label( result );
			CustomMenuItem item = new CustomMenuItem( entryLabel, true );
			item.setOnAction( new EventHandler<ActionEvent>() {
				@Override
				public void handle( ActionEvent actionEvent ) {
					text.setText( result );
					text.positionCaret( result.length() );
					entriesPopup.hide();
				}
			} );
			menuItems.add( item );
		}
		entriesPopup.getItems().clear();
		entriesPopup.getItems().addAll( menuItems );
	}
}
