/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.mapper.QueryHandler;
import com.ostrichemulators.jfxhacc.mapper.impl.RdfMapper;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.model.vocabulary.Loans;
import com.ostrichemulators.jfxhacc.model.vocabulary.Payees;
import com.ostrichemulators.jfxhacc.model.vocabulary.Recurrences;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import info.aduna.iteration.Iterations;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class DbUtil {

	private static final Logger log = Logger.getLogger( DbUtil.class );

	private DbUtil() {
	}

	public static RepositoryConnection createRepository( String db ) throws RepositoryException {
		RepositoryConnection rc = null;
		boolean doinit = true;
		if ( db.endsWith( "memorystore.data" ) ) {
			// make an in-memory repository
			rc = createInMemRepository( new File( db ) );
		}
		else if ( db.endsWith( ".ttl" ) || db.endsWith( ".rdf" ) || db.endsWith( ".nt" ) ) {
			// use the given file to populate an in-memory datastore
			rc = createInMemRepository();
			rc.clear(); // get rid of the extra dataset statements
			doinit = false;
			RDFFormat fmt = RDFFormat.NTRIPLES;
			if ( db.endsWith( ".ttl" ) ) {
				fmt = RDFFormat.TURTLE;
			}
			else if ( db.endsWith( ".rdf" ) ) {
				fmt = RDFFormat.RDFXML;
			}

			try {
				log.info( "loading data from:" + db );
				rc.add( new File( db ), "", fmt );
			}
			catch ( IOException | RDFParseException e ) {
				log.error( e, e );
			}
		}
		else if ( db.startsWith( "http" ) ) {
			// make an http repository
			Repository repo = new HTTPRepository( db );
			repo.initialize();
			rc = repo.getConnection();
		}

		if ( null == rc ) {
			//nothing worked so far, so see if we just have a plain old directory
			File tryfile = new File( db );
			if ( tryfile.isDirectory() ) {
				rc = createInMemRepository( tryfile );
			}
			else {
				throw new RepositoryException( "Unable to open database: " + db );
			}
		}

		initNamespaces( rc );

		if ( doinit && initDb( rc ) ) {
			firstTime( rc );
		}

		log.info( "database initialized" );
		return rc;
	}

	public static RepositoryConnection createInMemRepository() throws RepositoryException {
		Repository repo = new SailRepository( new MemoryStore() );
		repo.initialize();
		RepositoryConnection rc = repo.getConnection();
		initDb( rc );
		initNamespaces( rc );
		firstTime( rc );
		return rc;
	}

	private static RepositoryConnection createInMemRepository( File datadir )
			throws RepositoryException {
		boolean init = ( !datadir.exists() );

		if ( init ) {
			if ( datadir.getName().equals( "memorystore.data" ) ) {
				datadir.getParentFile().mkdirs();
			}
			else {
				datadir.mkdirs();
			}
		}

		if ( datadir.getName().equals( "memorystore.data" ) ) {
			// OpenRDF automatically adds the "memorystore.data" part
			datadir = datadir.getParentFile();
		}

		Repository repo = new SailRepository( new MemoryStore( datadir ) );
		repo.initialize();
		return repo.getConnection();
	}

	private static void initNamespaces( RepositoryConnection rc ) throws RepositoryException {
		rc.begin();
		rc.setNamespace( JfxHacc.PREFIX, JfxHacc.NAMESPACE );
		rc.setNamespace( Accounts.PREFIX, Accounts.NAMESPACE );
		rc.setNamespace( Splits.PREFIX, Splits.NAMESPACE );
		rc.setNamespace( Transactions.PREFIX, Transactions.NAMESPACE );
		rc.setNamespace( Payees.PREFIX, Payees.NAMESPACE );
		rc.setNamespace( Recurrences.PREFIX, Recurrences.NAMESPACE );
		rc.setNamespace( Loans.PREFIX, Loans.NAMESPACE );
		rc.setNamespace( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
		rc.setNamespace( "t", "http://com.ostrich-emulators/jfxhacc/transaction#" );
		rc.setNamespace( "p", "http://com.ostrich-emulators/jfxhacc/payee#" );
		rc.setNamespace( "a", "http://com.ostrich-emulators/jfxhacc/account#" );
		rc.setNamespace( "s", "http://com.ostrich-emulators/jfxhacc/split#" );
		rc.setNamespace( "j", "http://com.ostrich-emulators/jfxhacc/journal#" );
		rc.setNamespace( "r", "http://com.ostrich-emulators/jfxhacc/recurrence#" );
		rc.setNamespace( "l", "http://com.ostrich-emulators/jfxhacc/loan#" );

		rc.setNamespace( RDFS.PREFIX, RDFS.NAMESPACE );
		rc.setNamespace( RDF.PREFIX, RDF.NAMESPACE );
		rc.setNamespace( XMLSchema.PREFIX, XMLSchema.NAMESPACE );
		rc.commit();
	}

	private static URI getDbId( RepositoryConnection rc ) throws RepositoryException {
		List<Statement> stmts = Iterations.asList( rc.getStatements( null, RDF.TYPE,
				JfxHacc.DATASET_TYPE, true ) );
		return ( stmts.isEmpty() ? null
				: URI.class.cast( stmts.get( 0 ).getSubject() ) );
	}

	private static boolean initDb( RepositoryConnection rc ) throws RepositoryException {
		URI dbid = getDbId( rc );
		boolean wasCreated = true;

		ValueFactory vf = rc.getValueFactory();
		rc.begin();
		if ( null == dbid ) {
			dbid = UriUtil.randomUri( new URIImpl( JfxHacc.BASE ) );
			rc.add( dbid, RDF.TYPE, JfxHacc.DATASET_TYPE );
			rc.add( dbid, DCTERMS.CREATED, vf.createLiteral( new Date() ) );
			rc.add( dbid, DCTERMS.CREATOR, vf.createLiteral( System.getProperty( "user.name" ) ) );

			rc.add( dbid, JfxHacc.MAJOR_VERSION, vf.createLiteral( JfxHacc.MAJORV ) );
			rc.add( dbid, JfxHacc.MINOR_VERSION, vf.createLiteral( JfxHacc.MINORV ) );
			rc.add( dbid, JfxHacc.REVISION_VERSION, vf.createLiteral( JfxHacc.REVV ) );
		}
		else {
			wasCreated = false;
		}

		rc.remove( dbid, DCTERMS.MODIFIED, null );
		rc.add( dbid, DCTERMS.MODIFIED,
				rc.getValueFactory().createLiteral( new Date() ) );

		rc.commit();
		return wasCreated;
	}

	private static void firstTime( RepositoryConnection rc ) throws RepositoryException {
		// this is a good place to add the standard ontology
	}

	/**
	 * Gets a mapping of version number elements. The keys are {@link JfxHacc#MAJOR_VERSION},
	 * {@link JfxHacc#MINOR_VERSION}, and {@link JfxHacc#REVISION_VERSION}
	 *
	 * @param rc
	 * @return
	 * @throws RepositoryException
	 */
	public static Map<URI, Integer> getDbVersionMap( RepositoryConnection rc ) throws RepositoryException {
		Map<URI, Integer> map = new HashMap<>();
		URI dbid = getDbId( rc );

		for ( URI u : Arrays.asList( JfxHacc.MAJOR_VERSION, JfxHacc.MINOR_VERSION, JfxHacc.REVISION_VERSION ) ) {
			List<Statement> stmts = Iterations.asList( rc.getStatements( dbid, JfxHacc.MAJOR_VERSION,
					null, false ) );
			if ( stmts.isEmpty() ) {
				map.put( u, 0 );
			}
			else {
				map.put( u, Literal.class.cast( stmts.get( 0 ) ).intValue() );
			}
		}

		return map;
	}

	public static String getDbVersion( RepositoryConnection rc ) throws RepositoryException {
		Map<URI, Integer> map = getDbVersionMap( rc );
		StringBuilder sb = new StringBuilder();
		for ( URI u : Arrays.asList( JfxHacc.MAJOR_VERSION, JfxHacc.MINOR_VERSION,
				JfxHacc.REVISION_VERSION ) ) {
			if ( sb.length() > 0 ) {
				sb.append( "." );
			}
			sb.append( map.getOrDefault( u, 0 ) );
		}

		return sb.toString();
	}

	public static void upgradeIfNecessary( RepositoryConnection rc )
			throws RepositoryException {
		LinkedHashMap<URI, Integer> currentversions = new LinkedHashMap<>();
		currentversions.put( JfxHacc.MAJOR_VERSION, JfxHacc.MAJORV );
		currentversions.put( JfxHacc.MINOR_VERSION, JfxHacc.MINORV );
		currentversions.put( JfxHacc.REVISION_VERSION, JfxHacc.REVV );

		Map<URI, Integer> map = getDbVersionMap( rc );
		for ( Map.Entry<URI, Integer> en : currentversions.entrySet() ) {
			URI u = en.getKey();
			final int version = en.getValue();

			int dbver = map.getOrDefault( u, 0 );
			if ( dbver < version ) {
				upgradeFromTo( rc, u, version, dbver );
				break;
			}
		}
	}

	private static void upgradeFromTo( RepositoryConnection rc,
			URI versionkey, int version, int cur ) throws RepositoryException {
		log.warn( "upgrading " + versionkey + " from " + cur + " to " + version );

		final Map<URI, List<URI>> transsplits = new HashMap<>();
		final Map<URI, URI> splittrans = new HashMap<>();
		final Map<AccountType, List<URI>> atypesplits = new HashMap<>();
		final Map<URI, Integer> splitvals = new HashMap<>();
		final Set<URI> transdone = new HashSet<>();

		QueryHandler<Void> handler = new QueryHandler<Void>() {
			@Override
			public void handleTuple( BindingSet set, ValueFactory vf ) {
				URI tid = URI.class.cast( set.getValue( "tid" ) );
				URI sid = URI.class.cast( set.getValue( "sid" ) );
				AccountType atype = AccountType.valueOf( URI.class.cast( set.getValue( "atype" ) ) );
				Value val = set.getValue( "val" );

				if ( !transsplits.containsKey( tid ) ) {
					transsplits.put( tid, new ArrayList<>() );
				}
				transsplits.get( tid ).add( sid );

				splittrans.put( sid, tid );
				splitvals.put( sid, Literal.class.cast( val ).intValue() );

				if ( !atypesplits.containsKey( atype ) ) {
					atypesplits.put( atype, new ArrayList<>() );
				}
				atypesplits.get( atype ).add( sid );
			}

			@Override
			public Void getResult() {
				return null;
			}
		};

		String query = "SELECT ?atype ?sid ?val ?tid WHERE {"
				+ "  ?sid <" + Splits.ACCOUNT_PRED + "> ?aid ."
				+ "  ?aid <" + Accounts.TYPE_PRED + "> ?atype ."
				+ "  ?sid <" + Splits.VALUE_PRED + "> ?val ."
				+ "  ?tid <" + Transactions.SPLIT_PRED + "> ?sid ."
				+ "}";
		log.debug( "upgrade query: " + query );
		RdfMapper.query( query, new HashMap<>(), handler, rc );

		ValueFactory vf = new ValueFactoryImpl();
		rc.begin();
		List<URI> uris = new ArrayList<>( atypesplits.get( AccountType.ASSET ) );
		uris.addAll( atypesplits.get( AccountType.LIABILITY ) );
		uris.addAll( atypesplits.get( AccountType.REVENUE ) );

		for ( URI uri : uris ) {
			URI transid = splittrans.get( uri );
			if ( !transdone.contains( transid ) ) {
				transdone.add( transid );

				List<URI> allsplits = transsplits.get( transid );
				log.debug( "upgrading transaction: " + transid.getLocalName() );
				for ( URI splitid : allsplits ) {
					int val = splitvals.get( splitid );
					log.debug( "  split: " + splitid.getLocalName()
							+ " oldval: " + val + ", newval: " + ( -val ) );

					// reverse the split values for every transaction that touches an asset
					rc.remove( splitid, Splits.VALUE_PRED, null );
					rc.add( splitid, Splits.VALUE_PRED, vf.createLiteral( -val ) );
				}
			}
		}
		rc.commit();
	}

	public static Literal fromDate( Date date ) {
		return new ValueFactoryImpl().createLiteral( date );
	}

	public static Date toDate( Literal cal ) {
		return cal.calendarValue().toGregorianCalendar().getTime();
	}
}
