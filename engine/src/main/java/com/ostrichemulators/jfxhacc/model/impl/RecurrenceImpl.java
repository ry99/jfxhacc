/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.vocabulary.Recurrences;
import java.util.Date;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class RecurrenceImpl extends IDableImpl implements Recurrence {

	private static final Logger log = Logger.getLogger( RecurrenceImpl.class );
	private final ObjectProperty<Frequency> freq
			= new SimpleObjectProperty<>( Frequency.NEVER );
	private final ObjectProperty<Date> next = new SimpleObjectProperty<>();
	private final StringProperty name = new SimpleStringProperty();

	public RecurrenceImpl() {
		super( Recurrences.TYPE );
	}

	public RecurrenceImpl( URI id ) {
		super( Recurrences.TYPE, id );
	}

	public RecurrenceImpl( URI id, Frequency f, Date nextrun, String anme ) {
		this( id );
		freq.set( f );
		next.set( nextrun );
		name.set( anme );
	}

	public RecurrenceImpl( Recurrence t ) {
		this( t.getId(), t.getFrequency(), t.getNextRun(), t.getName() );
	}

	@Override
	public void setFrequency( Frequency f ) {
		freq.set( f );
	}

	@Override
	public Frequency getFrequency() {
		return freq.get();
	}

	@Override
	public Property<Frequency> getFrequencyProperty() {
		return freq;
	}

	@Override
	public void setNextRun( Date d ) {
		next.set( d );
	}

	@Override
	public Date getNextRun() {
		return next.get();
	}

	@Override
	public Property<Date> getNextRunProperty() {
		return next;
	}

	@Override
	public String getName() {
		return name.get();
	}

	@Override
	public void setName( String n ) {
		name.set( n );
	}

	@Override
	public StringProperty getNameProperty() {
		return name;
	}

	@Override
	public boolean isDue( Date d ) {
		return next.get().before( d );
	}
}
