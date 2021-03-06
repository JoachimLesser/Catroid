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

package org.catrobat.catroid.test.sensing;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.catrobat.catroid.CatroidApplication;
import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.FormulaBrick;
import org.catrobat.catroid.test.utils.TestUtils;
import org.catrobat.catroid.utils.ScreenValueHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CollisionFormulaConversionTest {

	private static final String COLLISION_TEST_PROJECT = "COLLISION_TEST_PROJECT";
	private static final float OLD_CATROBAT_LANGUAGE_VERSION = 0.992f;
	private ProjectManager projectManager;

	@Before
	public void setUp() throws Exception {
		ScreenValueHandler.updateScreenWidthAndHeight(InstrumentationRegistry.getContext());
		projectManager = ProjectManager.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		projectManager.setProject(null);
		TestUtils.deleteProjects(COLLISION_TEST_PROJECT);
		TestUtils.removeFromPreferences(InstrumentationRegistry.getContext(), Constants.PREF_PROJECTNAME_KEY);
	}

	@Test
	public void testCatrobatLanguageVersionUpdated() throws IOException {
		TestUtils.createTestProjectOnLocalStorageWithCatrobatLanguageVersion(OLD_CATROBAT_LANGUAGE_VERSION);
		try {
			projectManager.loadProject(TestUtils.DEFAULT_TEST_PROJECT_NAME, InstrumentationRegistry.getTargetContext());
		} catch (Exception e) {
			fail("couldn't load project");
		}
		assertEquals(Constants.CURRENT_CATROBAT_LANGUAGE_VERSION,
				projectManager.getCurrentProject().getCatrobatLanguageVersion());
		TestUtils.deleteProjects();
	}

	@Test
	public void testFormulaUpdated() throws IOException {
		String firstSpriteName = "a";
		String secondSpriteName = "b";
		String thirdSpriteName = "ab";
		String collisionTag = CatroidApplication.getAppContext().getString(R.string
				.formula_editor_function_collision);
		Project project = TestUtils.createProjectWithOldCollisionFormulas(COLLISION_TEST_PROJECT,
				InstrumentationRegistry.getTargetContext(),
				firstSpriteName, secondSpriteName, thirdSpriteName, collisionTag);

		project.updateCollisionFormulasToVersion(0.993f);

		Sprite sprite1 = project.getDefaultScene().getSprite(firstSpriteName);
		Brick brick = sprite1.getScript(0).getBrick(0);
		if (brick instanceof FormulaBrick) {
			FormulaBrick formulaBrick = (FormulaBrick) brick;
			String newFormula = formulaBrick.getFormulas().get(0).getDisplayString(InstrumentationRegistry
					.getTargetContext());
			String expected = collisionTag + "(" + thirdSpriteName + ") ";
			assertEquals(expected, newFormula);
		} else {
			fail("brick is no instance of FormulaBrick");
		}
		TestUtils.deleteProjects();
	}

	@Test
	public void testFormulaUpdatedWithLanguageConversion() throws IOException {
		String firstSpriteName = "sprite1";
		String secondSpriteName = "sprite2";
		String thirdSpriteName = "sprite3";

		//Set to US locale
		Resources res = CatroidApplication.getAppContext().getResources();
		Configuration conf = res.getConfiguration();
		Locale savedLocale = conf.locale;
		conf.locale = Locale.US;
		res.updateConfiguration(conf, null);
		String collisionTag = res.getString(R.string.formula_editor_function_collision);

		// restore original locale
		conf.locale = savedLocale;
		res.updateConfiguration(conf, null);

		collisionTag = CatroidApplication.getAppContext().getString(R.string
				.formula_editor_function_collision);

		Project project = TestUtils.createProjectWithOldCollisionFormulas(COLLISION_TEST_PROJECT,
				InstrumentationRegistry.getTargetContext(),
				firstSpriteName, secondSpriteName, thirdSpriteName, collisionTag);
		project.updateCollisionFormulasToVersion(0.993f);

		Sprite sprite1 = project.getDefaultScene().getSprite(firstSpriteName);
		Brick brick = sprite1.getScript(0).getBrick(0);
		if (brick instanceof FormulaBrick) {
			FormulaBrick formulaBrick = (FormulaBrick) brick;
			String newFormula = formulaBrick.getFormulas().get(0).getDisplayString(InstrumentationRegistry
					.getTargetContext());
			String expected = collisionTag + "(" + thirdSpriteName + ") ";
			assertEquals(expected, newFormula);
		} else {
			fail("brick is no instance of FormulaBrick");
		}
		TestUtils.deleteProjects();
	}
}
