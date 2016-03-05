package test;

import impl.ContactImpl;
import impl.ContactManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spec.*;

import java.util.*;

import static org.junit.Assert.*;
import static test.TestCommon.*;

/**
 * ContactManager tests
 * Split up the tests as the main test class was getting quite large
 *
 * This class tests the Meeting methods in ContactManager
 * @author lmignot
 */
public class ContactManagerMeetingsTest {

    private ContactManager emptyCM;
    private ContactManager contactsCM;
    private ContactManager meetingsCM;
    private Set<Contact> contactsA;
    private Set<Contact> contactsB;
    private Set<Contact> contactsC;

    private Calendar futureDate;
    private Calendar pastDate;
    private Calendar currentDate;

    @Before
    public void setUp() {
        deleteDataFile();

        emptyCM = new ContactManagerImpl();
        contactsCM = new ContactManagerImpl();
        meetingsCM = new ContactManagerImpl();

        addTestContacts(contactsCM);
        addTestContacts(meetingsCM);

        contactsA = contactsCM.getContacts(CONTACT_1_ID, CONTACT_2_ID, CONTACT_3_ID, CONTACT_4_ID);

        contactsB = meetingsCM.getContacts(CONTACT_1_ID, CONTACT_2_ID, CONTACT_3_ID, CONTACT_4_ID);
        contactsC = meetingsCM.getContacts(CONTACT_4_ID, CONTACT_5_ID, CONTACT_6_ID);

        addTestMeetings(meetingsCM, contactsB, contactsC);

        currentDate = new GregorianCalendar();
        futureDate = new GregorianCalendar(FUTURE_YEAR, FUTURE_MONTH, FUTURE_DAY);
        pastDate = new GregorianCalendar(PAST_YEAR, PAST_MONTH, PAST_DAY);
    }

    @After
    public void tearDown() {
        deleteDataFile();
    }

    /* =================== MEETINGS =================== */

    @Test
    public void testGetExistingMeeting () {
        int id = contactsCM.addFutureMeeting(contactsA, futureDate);
        Meeting mtg = contactsCM.getMeeting(id);

        assertNotNull(mtg);
        assertEquals(mtg.getId(), id);
        assertEquals(mtg.getDate(), futureDate);
        assertEquals(mtg.getContacts(), contactsA);
    }

    /* =================== FUTURE MEETINGS =================== */

    @Test
    public void testAddFutureMeeting () {
        int futureMeetingId = contactsCM.addFutureMeeting(contactsA, futureDate);
        currentDate.add(Calendar.SECOND, THIRTY_SECONDS);
        int futureMeetingId2 = contactsCM.addFutureMeeting(contactsA, currentDate);

        assertEquals(futureMeetingId, ID_ONE);
        assertEquals(futureMeetingId2, ID_TWO);
    }

