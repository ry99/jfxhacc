/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.engine.impl;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.DataMapper;
import com.ostrichemulators.jfxhacc.mapper.JournalMapper;
import com.ostrichemulators.jfxhacc.mapper.LoanMapper;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.mapper.RecurrenceMapper;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.mapper.impl.AccountMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.JournalMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.LoanMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.PayeeMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.RecurrenceMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.TransactionMapperImpl;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import static org.openrdf.rio.RDFFormat.NTRIPLES;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 *
 * @author ryan
 */
public class RdfDataEngine implements DataEngine {

	private static final Logger log = Logger.getLogger( RdfDataEngine.class );
	private final RepositoryConnection rc;
	private final AccountMapper amap;
	private final JournalMapper jmap;
	private final PayeeMapper pmap;
	private final TransactionMapper tmap;
	private final RecurrenceMapper rmap;
	private final LoanMapper lmap;

	public RdfDataEngine( RepositoryConnection conn ) {
		rc = conn;
		amap = new AccountMapperImpl( rc );
		jmap = new JournalMapperImpl( rc );
		pmap = new PayeeMapperImpl( rc );
		tmap = new TransactionMapperImpl( rc, amap, pmap, jmap );
		lmap = new LoanMapperImpl( rc );
		rmap = new RecurrenceMapperImpl( rc, tmap, lmap );
	}

	@Override
	public AccountMapper getAccountMapper() {
		return amap;
	}

	@Override
	public JournalMapper getJournalMapper() {
		return jmap;
	}

	@Override
	public PayeeMapper getPayeeMapper() {
		return pmap;
	}

	@Override
	public TransactionMapper getTransactionMapper() {
		return tmap;
	}

	@Override
	public RecurrenceMapper getRecurrenceMapper() {
		return rmap;
	}

	@Override
	public LoanMapper getLoanMapper() {
		return lmap;
	}

	@Override
	public void release() {
		for ( DataMapper<?> mapper : Arrays.asList( amap, pmap, tmap, jmap ) ) {
			mapper.release();
		}
	}

	/**
	 * Dumps the data to the given file. If the file is null, dumps it in NTriples
	 * format to stdout. If the file isn't null, the extension is used to
	 * determine the format (either nt, ttl, or rdf). If the extension parsing
	 * fails for whatever reason, the dump will be in NTriples format
	 *
	 * @param de
	 * @param out
	 * @throws IOException
	 */
	public void dump( File out ) throws RepositoryException, IOException {
		String ext = ( null == out
				? "nt" : FilenameUtils.getExtension( out.getName() ).toLowerCase() );
		Map<String, RDFFormat> map = new HashMap<>();
		map.put( "ttl", RDFFormat.TURTLE );
		map.put( "rdf", RDFFormat.RDFXML );

		RDFFormat fmt = map.getOrDefault( ext, NTRIPLES );
		try ( Writer w
				= ( null == out ? new PrintWriter( System.out ) : new FileWriter( out ) ) ) {
			RDFHandler handler = null;
			if ( RDFFormat.RDFXML == fmt ) {
				handler = new RDFXMLWriter( w );
			}
			else if ( RDFFormat.TURTLE == fmt ) {
				handler = new TurtleWriter( w );
			}
			else {
				handler = new NTriplesWriter( w );
			}

			try {
				rc.export( handler );
			}
			catch ( RDFHandlerException re ) {
				log.error( re, re );
			}
		}
	}
}
