/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2017 The Catrobat Team
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

package org.catrobat.catroid.uiespresso.content.brick;

import android.support.test.runner.AndroidJUnit4;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.bricks.LegoEv3MotorStopBrick;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.uiespresso.util.BaseActivityInstrumentationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.catrobat.catroid.uiespresso.content.brick.BrickTestUtils.checkIfBrickAtPositionShowsString;
import static org.catrobat.catroid.uiespresso.content.brick.BrickTestUtils.checkIfSpinnerOnBrickAtPositionShowsString;
import static org.catrobat.catroid.uiespresso.content.brick.BrickTestUtils.checkIfValuesAvailableInSpinnerOnBrick;
import static org.catrobat.catroid.uiespresso.content.brick.BrickTestUtils.clickSelectCheckSpinnerValueOnBrick;

@RunWith(AndroidJUnit4.class)
public class LegoEv3MotorStopBrickTest {
	private int brickPosition;

	@Rule
	public BaseActivityInstrumentationRule<ScriptActivity> baseActivityTestRule = new
			BaseActivityInstrumentationRule<>(ScriptActivity.class, true, false);

	@Before
	public void setUp() throws Exception {
		BrickTestUtils.createProjectAndGetStartScript("LegoEv3MotorStopBrickTest").addBrick(new
				LegoEv3MotorStopBrick(LegoEv3MotorStopBrick.Motor.MOTOR_A));
		brickPosition = 1;
		baseActivityTestRule.launchActivity(null);
	}

	@Test
	public void legoEv3MotorStopBrickTest() {
		checkIfBrickAtPositionShowsString(0, "When program starts");
		checkIfBrickAtPositionShowsString(brickPosition, "Stop EV3 motor");

		checkIfSpinnerOnBrickAtPositionShowsString(R.id.ev3_stop_motor_spinner, brickPosition, R.string.ev3_motor_a);
		clickSelectCheckSpinnerValueOnBrick(R.id.ev3_stop_motor_spinner, brickPosition, R.string.ev3_motor_b);

		List<Integer> spinnerValuesResourceIds = Arrays.asList(
				R.string.ev3_motor_a,
				R.string.ev3_motor_b,
				R.string.ev3_motor_c,
				R.string.ev3_motor_d,
				R.string.ev3_motor_b_and_c,
				R.string.ev3_motor_all);
		checkIfValuesAvailableInSpinnerOnBrick(spinnerValuesResourceIds, R.id.ev3_stop_motor_spinner, brickPosition);
	}
}
