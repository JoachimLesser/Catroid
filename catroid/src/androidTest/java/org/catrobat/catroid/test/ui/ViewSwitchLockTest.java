/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.test.ui;

import android.support.test.runner.AndroidJUnit4;

import org.catrobat.catroid.test.utils.Reflection;
import org.catrobat.catroid.ui.ViewSwitchLock;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.locks.Lock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ViewSwitchLockTest {

	@Test
	public void testViewSwitchLock() {
		Lock viewSwitchLock = new ViewSwitchLock();
		assertFalse((Boolean) Reflection.getPrivateField(viewSwitchLock, "locked"));

		boolean returnValue = viewSwitchLock.tryLock();
		assertTrue(returnValue);
		assertTrue((Boolean) Reflection.getPrivateField(viewSwitchLock, "locked"));

		viewSwitchLock.unlock();
		assertFalse((Boolean) Reflection.getPrivateField(viewSwitchLock, "locked"));

		viewSwitchLock = new ViewSwitchLock();
		returnValue = viewSwitchLock.tryLock();
		assertTrue(returnValue);
		assertTrue((Boolean) Reflection.getPrivateField(viewSwitchLock, "locked"));

		returnValue = viewSwitchLock.tryLock();
		assertFalse(returnValue);
		assertTrue((Boolean) Reflection.getPrivateField(viewSwitchLock, "locked"));

		viewSwitchLock.unlock();
		assertFalse((Boolean) Reflection.getPrivateField(viewSwitchLock, "locked"));
	}

	@Test
	public void testViewSwitchAutoUnlock() throws InterruptedException {
		Lock viewSwitchLock = new ViewSwitchLock();
		long timeout = (Long) Reflection.getPrivateField(ViewSwitchLock.class, "UNLOCK_TIMEOUT");

		viewSwitchLock.tryLock();
		assertTrue((Boolean) Reflection.getPrivateField(viewSwitchLock, "locked"));
		Thread.sleep(timeout + 50);
		assertFalse((Boolean) Reflection.getPrivateField(viewSwitchLock, "locked"));
	}

	@Test
	public void testDefaultSettings() {
		assertEquals(200L, Reflection.getPrivateField(ViewSwitchLock.class, "UNLOCK_TIMEOUT"));
	}

	@Test
	public void testUnsupportedMethods() {
		Lock viewSwitchLock = new ViewSwitchLock();

		try {
			viewSwitchLock.lock();
			fail("Method is supported");
		} catch (UnsupportedOperationException expected) {
		}

		try {
			viewSwitchLock.lockInterruptibly();
			fail("Method is supported");
		} catch (UnsupportedOperationException expected) {
		} catch (Exception exception) {
			fail("An unexcpected excpetion occured");
		}

		try {
			viewSwitchLock.newCondition();
			fail("Method is supported");
		} catch (UnsupportedOperationException expected) {
		}

		try {
			viewSwitchLock.tryLock(1L, null);
			fail("Method is supported");
		} catch (UnsupportedOperationException expected) {
		} catch (Exception exception) {
			fail("An unexcpected excpetion occured");
		}
	}
}
