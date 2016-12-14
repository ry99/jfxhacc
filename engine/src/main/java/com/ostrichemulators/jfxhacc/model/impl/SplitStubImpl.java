/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
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
public class SplitStubImpl extends AbstractSplitBase implements SplitStub {

	private static final Logger log = Logger.getLogger( SplitStubImpl.class );
	private final StringProperty number = new SimpleStringProperty();
	private final Property<Date> date = new SimpleObjectProperty<>();
	private final Property<URI> payee = new SimpleObjectProperty<>();
	private final Property<URI> acctid = new SimpleObjectProperty<>();
	private final Property<URI> jrnlid = new SimpleObjectProperty<>();
	private final Property<URI> tranid = new SimpleObjectProperty<>();

	public SplitStubImpl() {
		super( Splits.TYPE );
	}

	public SplitStubImpl( URI id ) {
		super( id );
	}

	public SplitStubImpl( Money m ) {
		super( m );
	}

	public SplitStubImpl( URI id, Money m ) {
		super( id, m );
	}

	public SplitStubImpl( URI jrnl, URI trans, URI acct, URI sid, Money m,
			String memo, URI payee, Date date, ReconcileState rs, String num ) {
		super( m, memo, rs );
		this.acctid.setValue( acct );
		this.jrnlid.setValue( jrnl );
		this.tranid.setValue( trans );
		this.payee.setValue( payee );
		this.date.setValue( date );
		this.number.setValue( num );
	}

	public SplitStubImpl( SplitStub s ) {
		this( s.getJournalId(), s.getTransactionId(), s.getAccountId(), s.getId(),
				s.getRawValueProperty().getValue(), s.getMemo(), s.getPayee(),
				s.getDate(), s.getReconciled(), s.getNumber() );
	}

	public SplitStubImpl( Transaction t, Split s ) {
		this( t.getJournal().getId(), t.getId(), s.getAccount().getId(), s.getId(),
				s.getRawValueProperty().getValue(), s.getMemo(), t.getPayee().getId(),
				t.getDate(), s.getReconciled(), t.getNumber() );
	}

	@Override
	public StringBinding getAnyChangeProperty() {
		return Bindings.createStringBinding( new Callable<String>() {
			@Override
			public String call() throws Exception {
				return getMemo() + getAccountId().toString()
						+ getValue().toString() + getReconciled();

			}
		}, getMemoProperty(), this.acctid, this.jrnlid, this.tranid,
				this.getReconciledProperty(), this.getValueProperty(),
				this.payee, this.date );
	}

	@Override
	public String toString() {
		return "SplitStubImpl{" + "date=" + date + ", payee=" + payee
				+ ", memo=" + getMemo() + ", value=" + getValue() + ", isdebit=" + this.isDebit()
				+ ", reco=" + getReconciled() + ", acctid=" + acctid + ", jrnlid=" + jrnlid
				+ ", tranid=" + tranid + '}';
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + Objects.hashCode( this.date );
		hash = 71 * hash + Objects.hashCode( this.payee );
		hash = 71 * hash + Objects.hashCode( this.getMemo() );
		hash = 71 * hash + Objects.hashCode( this.getValue() );
		hash = 71 * hash + ( this.isDebit() ? 1 : 0 );
		hash = 71 * hash + Objects.hashCode( this.getReconciled() );
		hash = 71 * hash + Objects.hashCode( this.acctid );
		hash = 71 * hash + Objects.hashCode( this.jrnlid );
		hash = 71 * hash + Objects.hashCode( this.tranid );
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		final SplitStubImpl other = (SplitStubImpl) obj;
		return super.equals( obj );
	}

	@Override
	public void setAccountId( URI a ) {
		acctid.setValue( a );
	}

	@Override
	public URI getAccountId() {
		return acctid.getValue();
	}

	@Override
	public Property<URI> getAccountIdProperty() {
		return acctid;
	}

	@Override
	public void setTransactionId( URI a ) {
		tranid.setValue( a );
	}

	@Override
	public URI getTransactionId() {
		return tranid.getValue();
	}

	@Override
	public Property<URI> getTransactionIdProperty() {
		return tranid;
	}

	@Override
	public void setJournalId( URI a ) {
		jrnlid.setValue( a );
	}

	@Override
	public URI getJournalId() {
		return jrnlid.getValue();
	}

	@Override
	public Property<URI> getJournalIdProperty() {
		return jrnlid;
	}

	@Override
	public void setPayee( URI a ) {
		payee.setValue( a );
	}

	@Override
	public URI getPayee() {
		return payee.getValue();
	}

	@Override
	public Property<URI> getPayeeProperty() {
		return payee;
	}

	@Override
	public void setDate( Date a ) {
		date.setValue( a );
	}

	@Override
	public Date getDate() {
		return date.getValue();
	}

	@Override
	public Property<Date> getDateProperty() {
		return date;
	}

	@Override
	public String getNumber() {
		return number.get();
	}

	@Override
	public void setNumber( String num ) {
		number.set( num );
	}

	@Override
	public StringProperty getNumberProperty() {
		return number;
	}
}
