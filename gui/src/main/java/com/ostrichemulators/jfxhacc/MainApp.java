package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.engine.impl.RdfDataEngine;
import com.ostrichemulators.jfxhacc.mapper.JournalMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.mapper.impl.AccountMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.JournalMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.PayeeMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.TransactionMapperImpl;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.impl.PayeeImpl;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class MainApp extends Application {

	private static final Logger log = Logger.getLogger( MainApp.class );
	private RepositoryConnection rc;
	private File datadir;

	private static RdfDataEngine engine;

	public static DataEngine getEngine() {
		return engine;
	}

	@Override
	public void start( Stage stage ) throws Exception {
		Parent root = FXMLLoader.load( getClass().getResource( "/fxml/Scene.fxml" ) );

		Scene scene = new Scene( root );
		scene.getStylesheets().add( "/styles/Styles.css" );

		stage.setTitle( "Jfx Home Accountant" );
		stage.setScene( scene );
		stage.show();
	}

	@Override
	public void init() {
		try {
			final Parameters params = getParameters();
			final List<String> parameters = params.getRaw();

			File defaultdb = new File( System.getProperty( "user.home" ), ".jfxhacc" );
			boolean doinit = ( parameters.isEmpty() && !defaultdb.exists() );

			String database
					= ( parameters.isEmpty() ? defaultdb.getPath() : parameters.get( 0 ) );

			rc = DbUtil.createRepository( database );

			if ( doinit ) {
				initKb( rc );
			}

			engine = new RdfDataEngine( rc );
		}
		catch ( Exception e ) {
			System.err.println( e.getLocalizedMessage() );
			log.fatal( e, e );
			Platform.exit();
		}
	}

	@Override
	public void stop() {
		if ( log.isTraceEnabled() ) {
			try {
				engine.dump( new File( "/tmp/dump.ttl" ) );
			}
			catch ( RepositoryException | IOException e ) {
				log.warn( e, e );
			}
		}

		try {
			rc.close();
		}
		catch ( Exception e ) {
			log.warn( e, e );
		}

		try {
			rc.getRepository().shutDown();
		}
		catch ( Exception e ) {
			log.warn( e, e );
		}
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application.
	 * main() serves only as fallback in case the application can not be launched
	 * through deployment artifacts, e.g., in IDEs with limited FX support.
	 * NetBeans ignores main().
	 *
	 * @param args the command line arguments
	 */
	public static void main( String[] args ) {
		launch( args );
	}

	private static void initKb( RepositoryConnection rc ) throws RepositoryException {
		// this is a good place to add the standard ontology

		Random r = new Random();

		JournalMapper jmap = new JournalMapperImpl( rc );
		Journal journal = null;
		try {
			journal = jmap.create( "General" );
		}
		catch ( MapperException ne ) {
			log.error( ne, ne );
		}

		PayeeMapperImpl pmap = new PayeeMapperImpl( rc );
		for ( int i = 0; i < 10; i++ ) {
			try {
				pmap.create( "payee-" + i );
			}
			catch ( MapperException ne ) {
				log.error( ne, ne );
			}
		}

		AccountMapperImpl amap = new AccountMapperImpl( rc );
		String[] anames = { "Eh", "BEE", "si" };

		Set<Account> testers = new HashSet<>();

		for ( String name : anames ) {
			try {
				Account a = amap.create( name, AccountType.ASSET, new Money( r.nextInt( 50000 ) ), null );
				Account e = amap.create( "expense-" + name, AccountType.EXPENSE,
						new Money( r.nextInt( 50000 ) ), null );

				testers.add( a );
				testers.add( e );
			}
			catch ( MapperException ne ) {
				log.error( ne, ne );
			}
		}

		TransactionMapper tmap = new TransactionMapperImpl( rc, amap, pmap );
		try {
			Map<String, Account> accts = new HashMap<>();
			Set<Account> alls = new HashSet<>( amap.getAll() );
			log.debug( testers.equals( alls ) );

			for ( Account a : amap.getAll() ) {
				accts.put( a.getName(), a );
			}

			Map<URI, String> payees = pmap.getPayees();
			Map<String, Payee> flip = new HashMap<>();
			for ( Map.Entry<URI, String> en : payees.entrySet() ) {
				flip.put( en.getValue(), new PayeeImpl( en.getKey(), en.getValue() ) );
			}

			for ( int i = 0; i < 100; i++ ) {
				Map<Account, Split> splits = new HashMap<>();
				Money m = new Money( r.nextInt( 5000 ) );

				Split credit = tmap.create( m, "", ReconcileState.NOT_RECONCILED );
				Split debit = tmap.create( m.opposite(), "", ReconcileState.NOT_RECONCILED );

				Account cacct = accts.get( anames[r.nextInt( anames.length )] );
				Account dacct = accts.get( "expense-" + anames[r.nextInt( anames.length )] );

				splits.put( cacct, credit );
				splits.put( dacct, debit );

				tmap.create( new Date(), flip.get( "payee-" + r.nextInt( 10 ) ),
						Integer.toString( i ), splits, journal );
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		amap.release();
		pmap.release();
		tmap.release();
	}
}
