// IAidlInterface.aidl
package com.soulkun.binderbestpractice.app1;

import com.soulkun.binderbestpractice.app1.Person;

interface IAidlInterface {
    void addPerson(in Person person);
    List<Person> getPersonList();
    void setInPerson(in Person person);
    void setOutPerson(out Person person);
    void setInOutPerson(inout Person person);
}