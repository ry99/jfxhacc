package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.engine.impl.RdfDataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.SplitMapper;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.mapper.impl.AccountMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.PayeeMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.SplitMapperImpl;
import com.ostrichemulators.jfxhacc.mapper.impl.TransactionMapperImpl;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.impl.AccountImpl;
import com.ostrichemulators.jfxhacc.model.impl.PayeeImpl;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.openrdf.rio.turtle.TurtleWriter;

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
			datadir = ( parameters.isEmpty()
					? new File( System.getProperty( "user.home" ), ".jfxhacc" )
					: new File( parameters.get( 0 ) ) );

			boolean initdataset = ( !datadir.exists() );
			rc = DbUtil.createRepository( datadir );
			if ( initdataset ) {
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

		PayeeMapperImpl pmap = new PayeeMapperImpl( rc );
		for ( int i = 0; i < 10; i++ ) {
			try {
				pmap.create( new PayeeImpl( "payee-" + i ) );
			}
			catch ( MapperException ne ) {
				log.error( ne, ne );
			}
		}

		AccountMapperImpl ami = new AccountMapperImpl( rc );
		String[] anames = { "Eh", "BEE", "si" };
		for ( String name : anames ) {
			Account a = new AccountImpl( AccountType.ASSET, name );
			a.setOpeningBalance( new Money( r.nextInt( 50000 ) ) );
			try {
				ami.create( a );
			}
			catch ( MapperException ne ) {
				log.error( ne, ne );
			}

			Account e = new AccountImpl( AccountType.EXPENSE, "expense-" + name );
			e.setOpeningBalance( new Money( r.nextInt( 50000 ) ) );
			try {
				ami.create( e );
			}
			catch ( MapperException ne ) {
				log.error( ne, ne );
			}
		}

		try ( FileWriter fw = new FileWriter( "/tmp/init.ttl" ) ) {
			rc.export( new TurtleWriter( fw ) );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		SplitMapper smap = new SplitMapperImpl( rc );

		TransactionMapper tmap = new TransactionMapperImpl( rc, smap, pmap );
		try {
			Map<String, Account> accts = new HashMap<>();
			for ( Account a : ami.getAll() ) {
				accts.put( a.getName(), a );
			}

			Map<URI, String> payees = pmap.getPayees();
			Map<String, Payee> flip = new HashMap<>();
			for ( Map.Entry<URI, String> en : payees.entrySet() ) {
				flip.put( en.getValue(), new PayeeImpl( en.getKey(), en.getValue() ) );
			}

			for ( int i = 0; i < 100; i++ ) {
				Map<Split, Account> splits = new HashMap<>();
				Money m = new Money( r.nextInt( 5000 ) );
				splits.put( new SplitImpl( m ),
						accts.get( anames[r.nextInt( anames.length )] ) );
				splits.put( new SplitImpl( m.opposite() ),
						accts.get( "expense-" + anames[r.nextInt( anames.length )] ) );

				tmap.create( new Date(), flip.get( "payee-" + r.nextInt( 10 ) ), splits );
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		ami.release();
		smap.release();
		pmap.release();
		tmap.release();
	}
}
