package org.springframework.data.keyvalue.core;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;


/**
 * @author Macko Martin <martin.macko@morosystems.cz>
 */
@RunWith(MockitoJUnitRunner.class)
public class SpelQueryEngineUnitTests {

	private final Person BOB_WITH_FIRSTNAME = new Person("bob", 30);
	private final Person MIKE_WITHOUT_FIRSTNAME = new Person(null, 25);

	@Mock RepositoryMetadata metadataMock;
	@Mock KeyValueAdapter adapterMock;

	private Iterable iterablePeople;

	@Before
	public void setUp() {
		List<Person> people = new LinkedList<Person>();
		people.add(BOB_WITH_FIRSTNAME);
		people.add(MIKE_WITHOUT_FIRSTNAME);
		iterablePeople = people;
	}

	@Test
	public void testExecuteWithPropertyNull() throws Exception {
		when(adapterMock.getAllOf(anyString())).thenReturn(iterablePeople);

		SpelQueryEngine sqe = new SpelQueryEngine();
		sqe.registerAdapter(adapterMock);

		Collection returnCol = sqe.execute(createQueryForMethodWithArgs("findByFirstname", "bob"), null, -1, -1, anyString());
		assertTrue(returnCol.contains(BOB_WITH_FIRSTNAME));
	}

	@Test
	public void testCountWithPropertyNull() throws Exception {
		when(adapterMock.getAllOf(anyString())).thenReturn(iterablePeople);

		SpelQueryEngine sqe = new SpelQueryEngine();
		sqe.registerAdapter(adapterMock);

		long returnCol = sqe.count(createQueryForMethodWithArgs("findByFirstname", "bob"), anyString());
		assertThat(returnCol, is(Long.valueOf(1)));
	}

	private SpelExpression createQueryForMethodWithArgs(String methodName, Object... args)
		throws NoSuchMethodException, SecurityException {

		Class<?>[] argTypes = new Class<?>[args.length];
		if (!ObjectUtils.isEmpty(args)) {

			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
			}
		}

		Method method = PersonRepository.class.getMethod(methodName, argTypes);

		PartTree partTree = new PartTree(method.getName(), method.getReturnType());
		SpelQueryCreator creator = new SpelQueryCreator(partTree, new ParametersParameterAccessor(new QueryMethod(method,
			metadataMock).getParameters(), args));

		KeyValueQuery<SpelExpression> q = creator.createQuery();
		q.getCritieria().setEvaluationContext(new StandardEvaluationContext(args));

		return q.getCritieria();
	}

	static interface PersonRepository {
		Person findByFirstname(String firstname);
	}

	static class Person {

		private @Id String id;
		private String firstname;
		private int age;

		public Person() {}

		public Person(String firstname, int age) {
			super();
			this.firstname = firstname;
			this.age = age;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
			hash = 29 * hash + (this.firstname != null ? this.firstname.hashCode() : 0);
			hash = 29 * hash + this.age;
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Person other = (Person) obj;
			if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
				return false;
			}
			if ((this.firstname == null) ? (other.firstname != null) : !this.firstname.equals(other.firstname)) {
				return false;
			}
			if (this.age != other.age) {
				return false;
			}
			return true;
		}

	}
}