    @Test
    public void testGetFutureMeeting () {
        int futureMeetingId = contactsCM.addFutureMeeting(contactsA, futureDate);

        Meeting meeting = contactsCM.getFutureMeeting(futureMeetingId);

        assertNotNull(meeting);
        assertFalse(meeting instanceof PastMeeting);
        assertEquals(meeting.getId(), futureMeetingId);
        assertEquals(meeting.getDate(), futureDate);
        assertEquals(meeting.getContacts(), contactsA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFutureMeetingWithPastMeetingId () {
        contactsCM.addNewPastMeeting(contactsA, pastDate, MEETING_NOTES);
        contactsCM.getFutureMeeting(ID_ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testAddFutureMeetingWithNullContacts () {
        contactsCM.addFutureMeeting(NULL_CONTACTS, futureDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFutureMeetingWithEmptyContacts () {
        contactsCM.addFutureMeeting(EMPTY_CONTACTS, futureDate);
    }

    @Test(expected = NullPointerException.class)
    public void testAddFutureMeetingWithNullDate () {
        contactsCM.addFutureMeeting(contactsA, NULL_CAL);
    }

    @Test(expected = NullPointerException.class)
    public void testAddFutureMeetingWithNullDateAndContacts () {
        contactsCM.addFutureMeeting(NULL_CONTACTS, NULL_CAL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFutureMeetingWithPastDate () {
        contactsCM.addFutureMeeting(contactsA, pastDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFutureMeetingWithCurrentDate () {
        contactsCM.addFutureMeeting(contactsA, currentDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFutureMeetingWithInvalidContacts () {
        emptyCM.addFutureMeeting(contactsA, pastDate);
    }

    /* =================== PAST MEETINGS =================== */

    @Test
    public void testAddPastMeeting () {
        contactsCM.addNewPastMeeting(contactsA, pastDate, MEETING_NOTES);
        PastMeeting pMtg = contactsCM.getPastMeeting(ID_ONE);

        assertNotNull(pMtg);
        assertEquals(pMtg.getId(), ID_ONE);
        assertEquals(pMtg.getContacts(), contactsA);
        assertEquals(pMtg.getDate(), pastDate);
        assertEquals(pMtg.getNotes(), MEETING_NOTES);
    }

    @Test
    public void testGetPastMeeting () {
        contactsCM.addNewPastMeeting(contactsA, pastDate, MEETING_NOTES);

        PastMeeting pMtg = contactsCM.getPastMeeting(ID_ONE);

        assertNotNull(pMtg);
        assertFalse(pMtg instanceof FutureMeeting);
        assertEquals(pMtg.getId(), ID_ONE);
        assertEquals(pMtg.getDate(), pastDate);
        assertEquals(pMtg.getContacts(), contactsA);
        assertEquals(pMtg.getNotes(), MEETING_NOTES);
    }

    @Test
    public void testAddPastMeetingWithCurrentDateMinusFiveMillis () {
        currentDate.add(Calendar.MILLISECOND, MINUS_FIVE_MILLISECONDS);
        contactsCM.addNewPastMeeting(contactsA, currentDate, MEETING_NOTES);
        PastMeeting pMtg = contactsCM.getPastMeeting(ID_ONE);

        assertNotNull(pMtg);
        assertEquals(pMtg.getId(), ID_ONE);
        assertEquals(pMtg.getDate(), currentDate);
        assertEquals(pMtg.getNotes(), MEETING_NOTES);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetPastMeetingWithFutureMeetingId () {
        contactsCM.addFutureMeeting(contactsA, futureDate);
        contactsCM.getPastMeeting(ID_ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPastMeetingWithFutureDate () {
        contactsCM.addNewPastMeeting(contactsA, futureDate, MEETING_NOTES);
    }

    @Test(expected = NullPointerException.class)
    public void testAddPastMeetingWithNullContacts () {
        contactsCM.addNewPastMeeting(NULL_CONTACTS, pastDate, MEETING_NOTES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPastMeetingWithEmptyContacts () {
        contactsCM.addNewPastMeeting(EMPTY_CONTACTS, pastDate, MEETING_NOTES);
    }

    @Test(expected = NullPointerException.class)
    public void testAddPastMeetingWithNullDate () {
        contactsCM.addNewPastMeeting(contactsA, NULL_CAL, MEETING_NOTES);
    }

    @Test(expected = NullPointerException.class)
    public void testAddPastMeetingWithNullNotes () {
        contactsCM.addNewPastMeeting(contactsA, pastDate, NULL_STRING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPastMeetingWithEmptyNotes () {
        contactsCM.addNewPastMeeting(contactsA, pastDate, EMPTY_STRING);
    }

    /* =================== MEETING LISTS =================== */

    @Test
    public void testEmptyGetFutureMeetingListForIsEmpty() {
        Contact testContact = contactsCM.getContacts(CONTACT_1_ID).stream().findFirst().get();
        List<Meeting> futureMeetingList = contactsCM.getFutureMeetingList(testContact);

        assertEquals(futureMeetingList.size(), EMPTY_SIZE);
        assertTrue(futureMeetingList.isEmpty());
    }

    @Test
    public void testGetFutureMeetingListForIsNotEmptyAndSorted() {
        Contact testContact = meetingsCM.getContacts(CONTACT_1_ID).stream().findFirst().get();
        List<Meeting> futureMeetingList = meetingsCM.getFutureMeetingList(testContact);

        assertEquals(futureMeetingList.size(), 4);
        assertEquals(futureMeetingList.get(0).getId(), ID_ONE);
        assertEquals(futureMeetingList.get(1).getId(), ID_FOUR);
        assertEquals(futureMeetingList.get(2).getId(), ID_FIVE);
        assertEquals(futureMeetingList.get(3).getId(), ID_SEVEN);
    }

    @Test
    public void testGetFutureMeetingListForHasNoDuplicates() {
        Contact testContact = meetingsCM.getContacts(CONTACT_4_ID).stream().findFirst().get();

        List<Meeting> futureMeetingList = meetingsCM.getFutureMeetingList(testContact);

        assertEquals(futureMeetingList.size(), 5);
        assertEquals(futureMeetingList.get(1).getId(), ID_NINE);
    }

    @Test(expected = NullPointerException.class)
    public void testGetFutureMeetingListWithNullContactShouldThrow () {
        contactsCM.getFutureMeetingList(NULL_CONTACT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFutureMeetingListWithInvalidContactShouldThrow () {
        contactsCM.getFutureMeetingList(new ContactImpl(ILLEGAL_ID_3, NON_EXISTENT_CONTACT_NAME));
    }

    @Test
    public void testGetMeetingListOnIsEmpty() {
        List<Meeting> meetingList = contactsCM.getMeetingListOn(
            new GregorianCalendar(FUTURE_YEAR,FUTURE_MONTH, FUTURE_DAY)
        );

        assertEquals(meetingList.size(), EMPTY_SIZE);
        assertTrue(meetingList.isEmpty());
    }

    @Test
    public void testGetMeetingListOnFutureHasNoDuplicates() {
        List<Meeting> meetingList = meetingsCM.getMeetingListOn(
            new GregorianCalendar(FUTURE_YEAR, FUTURE_MONTH,FUTURE_DAY)
        );

        assertEquals(meetingList.size(), 5);
    }

    @Test
    public void testGetMeetingListOnFutureIsSorted() {
        List<Meeting> meetingList = meetingsCM.getMeetingListOn(
            new GregorianCalendar(FUTURE_YEAR, FUTURE_MONTH,FUTURE_DAY)
        );

        assertEquals(meetingList.get(0).getId(), ID_ONE);
        assertEquals(meetingList.get(1).getId(), ID_NINE);
        assertEquals(meetingList.get(2).getId(), ID_FOUR);
        assertEquals(meetingList.get(3).getId(), ID_FIVE);
        assertEquals(meetingList.get(4).getId(), ID_SEVEN);
    }

    @Test
    public void testGetMeetingListOnPastHasNoDuplicates() {
        List<Meeting> meetingList = meetingsCM.getMeetingListOn(
            new GregorianCalendar(PAST_YEAR, PAST_MONTH, PAST_DAY)
        );

        assertEquals(meetingList.size(), 5);
    }

    @Test
    public void testGetMeetingListOnPastIsSorted() {
        List<Meeting> meetingList = meetingsCM.getMeetingListOn(
            new GregorianCalendar(PAST_YEAR, PAST_MONTH, PAST_DAY)
        );

        assertEquals(meetingList.get(0).getId(), ID_TWO);
        assertEquals(meetingList.get(1).getId(), ID_ELEVEN);
        assertEquals(meetingList.get(2).getId(), ID_THREE);
        assertEquals(meetingList.get(3).getId(), ID_SIX);
        assertEquals(meetingList.get(4).getId(), ID_EIGHT);
    }

    @Test(expected = NullPointerException.class)
    public void testGetMeetingListOnShouldThrowForNullDate() {
        contactsCM.getMeetingListOn(NULL_CAL);
    }

    @Test
    public void testGetEmptyPastMeetingListForIsEmpty() {
        Contact testContact = contactsCM.getContacts(CONTACT_1_ID).stream().findFirst().get();

        List<PastMeeting> pastMeetingList = contactsCM.getPastMeetingListFor(testContact);

        assertEquals(pastMeetingList.size(), EMPTY_SIZE);
        assertTrue(pastMeetingList.isEmpty());
    }

    @Test
    public void testGetPastMeetingListForIsNotEmptyAndSorted() {
        Contact testContact = meetingsCM.getContacts(CONTACT_1_ID).stream().findFirst().get();
        List<PastMeeting> pastMeetings = meetingsCM.getPastMeetingListFor(testContact);

        assertEquals(pastMeetings.size(), 4);
        assertEquals(pastMeetings.get(0).getId(), ID_TWO);
        assertEquals(pastMeetings.get(1).getId(), ID_THREE);
        assertEquals(pastMeetings.get(2).getId(), ID_SIX);
        assertEquals(pastMeetings.get(3).getId(), ID_EIGHT);
    }

    @Test
    public void testGetPastMeetingListForHasNoDuplicates() {
        Contact testContact = meetingsCM.getContacts(CONTACT_4_ID).stream().findFirst().get();
        List<PastMeeting> pastMeetings = meetingsCM.getPastMeetingListFor(testContact);

        assertEquals(pastMeetings.size(), 5);
        assertEquals(pastMeetings.get(1).getId(), ID_ELEVEN);
    }

    @Test(expected = NullPointerException.class)
    public void testGetPastMeetingListForWithNullContactShouldThrow () {
        contactsCM.getPastMeetingListFor(NULL_CONTACT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPastMeetingListForWithInvalidContactShouldThrow () {
        contactsCM.getPastMeetingListFor(new ContactImpl(ILLEGAL_ID_1, NON_EXISTENT_CONTACT_NAME));
    }

    @Test
    public void testAddMeetingNotes () {
        Calendar futureTime = new GregorianCalendar();
        futureTime.add(Calendar.MILLISECOND, ONE_MILLISECOND);
        contactsCM.addFutureMeeting(contactsA, futureTime);

        // pause for 5 milliseconds to ensure the new Future meeting
        // is now in the past
        try {
            Thread.sleep(FIVE_MILLISECONDS);
        } catch (InterruptedException iEx) {
            iEx.printStackTrace();
        }

        contactsCM.addMeetingNotes(ID_ONE, MEETING_NOTES);
        assertEquals(contactsCM.getPastMeeting(ID_ONE).getNotes(), MEETING_NOTES);
    }

    @Test
    public void testAppendMeetingNotes () {
        contactsCM.addNewPastMeeting(contactsA, pastDate, MEETING_NOTES);
        contactsCM.addMeetingNotes(ID_ONE, MEETING_NOTES_2);
        contactsCM.addMeetingNotes(ID_ONE, MEETING_NOTES_3);

        assertEquals(contactsCM.getPastMeeting(ID_ONE).getNotes(),
                MEETING_NOTES + NOTES_DELIMITER + MEETING_NOTES_2 + NOTES_DELIMITER + MEETING_NOTES_3);
    }

    @Test(expected = NullPointerException.class)
    public void testAddMeetingNotesWithNullNotes () {
        contactsCM.addMeetingNotes(ID_ONE, NULL_STRING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMeetingNotesWithInvalidId () {
        contactsCM.addMeetingNotes(ILLEGAL_ID_3, MEETING_NOTES);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddMeetingNotesToFutureMeeting () {
        contactsCM.addFutureMeeting(contactsA, futureDate);
        contactsCM.addMeetingNotes(ID_ONE, MEETING_NOTES);
    }
}
