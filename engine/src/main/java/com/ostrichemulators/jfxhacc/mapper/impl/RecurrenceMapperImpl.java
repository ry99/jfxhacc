/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.LoanMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.RecurrenceMapper;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Loan;
import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.Recurrence.Frequency;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.RecurrenceImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Loans;
import com.ostrichemulators.jfxhacc.model.vocabulary.Recurrences;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class RecurrenceMapperImpl extends RdfMapper<Recurrence>
		implements RecurrenceMapper {

	private static final Logger log = Logger.getLogger( RecurrenceMapperImpl.class );
	private final TransactionMapper tmap;
	private final LoanMapper lmap;

	public RecurrenceMapperImpl( RepositoryConnection repoc, TransactionMapper t,
			LoanMapper l ) {
		super( repoc, Recurrences.TYPE );
		tmap = t;
		lmap = l;
	}

	@Override
	public void remove( URI id ) throws MapperException {
		RepositoryConnection rc = getConnection();
		try {
			rc.remove( id, null, null );
			// get rid of the "model" transaction as well
			for ( Statement s : Iterations.asList( rc.getStatements( null, null, id, false ) ) ) {
				tmap.remove( URI.class.cast( s.getSubject() ) );
				lmap.remove( URI.class.cast( s.getSubject() ) );
			}
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Recurrence create( Recurrence r, Transaction t ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();

			URI id = createBaseEntity();

			rc.add( id, Recurrences.FREQUENCY_PRED,
					vf.createLiteral( r.getFrequency().toString() ) );
			rc.add( id, RDFS.LABEL, vf.createLiteral( r.getName() ) );
			if ( null != r.getNextRun() ) {
				rc.add( id, Recurrences.NEXTRUN_PRED,
						vf.createLiteral( r.getNextRun() ) );
			}

			if ( null != t ) {
				Transaction newt = tmap.create( t );
				rc.add( newt.getId(), Recurrences.TYPE, id );
			}

			RecurrenceImpl newr = new RecurrenceImpl( id, r.getFrequency(),
					r.getNextRun(), r.getName() );
			rc.commit();

			return newr;
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Recurrence create( Recurrence r, Loan l ) throws MapperException {
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();

			URI id = createBaseEntity();

			rc.add( id, Recurrences.FREQUENCY_PRED,
					vf.createLiteral( r.getFrequency().toString() ) );
			rc.add( id, RDFS.LABEL, vf.createLiteral( r.getName() ) );
			if ( null != r.getNextRun() ) {
				rc.add( id, Recurrences.NEXTRUN_PRED,
						vf.createLiteral( r.getNextRun() ) );
			}

			if ( null != l ) {
				Loan newt = lmap.create( l );
				rc.add( newt.getId(), Recurrences.TYPE, id );
			}

			RecurrenceImpl newr = new RecurrenceImpl( id, r.getFrequency(),
					r.getNextRun(), r.getName() );
			rc.commit();

			return newr;
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public Recurrence get( URI id ) throws MapperException {

		RecurrenceImpl trans = new RecurrenceImpl( id );
		Value fval = super.oneval( id, Recurrences.FREQUENCY_PRED );
		Value nrval = super.oneval( id, Recurrences.NEXTRUN_PRED );
		Value lval = super.oneval( id, RDFS.LABEL );

		trans.setFrequency( Frequency.valueOf( fval.stringValue() ) );
		trans.setName( lval.stringValue() );
		if ( null != nrval ) {
			trans.setNextRun( DbUtil.toDate( Literal.class.cast( nrval ) ) );
		}

		return trans;
	}

	@Override
	public void update( Recurrence t ) throws MapperException {

		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();

			rc.remove( t.getId(), Recurrences.FREQUENCY_PRED, null );
			rc.remove( t.getId(), Recurrences.NEXTRUN_PRED, null );
			rc.remove( t.getId(), RDFS.LABEL, null );

			rc.add( t.getId(), Recurrences.FREQUENCY_PRED,
					vf.createLiteral( t.getFrequency().toString() ) );
			rc.add( t.getId(), RDFS.LABEL, vf.createLiteral( t.getName() ) );

			if ( null != t.getNextRun() ) {
				rc.add( t.getId(), Recurrences.NEXTRUN_PRED,
						DbUtil.fromDate( t.getNextRun() ) );
			}

			rc.commit();
		}
		catch ( Exception me ) {
			rollback( rc );
			throw new MapperException( me );
		}
	}

	@Override
	public URI getObjectType( Recurrence r ) throws MapperException {
		Value val = oneval( "SELECT ?type WHERE { ?sub a ?type . ?sub jfxhacc:recurrence ?rec }",
				bindmap( "rec", r.getId() ) );
		return URI.class.cast( val );
	}

	@Override
	public void execute( Recurrence r ) throws MapperException {
		Calendar cal = Calendar.getInstance();
		cal.setTime( r.getNextRun() );

		URI type = getObjectType( r );
		if ( Transactions.TYPE.equals( type ) ) {
			Transaction t = tmap.get( r );
			t.setDate( r.getNextRun() );
			tmap.create( t );
		}
		else if ( Loans.TYPE.equals( type ) ) {
			Loan l = lmap.get( r );
		}

		r.setNextRun( getNextRun( r.getNextRun(), r.getFrequency() ) );

		if ( Frequency.ONCE == r.getFrequency() ) {
			remove( r );
		}
		else if ( Frequency.NEVER != r.getFrequency() ) {
			// check to make sure this recurrence hasn't already been run
			Recurrence dbr = get( r.getId() );
			if ( r.getNextRun().after( dbr.getNextRun() ) ) {
				update( r );
			}
		}
	}

	@Override
	public List<Recurrence> getDue( Date d ) throws MapperException {
		List<Recurrence> dues = new ArrayList<>();
		Collection<Recurrence> recs = getAll();
		for ( Recurrence r : recs ) {
			Date next = r.getNextRun();
			Calendar cal = Calendar.getInstance();
			cal.setTime( next );

			while ( !next.after( d ) ) {
				Frequency freq = r.getFrequency();
				if ( Frequency.NEVER != freq ) {
					Recurrence newr = new RecurrenceImpl( r );
					newr.setNextRun( next );
					dues.add( newr );
				}

				if ( Frequency.ONCE == freq || Frequency.NEVER == freq ) {
					break;
				}

				next = getNextRun( next, freq );
			}
		}

		return dues;
	}

	public static Date getNextRun( Date today, Frequency freq ) {
		Calendar cal = Calendar.getInstance();
		cal.setTime( today );

		switch ( freq ) {
			case DAILY:
				cal.add( Calendar.DAY_OF_YEAR, 1 );
				break;
			case WEEKLY:
				cal.add( Calendar.WEEK_OF_YEAR, 1 );
				break;
			case BIWEEKLY:
				cal.add( Calendar.WEEK_OF_YEAR, 2 );
				break;
			case END_OF_MONTH:
				cal.add( Calendar.MONTH, 1 );
				cal.set( Calendar.DAY_OF_MONTH,
						cal.getActualMaximum( Calendar.DAY_OF_MONTH ) );
				break;
			case MONTHLY:
				cal.add( Calendar.MONTH, 1 );
				break;
			case BIMONTHLY:
				cal.add( Calendar.MONTH, 2 );
				break;
			case QUARTERLY:
				cal.add( Calendar.MONTH, 3 );
				break;
			case SEMIYEARLY:
				cal.add( Calendar.MONTH, 6 );
				break;
			case YEARLY:
				cal.add( Calendar.YEAR, 1 );
				break;
		}

		return cal.getTime();
	}

	@Override
	public Collection<Recurrence> getAll( URI type ) throws MapperException {
		List<Recurrence> list = new ArrayList<>();
		for ( Recurrence r : getAll() ) {
			URI u = getObjectType( r );
			if ( u.equals( type ) ) {
				list.add( r );
			}
		}

		return list;
	}

	public static Date getNextRun( Recurrence rec ) {
		return getNextRun( rec.getNextRun(), rec.getFrequency() );
	}
}
