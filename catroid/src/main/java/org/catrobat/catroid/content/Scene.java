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
package org.catrobat.catroid.content;

import android.support.annotation.NonNull;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.FormulaBrick;
import org.catrobat.catroid.content.bricks.UserListBrick;
import org.catrobat.catroid.content.bricks.UserVariableBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.datacontainer.DataContainer;
import org.catrobat.catroid.io.XStreamFieldKeyOrder;
import org.catrobat.catroid.physics.PhysicsWorld;
import org.catrobat.catroid.utils.PathBuilder;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.catrobat.catroid.common.Constants.AUTOMATIC_SCREENSHOT_FILE_NAME;
import static org.catrobat.catroid.common.Constants.BACKPACK_SCENE_DIRECTORY;
import static org.catrobat.catroid.common.Constants.MANUAL_SCREENSHOT_FILE_NAME;

@XStreamAlias("scene")
// Remove checkstyle disable when https://github.com/checkstyle/checkstyle/issues/1349 is fixed
// CHECKSTYLE DISABLE IndentationCheck FOR 5 LINES
@XStreamFieldKeyOrder({
		"name",
		"objectList",
		"data"
})
public class Scene implements Serializable {

	private static final long serialVersionUID = 1L;

	@XStreamAlias("name")
	private String name;
	@XStreamAlias("objectList")
	private List<Sprite> spriteList = new ArrayList<>();
	@XStreamAlias("data")
	private DataContainer dataContainer = null;

	private transient PhysicsWorld physicsWorld;
	private transient Project project;

	public transient boolean firstStart = true;

	public Scene() {
	}

	public Scene(String name, @NonNull Project project) {
		this.name = name;
		this.project = project;
		dataContainer = new DataContainer(project);
	}

	public String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public Project getProject() {
		return project;
	}

	public synchronized void setProject(Project project) {
		this.project = project;
	}

	public List<Sprite> getSpriteList() {
		return spriteList;
	}

	public List<String> getSpriteNames() {
		List<String> spriteNames = new ArrayList<>();

		for (Sprite sprite : spriteList) {
			spriteNames.add(sprite.getName());
		}

		return spriteNames;
	}

	public Sprite getSprite(String spriteName) {
		for (Sprite sprite : spriteList) {
			if (spriteName.equals(sprite.getName())) {
				return sprite;
			}
		}
		return null;
	}

	public Sprite getBackgroundSprite() {
		if (spriteList.size() > 0) {
			return spriteList.get(0);
		}
		return null;
	}

	public synchronized void addSprite(Sprite sprite) {
		if (spriteList.contains(sprite)) {
			return;
		}
		spriteList.add(sprite);
	}

	public synchronized boolean removeSprite(Sprite sprite) {
		return spriteList.remove(sprite);
	}

	public DataContainer getDataContainer() {
		return dataContainer;
	}

	public synchronized void setDataContainer(DataContainer container) {
		dataContainer = container;
	}

	public PhysicsWorld getPhysicsWorld() {
		if (physicsWorld == null) {
			resetPhysicsWorld();
		}
		return physicsWorld;
	}

	public synchronized PhysicsWorld resetPhysicsWorld() {
		return (physicsWorld = new PhysicsWorld(
				project.getXmlHeader().virtualScreenWidth,
				project.getXmlHeader().virtualScreenHeight));
	}

	public synchronized void setPhysicsWorld(PhysicsWorld world) {
		physicsWorld = world;
	}

	public File getDirectory() {
		if (project == null) {
			return new File(BACKPACK_SCENE_DIRECTORY, name);
		} else {
			return new File(PathBuilder.buildScenePath(project.getName(), name));
		}
	}

	public boolean hasScreenshot() {
		File automaticScreenshot = new File(getDirectory(), AUTOMATIC_SCREENSHOT_FILE_NAME);
		File manualScreenshot = new File(getDirectory(), MANUAL_SCREENSHOT_FILE_NAME);
		return automaticScreenshot.exists() || manualScreenshot.exists();
	}

	public boolean mergeProjectVariables() {
		List<String> variables = new ArrayList<>();
		List<String> lists = new ArrayList<>();

		for (Sprite sprite : spriteList) {
			for (Brick brick : sprite.getAllBricks()) {
				if (brick instanceof UserVariableBrick
						&& !variables.contains(((UserVariableBrick) brick).getUserVariable().getName())) {
					variables.add(((UserVariableBrick) brick).getUserVariable().getName());
				}
				if (brick instanceof UserListBrick
						&& !lists.contains(((UserListBrick) brick).getUserList().getName())) {
					lists.add(((UserListBrick) brick).getUserList().getName());
				}
				if (brick instanceof FormulaBrick) {
					for (Formula formula : ((FormulaBrick) brick).getFormulas()) {
						formula.getVariableAndListNames(variables, lists);
					}
				}
			}
		}

		for (String variable : variables) {
			if (dataContainer.variableExistsInAnySprite(spriteList, variable)) {
				return false;
			}
			if (!dataContainer.existProjectVariableWithName(variable)
					&& !dataContainer.existUserVariableWithName(variable)) {
				dataContainer.addProjectUserVariable(variable);
			}
		}

		for (String list : lists) {
			if (dataContainer.existListInAnySprite(spriteList, list)) {
				return false;
			}
			if (!dataContainer.existProjectListWithName(list)) {
				dataContainer.addProjectUserList(list);
			}
		}

		return true;
	}

	public void removeAllClones() {
		ProjectManager.getInstance().getCurrentProject().removeInvalidVariablesAndLists(dataContainer);
		dataContainer.removeVariablesOfClones();
		for (Sprite s : new ArrayList<>(spriteList)) {
			if (s.isClone()) {
				spriteList.remove(s);
			}
		}
	}

	public Set<String> getBroadcastMessagesInUse() {
		Set<String> messagesInUse = new LinkedHashSet<>();
		for (Sprite currentSprite : spriteList) {
			for (Script currentScript : currentSprite.getScriptList()) {
				if (currentScript instanceof BroadcastScript) {
					messagesInUse.add(((BroadcastScript) currentScript).getBroadcastMessage());
				}
				for (Brick currentBrick : currentScript.getBrickList()) {
					if (currentBrick instanceof BroadcastMessageBrick) {
						messagesInUse.add(((BroadcastMessageBrick) currentBrick).getBroadcastMessage());
					}
				}
			}
		}
		return messagesInUse;
	}

	public synchronized void correctUserVariableAndListReferences() {
		for (Sprite sprite : spriteList) {
			for (Brick brick : sprite.getAllBricks()) {
				if (brick instanceof UserVariableBrick) {
					((UserVariableBrick) brick).setUserVariable(dataContainer.getUserVariable(sprite,
							((UserVariableBrick) brick).getUserVariable().getName()));
				}

				if (brick instanceof UserListBrick) {
					((UserListBrick) brick).setUserList(dataContainer.getUserList(sprite,
							((UserListBrick) brick).getUserList().getName()));
				}
			}
		}
	}
}
