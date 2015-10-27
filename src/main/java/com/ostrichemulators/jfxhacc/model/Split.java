/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

/**
 *
 * @author ryan
 */
public interface Split extends IDable {

	public static enum ReconcileState {

		RECONCILED, CLEARED, NOT_RECONCILED
	};

	public String getMemo();

	public void setMemo( String memo );

	public Money getValue();

	public void setValue( Money m );

	public void setReconciled( ReconcileState rs );

	public ReconcileState getReconciled();
}
