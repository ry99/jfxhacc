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
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class SplitImpl extends IDableImpl implements Split {

	private String memo = "";
	private Money value = new Money();
	private boolean isdebit = false;
	private ReconcileState reco = ReconcileState.NOT_RECONCILED;

	public SplitImpl() {
		super( JfxHacc.SPLIT_TYPE );
	}

	public SplitImpl( URI id ) {
		super( JfxHacc.SPLIT_TYPE, id );
	}

	public SplitImpl( Money m ) {
		super( JfxHacc.SPLIT_TYPE );
		isdebit = m.isNegative();
		value = m.abs();
	}

	public SplitImpl( URI id, Money m ) {
		super( JfxHacc.SPLIT_TYPE, id );
		isdebit = m.isNegative();
		value = m.abs();
	}

	@Override
	public String getMemo() {
		return memo;
	}

	@Override
	public void setMemo( String memo ) {
		this.memo = memo;
	}

	@Override
	public Money getValue() {
		return value;
	}

	@Override
	public void setValue( Money m ) {
		isdebit = m.isNegative();
		value = m.abs();
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
		reco = rs;
	}

	@Override
	public ReconcileState getReconciled() {
		return reco;
	}

	@Override
	public String toString() {
		return ( isDebit() ? "debit" : "credit" ) + ": " + value;
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

}
