package net.ihe.gazelle.users.action;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.users.model.Person;
import net.ihe.gazelle.users.model.PersonFunction;
import net.ihe.gazelle.users.model.PersonQuery;
import net.ihe.gazelle.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

@Ignore
public class PersonManagerTest extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void test_saveContact() {

        setFunctionForAContact(680);
        setFunctionForAContact(555);
        setFunctionForAContact(680);
        setFunctionForAContact(555);
        setFunctionForAContact(680);
    }

    private void setFunctionForAContact(int id) {
        PersonManager pm = new PersonManager();
        ArrayList<Pair<Boolean, PersonFunction>> personFunctionsForSelectedContact = createUserFunction();
        pm.setPersonFunctionsForSelectedContact(personFunctionsForSelectedContact);

        Person person2 = findUser(id);
        pm.setSelectedContact(person2);
        pm.saveContact();
    }

    private Person findUser(int id) {
        PersonQuery personQuery = new PersonQuery();
        personQuery.id().eq(id);
        Person person = personQuery.getUniqueResult();
        return person;
    }

    private ArrayList<Pair<Boolean, PersonFunction>> createUserFunction() {
        ArrayList<Pair<Boolean, PersonFunction>> personFunctionsForSelectedContact = new ArrayList<Pair<Boolean, PersonFunction>>();
        personFunctionsForSelectedContact.add(new Pair<Boolean, PersonFunction>(Boolean.TRUE, PersonFunction
                .getPrimaryTechnicalFunction(null)));
        return personFunctionsForSelectedContact;
    }

    @Override
    protected String getDb() {
        return "gazelle-junit";
    }
}
