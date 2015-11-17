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
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class MainApp extends Application {

	private static final Logger log = Logger.getLogger( MainApp.class );
	private RepositoryConnection rc;

	private static RdfDataEngine engine;
	private static StageRememberer stager;

	public static DataEngine getEngine() {
		return engine;
	}

	public static StageRememberer getShutdownNotifier() {
		return stager;
	}

	@Override
	public void start( Stage stage ) throws Exception {
		MainApp.stager = new StageRememberer( stage );

		URL loc = getClass().getResource( "/fxml/Scene.fxml" );
		FXMLLoader fxmlloader = new FXMLLoader( loc );
		fxmlloader.setController( new FXMLController() );

		Parent root = FXMLLoader.load( getClass().getResource( "/fxml/Scene.fxml" ) );

		Scene scene = new Scene( root );
		scene.getStylesheets().add( "/styles/Styles.css" );

		stage.setTitle( "Jfx Home Accountant" );
		stage.setScene( scene );

		MainApp.stager.restore( stage );
		stage.setOnCloseRequest( MainApp.stager );

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

	public class StageRememberer implements EventHandler<WindowEvent> {

		Preferences userPrefs = Preferences.userNodeForPackage( MainApp.class );
		private final Stage mystage;
		private final List<ShutdownListener> listeners = new ArrayList<>();

		public StageRememberer( Stage primaryStage ) {
			this.mystage = primaryStage;
		}

		public void addShutdownListener( ShutdownListener r ) {
			listeners.add( r );
		}

		public void removeShutdownListener( ShutdownListener r ) {
			listeners.remove( r );
		}

		public Stage getStage() {
			return mystage;
		}

		public void restore( Stage stage ) {
			double x = userPrefs.getDouble( "stage.x", 100 );
			double y = userPrefs.getDouble( "stage.y", 100 );
			double w = userPrefs.getDouble( "stage.width", 800 );
			double h = userPrefs.getDouble( "stage.height", 600 );
			mystage.setX( x );
			mystage.setY( y );
			mystage.setWidth( w );
			mystage.setHeight( h );
		}

		@Override
		public void handle( WindowEvent t ) {
			userPrefs.putDouble( "stage.x", mystage.getX() );
			userPrefs.putDouble( "stage.y", mystage.getY() );
			userPrefs.putDouble( "stage.width", mystage.getWidth() );
			userPrefs.putDouble( "stage.height", mystage.getHeight() );

			for ( ShutdownListener p : listeners ) {
				p.shutdown();
			}
		}
	}
}
