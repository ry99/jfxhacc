/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import java.util.Objects;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class SplitImpl extends IDableImpl implements Split {

	private final StringProperty memo = new SimpleStringProperty();
	private final SimpleObjectProperty<Money> value = new SimpleObjectProperty<>( new Money() );
	private boolean isdebit = false;
	private final SimpleObjectProperty<ReconcileState> reco
			= new SimpleObjectProperty<>( ReconcileState.NOT_RECONCILED );

	public SplitImpl() {
		super( JfxHacc.SPLIT_TYPE );
	}

	public SplitImpl( URI id ) {
		super( JfxHacc.SPLIT_TYPE, id );
	}

	public SplitImpl( Money m ) {
		super( JfxHacc.SPLIT_TYPE );
		isdebit = m.isNegative();
		value.set( m.abs() );
	}

	public SplitImpl( URI id, Money m ) {
		super( JfxHacc.SPLIT_TYPE, id );
		isdebit = m.isNegative();
		value.set( m.abs() );
	}

	@Override
	public String getMemo() {
		return memo.get();
	}

	@Override
	public void setMemo( String memo ) {
		this.memo.set( memo );
	}

	@Override
	public Money getValue() {
		return value.get();
	}

	@Override
	public void setValue( Money m ) {
		isdebit = m.isNegative();
		value.set( m.abs() );
	}

	@Override
	public boolean isDebit() {
		return isdebit;
	}

	@Override
	public boolean isCredit() {
		return !isDebit();
	}

	@Override
	public void setReconciled( ReconcileState rs ) {
		reco.set( rs );
	}

	@Override
	public ReconcileState getReconciled() {
		return reco.get();
	}

	@Override
	public String toString() {
		return ( isDebit() ? "debit" : "credit" ) + ": " + value
				+ " {" + reco.getValue().toString().charAt( 0 ) + "}";
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + Objects.hashCode( this.memo );
		hash = 13 * hash + Objects.hashCode( this.value );
		hash = 13 * hash + ( this.isdebit ? 1 : 0 );
		hash = 13 * hash + Objects.hashCode( this.reco );
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		final SplitImpl other = (SplitImpl) obj;
		if ( !Objects.equals( this.memo, other.memo ) ) {
			return false;
		}
		if ( !Objects.equals( this.value, other.value ) ) {
			return false;
		}
		if ( this.isdebit != other.isdebit ) {
			return false;
		}
		if ( this.reco != other.reco ) {
			return false;
		}

		return getId().equals( other.getId() );
	}

	@Override
	public StringProperty getMemoProperty() {
		return memo;
	}

	@Override
	public Property<Money> getValueProperty() {
		return value;
	}

	@Override
	public Property<ReconcileState> getReconciledProperty() {
		return reco;
	}

}
