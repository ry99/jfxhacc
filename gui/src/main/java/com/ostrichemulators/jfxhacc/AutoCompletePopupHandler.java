/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import java.text.Collator;
import java.util.ArrayList;
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
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.MenuItem;
import org.apache.log4j.Logger;

/**
 * This class is a TextField which implements an "autocomplete" functionality,
 * based on a supplied list of entries.
 *
 * @author ryan
 */
public class AutoCompletePopupHandler {

	private static final Logger log = Logger.getLogger( AutoCompletePopupHandler.class );
	private static final int maxShownEntries = 12;
	private ContextMenu entriesPopup = new ContextMenu();
	private final TextField text;
	private final SortedList<String> completions;

	/**
	 * Construct a new AutoCompleteTextField.
	 *
	 * @param text
	 * @param choices
	 */
	public AutoCompletePopupHandler( TextField text, ObservableList<String> choices ) {
		super();
		this.text = text;
		Collator col = Collator.getInstance();
		col.setStrength( Collator.SECONDARY ); // approximately case-insensitive

		this.completions = new SortedList<>( choices, col );
		FilteredList<String> filter = new FilteredList<>( this.completions );

		ObservableList<MenuItem> menuItems = entriesPopup.getItems();
		Map<String, CustomMenuItem> choicelkp = new HashMap<>();
		
		XXX need to worry about new entries being added to the choices list

		for ( String result : filter ) {
			Label entryLabel = new Label( result );
			CustomMenuItem choice = new CustomMenuItem( entryLabel, true );
			choicelkp.put( result, choice );

			choice.setOnAction( new EventHandler<ActionEvent>() {
				@Override
				public void handle( ActionEvent actionEvent ) {
					log.debug( "autocomplete item selected: " + result );
					entriesPopup.hide();
					text.setText( result );
					text.positionCaret( result.length() );
					actionEvent.consume();
				}
			} );
			
			menuItems.add( choice );
		}

		text.textProperty().addListener( new ChangeListener<String>() {
			@Override
			public void changed( ObservableValue<? extends String> observableValue,
					String oldtext, String newtext ) {
				entriesPopup.hide();
				filter.setPredicate( new Predicate<String>() {
					@Override
					public boolean test( String t ) {
						return ( t.toUpperCase().contains( newtext.toUpperCase() ) );
					}
				} );

				if ( !filter.isEmpty() ) {
					populatePopup( filter, 0 );
					entriesPopup.show( text, Side.BOTTOM, 0, 0 );
				}
			}
		} );
	}

	public void hidePopup() {
		entriesPopup.hide();
	}

	/**
	 * Get the existing set of autocomplete entries.
	 *
	 * @return The existing autocomplete entries.
	 */
	public ObservableList<String> getCompletions() {
		return completions;
	}

	public boolean isPoppedUp() {
		return entriesPopup.isShowing();
	}

	/**
	 * Populate the entry set with the given search results.
	 *
	 * @param choices The set of matching strings.
	 */
	private void populatePopup( ObservableList<String> choices, int start ) {
		List<CustomMenuItem> menuItems = new ArrayList<>();

		int end = Math.min( choices.size(), start + maxShownEntries );

		List<String> showables = new ArrayList<>( choices.subList( start, end ) );

		if ( start > 0 ) {
			Label moveup = new Label( "..." );
			CustomMenuItem item = new CustomMenuItem( moveup, true );

			menuItems.add( item );
			moveup.setOnMouseEntered( event -> {
				repopulateEntries( choices, start - 1 );
			} );
			item.setOnAction( event -> {
				repopulateEntries( choices, start - 1 );
			} );
		}

		for ( String result : showables ) {
			Label entryLabel = new Label( result );
			CustomMenuItem choice = new CustomMenuItem( entryLabel, true );
			choice.setOnAction( new EventHandler<ActionEvent>() {
				@Override
				public void handle( ActionEvent actionEvent ) {
					log.debug( "autocomplete item selected: " + result );
					entriesPopup.hide();
					text.setText( result );
					text.positionCaret( result.length() );
					actionEvent.consume();
				}
			} );

			menuItems.add( choice );
		}

		if ( choices.size() > ( start + maxShownEntries ) ) {
			Label movedown = new Label( "..." );
			CustomMenuItem item = new CustomMenuItem( movedown, true );

			menuItems.add( item );
			movedown.setOnMouseEntered( event -> {
				repopulateEntries( choices, start + 1 );
			} );
			item.setOnAction( event -> {
				repopulateEntries( choices, start + 1 );
			} );
		}

		entriesPopup.getItems().setAll( menuItems );
	}

	private void repopulateEntries( ObservableList<String> choices, int start ) {
		Platform.runLater( new Runnable() {

			@Override
			public void run() {
				populatePopup( choices, start );
			}
		} );
	}
}
