/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import java.text.Collator;
import java.util.ArrayList;
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
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
	private final SortedList<String> completions;

	/**
	 * Construct a new AutoCompleteTextField.
	 *
	 * @param text
	 * @param choices
	 */
	public AutoCompletePopupHandler( TextField text, ObservableList<String> choices ) {
		super();
		Collator col = Collator.getInstance();
		col.setStrength( Collator.SECONDARY ); // approximately case-insensitive

		this.completions = new SortedList<>( choices, col );
		FilteredList<String> filter = new FilteredList<>( this.completions );

		text.textProperty().addListener( new ChangeListener<String>() {
			@Override
			public void changed( ObservableValue<? extends String> observableValue,
					String oldtext, String newtext ) {
				filter.setPredicate( new Predicate<String>() {
					@Override
					public boolean test( String t ) {
						return ( t.toUpperCase().contains( newtext.toUpperCase() ) );
					}
				} );

				if ( !filter.isEmpty() ) {
					createAndShowMenu( text, filter, 0 );
				}
			}
		} );
	}

	/**
	 * Get the existing set of autocomplete entries.
	 *
	 * @return The existing autocomplete entries.
	 */
	public ObservableList<String> getCompletions() {
		return completions;
	}

	/**
	 * Populate the entry set with the given search results.
	 *
	 * @param choices The set of matching strings.
	 */
	private static void createAndShowMenu( TextField tf,
			ObservableList<String> choices, int start ) {

		final ContextMenu menu = new ContextMenu();
		int end = Math.min( choices.size(), start + maxShownEntries );

		List<String> showables = new ArrayList<>( choices.subList( start, end ) );

		if ( start > 0 ) {
			Label moveup = new Label( "..." );
			CustomMenuItem item = new CustomMenuItem( moveup, true );

			menu.getItems().add( item );
			moveup.setOnMouseEntered( event -> {
				repopulateEntries( choices, start - 1, tf, menu );
			} );
			item.setOnAction( event -> {
				repopulateEntries( choices, start - 1, tf, menu );
			} );
		}

		for ( String hit : showables ) {
			CustomMenuItem choice = createMenuItem( hit );

			choice.setOnAction( new EventHandler<ActionEvent>() {
				@Override
				public void handle( ActionEvent actionEvent ) {
					log.debug( "autocomplete item selected: " + hit );
					menu.hide();
					tf.setText( hit );
					tf.positionCaret( hit.length() );
					actionEvent.consume();
				}
			} );

			menu.getItems().add( choice );
		}

		if ( choices.size() > ( start + maxShownEntries ) ) {
			Label movedown = new Label( "..." );
			CustomMenuItem item = new CustomMenuItem( movedown, true );

			menu.getItems().add( item );
			movedown.setOnMouseEntered( event -> {
				repopulateEntries( choices, start + 1, tf, menu );
			} );
			item.setOnAction( event -> {
				repopulateEntries( choices, start + 1, tf, menu );
			} );
		}

		menu.show( tf, Side.BOTTOM, 0, 0 );
		tf.textProperty().addListener( new InvalidationListener() {
			@Override
			public void invalidated( Observable observable ) {
				menu.hide();
			}
		} );
	}

	private static void repopulateEntries( ObservableList<String> choices, int start,
			TextField tf, ContextMenu menu ) {
		Platform.runLater( new Runnable() {

			@Override
			public void run() {
				menu.hide();
				createAndShowMenu( tf, choices, start );
			}
		} );
	}

	private static CustomMenuItem createMenuItem( String text ) {
		Label entryLabel = new Label( text );
		CustomMenuItem choice = new CustomMenuItem( entryLabel, true );
		return choice;
	}
}
