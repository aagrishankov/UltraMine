package org.ultramine.service

import org.ultramine.core.service.ServiceDelegate
import org.ultramine.server.service.ServiceDelegateGenerator
import spock.lang.Specification

class ServiceDelegateGeneratorTest extends Specification {
	def "MakeInterfaceDelegate"() {
		setup:
		ServiceDelegate<TestInterface> delegate = ServiceDelegateGenerator.makeServiceDelegate(getClass(), "qwe", TestInterface.class).newInstance();
		def receiver = Mock(TestInterface)
		delegate.setProvider(receiver)
		def wrapper = (TestInterface) delegate;

		expect:
		wrapper == delegate.asService()
		receiver == delegate.getProvider()

		when:
		wrapper.testBooleanArg(true)
		wrapper.testByteArg((byte) 1)
		wrapper.testShortArg((short) 1)
		wrapper.testIntArg(1)
		wrapper.testLongArg(Long.MAX_VALUE, 1)
		wrapper.testPrimitives(false, (byte)1, (short)2, 3, 4L, 5f, 6d);
		wrapper.testObject("123")
		then:
		1 * receiver.testBooleanArg(true);
		1 * receiver.testByteArg(1);
		1 * receiver.testShortArg(1)
		1 * receiver.testIntArg(1)
		1 * receiver.testLongArg(Long.MAX_VALUE, 1)
		1 * receiver.testPrimitives(false, 1, 2, 3, 4L, 5f, 6d)
		1 * receiver.testObject("123")

		when:
		receiver.testReturnInt() >> 15
		receiver.testReturnLong() >> 16
		receiver.testReturnObject() >> "123"
		then:
		wrapper.testReturnInt() == 15
		wrapper.testReturnLong() == 16
		wrapper.testReturnObject() == "123"
	}

	interface TestInterface {
		void testBooleanArg(boolean b);
		void testByteArg(byte b);
		void testShortArg(short b);
		void testIntArg(int b);
		void testLongArg(long b, long b1);
		void testPrimitives(boolean b, byte bt, short s, int i, long l, float f, double d);
		void testObject(String str)
		int testReturnInt();
		long testReturnLong();
		String testReturnObject();
	}
}
