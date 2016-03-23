/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.JournalMapper;
import com.ostrichemulators.jfxhacc.mapper.LoanMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.model.Loan;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.impl.LoanImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Loans;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class LoanMapperImpl extends SimpleEntityRdfMapper<Loan>
		implements LoanMapper {

	private static final Logger log = Logger.getLogger( LoanMapperImpl.class );
	private final AccountMapper amap;
	private final JournalMapper jmap;

	public LoanMapperImpl( RepositoryConnection repoc, AccountMapper amap,
			JournalMapper jmap ) {
		super( repoc, Loans.TYPE );
		this.amap = amap;
		this.jmap = jmap;
	}

	@Override
	public Loan create( Loan t ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		boolean active = false;
		try {
			active = rc.isActive();

			if ( !active ) {
				rc.begin();
			}

			URI id = createBaseEntity();

			rc.add( id, Loans.PCT_PRED, vf.createLiteral( t.getApr() ) );
			rc.add( id, Loans.NUMPAYMENTS_PRED, vf.createLiteral( t.getNumberOfPayments() ) );
			rc.add( id, Loans.VALUE_PRED, vf.createLiteral( t.getInitialValue().value() ) );
			rc.add( id, Loans.INTEREST_PRED, t.getInterestAccount().getId() );
			rc.add( id, Loans.PRINCIPAL_PRED, t.getPrincipalAccount().getId() );
			rc.add( id, Loans.FROM_PRED, t.getSourceAccount().getId() );
			rc.add( id, Loans.JOURNAL_PRED, t.getJournal().getId() );

			if ( !active ) {
				rc.commit();
			}

			LoanImpl loan
					= new LoanImpl( id, t.getApr(), t.getInitialValue(), t.getNumberOfPayments(),
							t.getPrincipalAccount(), t.getInterestAccount(), t.getSourceAccount() );
			notifyAdded( loan );
			return loan;
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Loan get( URI id ) throws MapperException {
		Map<String, Value> bindings = new HashMap<>();
		bindings.put( "id", id );
		Loan loan = new LoanImpl( id );

		return query( "SELECT ?p ?o WHERE { ?id ?p ?o }",
				bindings, new QueryHandler<Loan>() {

					@Override
					public void handleTuple( BindingSet set, ValueFactory vf ) {
						final URI uri = URI.class.cast( set.getValue( "p" ) );
						Value val = set.getValue( "o" );

						if ( Loans.NUMPAYMENTS_PRED.equals( uri ) ) {
							loan.setNumberOfPayments( Literal.class.cast( val ).intValue() );
						}
						else if ( Loans.PCT_PRED.equals( uri ) ) {
							loan.setApr( Literal.class.cast( val ).doubleValue() );
						}
						else if ( Loans.VALUE_PRED.equals( uri ) ) {
							loan.setInitialValue( new Money( Literal.class.cast( val ).intValue() ) );
						}
						else if ( Loans.JOURNAL_PRED.equals( uri ) ) {
							try {
								loan.setJournal( jmap.get( URI.class.cast( val ) ) );
							}
							catch ( MapperException me ) {
								log.error( me, me );
							}
						}
						else if ( Loans.PRINCIPAL_PRED.equals( uri ) ) {
							try {
								loan.setPrincipalAccount( amap.get( URI.class.cast( val ) ) );
							}
							catch ( MapperException me ) {
								log.error( me, me );
							}
						}
						else if ( Loans.INTEREST_PRED.equals( uri ) ) {
							try {
								loan.setInterestAccount( amap.get( URI.class.cast( val ) ) );
							}
							catch ( MapperException me ) {
								log.error( me, me );
							}
						}
						else if ( Loans.FROM_PRED.equals( uri ) ) {
							try {
								loan.setSourceAccount( amap.get( URI.class.cast( val ) ) );
							}
							catch ( MapperException me ) {
								log.error( me, me );
							}
						}
					}

					@Override
					public Loan getResult() {
						return loan;
					}
				} );
	}

	@Override
	public void update( Loan t ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();

		try {
			rc.begin();

			URI id = t.getId();
			rc.remove( id, Loans.NUMPAYMENTS_PRED, null );
			rc.remove( id, Loans.PCT_PRED, null );
			rc.remove( id, Loans.VALUE_PRED, null );
			rc.remove( id, Loans.PRINCIPAL_PRED, null );
			rc.remove( id, Loans.INTEREST_PRED, null );
			rc.remove( id, Loans.FROM_PRED, null );
			rc.remove( id, Loans.JOURNAL_PRED, null );

			rc.add( id, Loans.PCT_PRED, vf.createLiteral( t.getApr() ) );
			rc.add( id, Loans.NUMPAYMENTS_PRED, vf.createLiteral( t.getNumberOfPayments() ) );
			rc.add( id, Loans.VALUE_PRED, vf.createLiteral( t.getInitialValue().value() ) );
			rc.add( id, Loans.PRINCIPAL_PRED, t.getPrincipalAccount().getId() );
			rc.add( id, Loans.INTEREST_PRED, t.getInterestAccount().getId() );
			rc.add( id, Loans.FROM_PRED, t.getSourceAccount().getId() );
			rc.add( id, Loans.JOURNAL_PRED, t.getJournal().getId() );

			rc.commit();

			notifyUpdated( t );
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Loan get( Recurrence r ) throws MapperException {
		Value val = oneval( "SELECT ?id WHERE { ?id jfxhacc:recurrence ?rec }",
				bindmap( "rec", r.getId() ) );

		if ( null == val ) {
			return null;
		}

		Loan loan = get( URI.class.cast( val ) );
		return loan;
	}
}
