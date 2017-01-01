/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.SplitBase;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import java.util.concurrent.Callable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public abstract class AbstractSplitBase extends IDableImpl implements SplitBase {

	private static final Logger log = Logger.getLogger( AbstractSplitBase.class );

	private final StringProperty memo = new SimpleStringProperty();
	private final Property<Money> value = new SimpleObjectProperty<>( new Money() );
	private boolean isdebit = false;
	private final Property<SplitBase.ReconcileState> reco
			= new SimpleObjectProperty<>( SplitBase.ReconcileState.NOT_RECONCILED );

	protected AbstractSplitBase() {
		super( Splits.TYPE );
	}

	protected AbstractSplitBase( URI id ) {
		super( Splits.TYPE, id );
	}

	protected AbstractSplitBase( Money m ) {
		super( Splits.TYPE );
		isdebit = m.isNegative();
		value.setValue( m.abs() );
	}

	protected AbstractSplitBase( URI id, Money m ) {
		super( Splits.TYPE, id );
		isdebit = m.isNegative();
		value.setValue( m.abs() );
	}

	protected AbstractSplitBase( Money m, String memo, ReconcileState rs ) {
		this( m );
		this.memo.set( memo );
		this.reco.setValue( rs );
	}

	protected AbstractSplitBase( SplitBase s ) {
		this( s.getRawValueProperty().getValue(), s.getMemo(), s.getReconciled() );
		super.setId( s.getId() );
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
		return value.getValue();
	}

	@Override
	@Deprecated
	public void setValue( Money m ) {
		isdebit = m.isNegative();
		value.setValue( m.abs() );
	}

	@Override
	public void setCredit( Money m ) {
		isdebit = false;
		value.setValue( m.abs() );
	}

	@Override
	public void setDebit( Money m ) {
		isdebit = true;
		value.setValue( m.abs() );
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
		reco.setValue( rs );
	}

	@Override
	public ReconcileState getReconciled() {
		return reco.getValue();
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
	public ReadOnlyProperty<Money> getRawValueProperty() {
		SimpleObjectProperty<Money> prop = new SimpleObjectProperty<>();
		prop.bind( Bindings.createObjectBinding( new Callable<Money>() {
			@Override
			public Money call() throws Exception {
				Money val = getValue();
				// val is always positive
				// but we've arbitrarily declared that we'll use negative numbers
				// to represent debits and positive ones to represent credits
				return ( isdebit ? val.opposite() : val );
			}
		}, value ) );
		return prop;
	}

	@Override
	public ReadOnlyProperty<Money> getCreditProperty() {
		SimpleObjectProperty<Money> prop = new SimpleObjectProperty<>();
		prop.bind( Bindings.createObjectBinding( new Callable<Money>() {
			@Override
			public Money call() throws Exception {
				return ( isdebit ? new Money() : value.getValue() );
			}
		}, value ) );

		return prop;
	}

	@Override
	public ReadOnlyProperty<Money> getDebitProperty() {
		SimpleObjectProperty<Money> prop = new SimpleObjectProperty<>();
		prop.bind( Bindings.createObjectBinding( new Callable<Money>() {
			@Override
			public Money call() throws Exception {
				return ( isdebit ? value.getValue() : new Money() );
			}
		}, value ) );

		return prop;
	}

	@Override
	public Property<ReconcileState> getReconciledProperty() {
		return reco;
	}

	@Override
	public Money add( Money m ) {
		Money old = value.getValue();
		if ( !isdebit ) {
			old = old.opposite();
		}

		Money newmoney = old.plus( m );
		if( newmoney.isNegative() ){
			setDebit( newmoney );
		}
		else{
			setCredit( newmoney );
		}

		return getValue();
	}

	@Override
	public String toString() {
		return ( isDebit() ? "debit" : "credit" ) + ": " + value
				+ " {" + reco.getValue().toString().charAt( 0 ) + "}";
	}
}
