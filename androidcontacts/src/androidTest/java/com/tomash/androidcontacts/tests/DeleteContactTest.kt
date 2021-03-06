package com.tomash.androidcontacts.tests

import com.tomash.androidcontacts.BaseTest
import com.tomash.androidcontacts.contactgetter.entity.ContactData
import com.tomash.androidcontacts.contactgetter.main.contactsDeleter.ContactsDeleter
import com.tomash.androidcontacts.contactgetter.main.contactsDeleter.UnsuccessfulDeleteException
import com.tomash.androidcontacts.utils.ACResultTestRule
import com.tomash.androidcontacts.utils.context
import com.tomash.androidcontacts.utils.createRandomContactData
import com.tomash.androidcontacts.utils.getAllContacts
import com.tomash.androidcontacts.utils.mockContactDataList
import com.tomash.androidcontacts.utils.saveAll
import com.tomash.androidcontacts.utils.wrap
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class DeleteContactTest : BaseTest() {

    @get:Rule
    var callbackRule = ACResultTestRule()

    @Test
    fun deletesCorrectlyByOne() {
        mockContactDataList().saveAll()
        val deleter = ContactsDeleter(context)
        getAllContacts().forEach {
            deleter.deleteContact(it, wrap(
                callbackRule.shouldComplete(),
                callbackRule.shouldGetResult(it))
            )
        }
        Assert.assertTrue(getAllContacts().isEmpty())
    }

    @Test
    fun deletesCorrectlyAll() {
        mockContactDataList().saveAll()
        val deleter = ContactsDeleter(context)
        val allContacts = getAllContacts()
        deleter.deleteContacts(allContacts, wrap(
            callbackRule.shouldComplete(),
            callbackRule.shouldGetResult(allContacts)
        ))
        Assert.assertTrue(getAllContacts().isEmpty())
    }

    @Test
    fun oneContactDeleteFails() {
        val data = createRandomContactData()
        val deleter = ContactsDeleter(context)
        deleter.deleteContact(data, callbackRule.shouldFailWithError(UnsuccessfulDeleteException()))
    }

    @Test
    fun multipleContactDeleteFails() {
        val data = createRandomContactData()
        val deleter = ContactsDeleter(context)
        deleter.deleteContacts(listOf(data, data),
            callbackRule.shouldFailWithError(mapOf(
                data to UnsuccessfulDeleteException(),
                data to UnsuccessfulDeleteException())
            ))
    }

    @Test
    fun mixOfSuccessAndFailWorks() {
        mockContactDataList().saveAll()
        val allSavedContacts = getAllContacts()
        val data = createRandomContactData()
        val deleter = ContactsDeleter(context)
        deleter.deleteContacts(listOf(allSavedContacts.first(), data),
            wrap<List<ContactData>, Map<ContactData, Exception>>(
                callbackRule.shouldFailWithError(mapOf(data to UnsuccessfulDeleteException())),
                callbackRule.shouldGetResult(listOf(allSavedContacts.first()))
            )
        )
    }

    @Test
    fun finishesWithCompleteOnEmptyList() {
        val deleter = ContactsDeleter(context)
        deleter.deleteContacts(listOf(),
            wrap(
                callbackRule.shouldHaveNoResults(),
                callbackRule.shouldComplete()
            )
        )
    }
}
