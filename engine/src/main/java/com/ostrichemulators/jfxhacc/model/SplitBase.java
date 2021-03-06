/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author ryan
 */
public interface SplitBase extends IDable {

	public static enum ReconcileState {

		NOT_RECONCILED, CLEARED, RECONCILED,
	};

	public String getMemo();

	public void setMemo( String memo );

	public StringProperty getMemoProperty();

	/**
	 * Gets the (always positive) value of this split.
	 *
	 * @return
	 */
	public Money getValue();

	public Property<Money> getValueProperty();

	public void setCredit( Money m );

	public void setDebit( Money m );

	public ReadOnlyProperty<Money> getRawValueProperty();

	public ReadOnlyProperty<Money> getCreditProperty();

	public ReadOnlyProperty<Money> getDebitProperty();

	/**
	 * Sets the value of this split.
	 *
	 * @param m the value. If negative, {@link #isDebit()} will be true.
	 * @deprecated
	 */
	@Deprecated
	public void setValue( Money m );

	public boolean isDebit();

	public boolean isCredit();

	public void setReconciled( ReconcileState rs );

	public ReconcileState getReconciled();

	public Property<ReconcileState> getReconciledProperty();

	/**
	 * Adds the given money to this split's value
	 *
	 * @param m
	 * @return
	 */
	public Money add( Money m );
}
