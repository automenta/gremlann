package syncleus.gremlann.build.example;

import java.util.Date;

import syncleus.gremlann.build.Container;
import syncleus.gremlann.build.Scope;
import syncleus.gremlann.build.impl.MentaContainer;

public class BasicOperations {

	public static void main(String[] args) {

		case1();
		case2();
		case3();
		case4();
		case5();
		case6();
		case7();
		case8();
		case9();
		case10();
	}

	public static class Connection {

	}

	public static interface UserDAO {

		public String getUsername(int id);
	}

	public static class JdbcUserDAO implements UserDAO {

		private Connection conn;

		public void setConnection(Connection conn) {
			this.conn = conn;
		}

		@Override
		public String getUsername(int id) {

			// connection will be injected by the container...
			if (conn == null) throw new IllegalStateException("conn is null!");

			// use the connection to get the username...

			return "saoj";
		}
	}

	public static interface AccountDAO {

		public double getBalance(int id);
	}

	public static class JdbcAccountDAO implements AccountDAO {

		private final Connection conn;

		public JdbcAccountDAO(Connection conn) {
			this.conn = conn;
		}

		@Override
		public double getBalance(int id) {

			assert conn != null;

			// use the connection to get the balance...
			return 1000000D;
		}
	}
	
	private static void case9() {
		
		Container c = new MentaContainer();

		c.use("connection", Connection.class); // in real life this will be a connection pool factory...
		c.use("accountDAO", JdbcAccountDAO.class);
		c.use("userDAO", JdbcUserDAO.class);

		c.useAuto("connection"); // all beans that need a connection in the constructor or setter will receive one...

		AccountDAO accountDAO = c.get("accountDAO");
		UserDAO userDAO = c.get("userDAO");

		System.out.println(accountDAO.getBalance(25)); // => 1000000
		System.out.println(userDAO.getUsername(45)); // => "saoj"
	}

	private static void case1() {

		Container c = new MentaContainer();

		c.use("myString1", String.class);

		String myString1 = c.get("myString1");

		System.out.println(myString1); // ==> "" ==> default constructor new String() was used

		c.use("myString2", String.class).addInitValue("saoj");

		String myString2 = c.get("myString2");

		System.out.println(myString2); // ==> "saoj" ==> constructor new String("saoj") was used

		c.use("myDate1", Date.class).addPropertyValue("hours", 15) // setHours(15)
		        .addPropertyValue("minutes", 10) // setMinutes(10)
		        .addPropertyValue("seconds", 45); // setSeconds(45)

		Date myDate1 = c.get("myDate1");

		System.out.println(myDate1); // ==> a date with time 15:10:45
	}

	private static void case5() {

		Container c = new MentaContainer();

		c.use("connection", Connection.class); // in real life this will be a connection pool factory...

		c.use("accountDAO", JdbcAccountDAO.class).addConstructorDependency("connection");

		AccountDAO accountDAO = c.get("accountDAO");

		System.out.println(accountDAO.getBalance(25)); // => 1000000
	}

	private static void case7() {

		Container c = new MentaContainer();

		c.use("connection", Connection.class); // in real life this will be a connection pool factory...

		c.use("accountDAO", JdbcAccountDAO.class);

		c.useAuto("connection"); // all beans that need a connection in the constructor will get one...

		AccountDAO accountDAO = c.get("accountDAO");

		System.out.println(accountDAO.getBalance(25)); // => 1000000

	}

	private static void case6() {

		Container c = new MentaContainer();

		c.use("connection", Connection.class); // in real life this will be a connection pool factory...

		c.use("userDAO", JdbcUserDAO.class).addPropertyDependency("connection");

		UserDAO userDAO = c.get("userDAO");

		System.out.println(userDAO.getUsername(54)); // => "saoj"
	}

	private static void case8() {

		Container c = new MentaContainer();

		c.use("connection", Connection.class); // in real life this will be a connection pool factory...

		c.use("userDAO", JdbcUserDAO.class);

		c.useAuto("connection");

		UserDAO userDAO = c.get("userDAO");

		System.out.println(userDAO.getUsername(54)); // => "saoj"

	}

	private static void case2() {

		Container c = new MentaContainer();

		c.use("myString", String.class, Scope.SINGLETON).addInitValue("saoj");

		String s1 = c.get("myString");

		String s2 = c.get("myString");

		System.out.println(s1 == s2); // ==> true ==> same instance

		System.out.println(s1.equals(s2)); // ==> true => of course
	}

	private static void case3() {

		Container c = new MentaContainer();

		c.use("userDAO", JdbcUserDAO.class);

		c.use("connection", Connection.class); // in real life this would be a connection pool
		                                       // or the hibernate SessionFactory

		// "conn" = the name of the property
		// Connection.class = the type of the property
		// "connection" = the source from where the dependency will come from
		c.useAuto("connection");

		UserDAO userDAO = c.get("userDAO");

		// the container detects that userDAO has a dependency: name = "conn" and type = "Connection.class"
		// where does it go to get the dependency to insert?
		// In itself: it does a Container.get("connection") => "connection" => the source

		System.out.println(userDAO.getUsername(11)); // ==> "saoj" ==> connection is not null as expected...
	}

	public static class SomeService {

		private UserDAO userDAO;

		public void setUserDAO(UserDAO userDAO) {
			this.userDAO = userDAO;
		}

		public void doSomething() {
			System.out.println(userDAO.getUsername(11));
		}
	}

	private static void case4() {

		Container c = new MentaContainer();

		c.use("userDAO", JdbcUserDAO.class);

		c.use("connection", Connection.class);

		c.useAuto("connection");

		SomeService service = new SomeService();

		c.inject(service); // populate (inject) all properties of SomeService with
		                     // beans from the container

		service.doSomething(); // ==> "saoj"
	}
	
	public static class SomeService2 {

		private final UserDAO userDAO;

		public SomeService2(UserDAO userDAO) {
			this.userDAO = userDAO;
		}
		
		public void doSomething() {
			System.out.println(userDAO.getUsername(11));
		}
	}

	private static void case10() {

		Container c = new MentaContainer();

		c.use("userDAO", JdbcUserDAO.class);

		c.use("connection", Connection.class);

		c.useAuto("connection");
		
		SomeService2 service = c.get(SomeService2.class);

		service.doSomething(); // ==> "saoj"
	}	

}