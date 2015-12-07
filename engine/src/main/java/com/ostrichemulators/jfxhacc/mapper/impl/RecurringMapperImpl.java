/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.mapper.RecurringMapper;
import com.ostrichemulators.jfxhacc.mapper.TransactionListener;
import com.ostrichemulators.jfxhacc.model.Recurring;
import com.ostrichemulators.jfxhacc.model.Recurring.Frequency;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.RecurringImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.model.vocabulary.Recurrings;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class RecurringMapperImpl extends RdfMapper<Recurring>
		implements RecurringMapper {

	private static final Logger log = Logger.getLogger( RecurringMapperImpl.class );
	private final List<TransactionListener> listenees = new ArrayList<>();
	private final TransactionMapperImpl tmap;

	public RecurringMapperImpl( RepositoryConnection repoc, AccountMapper amap, PayeeMapper pmap ) {
		super( repoc, JfxHacc.RECURRING_TYPE );
		tmap = new TransactionMapperImpl( repoc, amap, pmap, JfxHacc.RECURRING_TYPE );
	}

	@Override
	public Recurring create( Recurring r ) throws MapperException {
		Transaction trans = tmap.create( r, r.getJournal() );

		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();

			rc.add( trans.getId(), Recurrings.FREQUENCY_PRED,
					vf.createLiteral( r.getFrequency().toString() ) );
			if ( null != r.getNextRun() ) {
				rc.add( trans.getId(), Recurrings.NEXTRUN_PRED,
						vf.createLiteral( r.getNextRun() ) );
			}

			RecurringImpl newr = RecurringImpl.fromTrans( trans );
			newr.setFrequency( r.getFrequency() );
			newr.setNextRun( r.getNextRun() );
			rc.commit();

			return newr;
		}
		catch ( RepositoryException re ) {
			rollback( rc );
			throw new MapperException( re );
		}
	}

	@Override
	public void remove( URI id ) throws MapperException {
		tmap.remove( id );
	}

	@Override
	public Recurring get( URI id ) throws MapperException {
		Transaction t = tmap.get( id );
		RecurringImpl trans = RecurringImpl.fromTrans( t );
		Value fval = super.oneval( id, Recurrings.FREQUENCY_PRED );
		Value nrval = super.oneval( id, Recurrings.NEXTRUN_PRED );

		trans.setFrequency( Frequency.valueOf( fval.stringValue() ) );
		if ( null != nrval ) {
			trans.setNextRun( DbUtil.toDate( Literal.class.cast( nrval ) ) );
		}

		return trans;
	}

	@Override
	public void update( Recurring t ) throws MapperException {
		tmap.update( t );
		RepositoryConnection rc = getConnection();
		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();
			
			rc.remove( t.getId(), Recurrings.FREQUENCY_PRED, null );
			rc.remove( t.getId(), Recurrings.NEXTRUN_PRED, null );

			rc.add( t.getId(), Recurrings.FREQUENCY_PRED,
					vf.createLiteral( t.getFrequency().toString() ) );

			if ( null != t.getNextRun() ) {
				rc.add( t.getId(), Recurrings.NEXTRUN_PRED,
						DbUtil.fromDate( t.getNextRun() ) );
			}

			rc.commit();
		}
		catch ( Exception me ) {
			rollback( rc );
			throw new MapperException( me );
		}
	}
}
